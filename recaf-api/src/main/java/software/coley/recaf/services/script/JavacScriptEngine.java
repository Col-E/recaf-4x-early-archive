package software.coley.recaf.services.script;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jregex.Matcher;
import org.slf4j.Logger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.services.compile.*;
import software.coley.recaf.util.ClassDefiner;
import software.coley.recaf.util.RegexUtil;
import software.coley.recaf.util.StringUtil;
import software.coley.recaf.util.threading.ThreadPoolFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Basic implementation of {@link ScriptEngine} using {@link JavacCompiler}.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class JavacScriptEngine implements ScriptEngine {
	private static final Logger logger = Logging.get(JavacScriptEngine.class);
	private static final String SCRIPT_PACKAGE_NAME = "software.coley.recaf.generated";
	private static final String PATTERN_PACKAGE = "package ([\\w\\.\\*]+);?";
	private static final String PATTERN_IMPORT = "import ([\\w\\.\\*]+);?";
	private static final String PATTERN_CLASS_NAME = "(?<=class)\\s+(\\w+)\\s+(?:implements|extends|\\{)";
	private static final List<String> DEFAULT_IMPORTS = Arrays.asList(
			"java.io.*",
			"java.nio.file.*",
			"java.util.*",
			"software.coley.recaf.*",
			"software.coley.recaf.analytics.logging.*",
			"software.coley.recaf.info.*",
			"software.coley.recaf.info.annotation.*",
			"software.coley.recaf.info.builder.*",
			"software.coley.recaf.info.member.*",
			"software.coley.recaf.info.properties.*",
			"software.coley.recaf.util.*",
			"org.objectweb.asm.*",
			"org.objectweb.asm.tree.*"
	);
	private final Map<Integer, GenerateResult> generateResultMap = new HashMap<>();
	private final ExecutorService compileAndRunPool = ThreadPoolFactory.newSingleThreadExecutor("script-loader");
	private final JavacCompiler compiler;
	private final ScriptEngineConfig config;

	@Inject
	public JavacScriptEngine(JavacCompiler compiler, ScriptEngineConfig config) {
		this.compiler = compiler;
		this.config = config;
	}

	@Nonnull
	@Override
	public String getServiceId() {
		return SERVICE_ID;
	}

	@Nonnull
	@Override
	public ScriptEngineConfig getServiceConfig() {
		return config;
	}

	@Override
	public CompletableFuture<ScriptResult> run(String script) {
		return CompletableFuture.supplyAsync(() -> handleExecute(script), compileAndRunPool);
	}

	/**
	 * Compiles and executes the script.
	 * If the same script has already been compiled previously, the prior class reference will be used
	 * to reduce duplicate compilations.
	 *
	 * @param script
	 * 		Script to execute.
	 *
	 * @return Result of script execution.
	 */
	private ScriptResult handleExecute(String script) {
		int hash = script.hashCode();
		GenerateResult result;
		if (RegexUtil.matchesAny(PATTERN_CLASS_NAME, script)) {
			logger.info("Executing script class");
			result = generateResultMap.computeIfAbsent(hash, n -> generateStandardClass(script));
		} else {
			logger.info("Executing script");
			String className = "Script" + Math.abs(hash);
			result = generateResultMap.computeIfAbsent(hash, n -> generateScriptClass(className, script));
		}
		if (result.cls != null) {
			try {
				Method run = result.cls.getDeclaredMethod("run");
				run.invoke(null);
				logger.info("Successfully ran script");
				return new ScriptResult(result.diagnostics);
			} catch (Exception ex) {
				logger.error("Failed to execute script", ex);
				return new ScriptResult(result.diagnostics, ex);
			}
		} else {
			logger.error("Failed to compile script");
			return new ScriptResult(result.diagnostics);
		}
	}

	/**
	 * Used when the script contains a class definition in itself.
	 * Adds the default script package name, if no package is defined.
	 *
	 * @param source
	 * 		Initial source of the script.
	 *
	 * @return Compiler result wrapper containing the loaded class reference.
	 */
	private GenerateResult generateStandardClass(String source) {
		// Extract package name
		String packageName = SCRIPT_PACKAGE_NAME;
		Matcher matcher = RegexUtil.getMatcher(PATTERN_PACKAGE, source);
		if (matcher.find())
			packageName = matcher.group(1);
		else
			source = "package " + packageName + "; " + source;
		packageName = packageName.replace('.', '/');

		// Extract class name
		String className = null;
		matcher = RegexUtil.getMatcher(PATTERN_CLASS_NAME, source);
		if (matcher.find()) {
			className = packageName + "/" + matcher.group(1);
		} else {
			return new GenerateResult(null, List.of(
					new CompilerDiagnostic(-1, "Could not determine name of class", CompilerDiagnostic.Level.ERROR)));
		}

		// Compile the class
		return generate(className, source);
	}

	/**
	 * Used when the script immediately starts with the code.
	 * This will wrap that content in a basic class.
	 *
	 * @param className
	 * 		Name of the script class.
	 * @param script
	 * 		Initial source of the script.
	 *
	 * @return Compiler result wrapper containing the loaded class reference.
	 */
	private GenerateResult generateScriptClass(String className, String script) {
		Set<String> imports = new HashSet<>(DEFAULT_IMPORTS);
		Matcher matcher = RegexUtil.getMatcher(PATTERN_IMPORT, script);
		while (matcher.find()) {
			// Record import statement
			String importIdentifier = matcher.group(1);
			imports.add(importIdentifier);

			// Replace text with spaces to maintain script character offsets
			String importMatch = script.substring(matcher.start(), matcher.end());
			script = script.replace(importMatch, StringUtil.repeat(" ", importMatch.length()));
		}

		// Create code (just a basic class with a static 'run' method)
		StringBuilder code = new StringBuilder(
				"public class " + className + " implements Opcodes { public static void run() {\n" + script + "\n" + "}}");
		for (String imp : imports)
			code.insert(0, "import " + imp + "; ");
		code.insert(0, "package " + SCRIPT_PACKAGE_NAME + "; ");
		className = SCRIPT_PACKAGE_NAME.replace('.', '/') + "/" + className;

		// Compile the class
		return generate(className, code.toString());
	}

	/**
	 * @param className
	 * 		Name of the script class.
	 * @param source
	 * 		Source of the script.
	 *
	 * @return Compiler result wrapper containing the loaded class reference.
	 */
	private GenerateResult generate(String className, String source) {
		JavacArguments args = new JavacArgumentsBuilder()
				.withClassName(className)
				.withClassSource(source)
				.build();
		CompilerResult result = compiler.compile(args, null, null);
		if (result.wasSuccess()) {
			try {
				ClassDefiner definer = new ClassDefiner(result.getCompilations());
				Class<?> cls = definer.findClass(className.replace('/', '.'));
				return new GenerateResult(cls, result.getDiagnostics());
			} catch (Exception ex) {
				logger.error("Failed to define generated script class", ex);
			}
		}
		return new GenerateResult(null, Collections.emptyList());
	}

	private record GenerateResult(Class<?> cls, List<CompilerDiagnostic> diagnostics) {
	}
}
