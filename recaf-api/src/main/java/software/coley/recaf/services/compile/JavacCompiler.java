package software.coley.recaf.services.compile;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.coley.recaf.analytics.logging.DebuggingLogger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.services.Service;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import javax.tools.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Wrapper for {@link JavaCompiler}.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class JavacCompiler implements Service {
	public static final String SERVICE_ID = "java-compiler";
	private static final DebuggingLogger logger = Logging.get(JavacCompiler.class);
	private static final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	private final JavacCompilerConfig config;

	@Inject
	public JavacCompiler(JavacCompilerConfig config) {
		this.config = config;
	}

	/**
	 * @param arguments
	 * 		Wrapper of all arguments.
	 * @param workspace
	 * 		Optional workspace to include for additional classpath support.
	 * @param listener
	 * 		Optional listener to handle feedback with,
	 * 		mirroring what is reported by {@link CompilerResult#getDiagnostics()}
	 *
	 * @return Compilation result wrapper.
	 */
	public CompilerResult compile(@Nonnull JavacArguments arguments,
								  @Nullable Workspace workspace,
								  @Nullable JavacListener listener) {
		if (compiler == null)
			return new CompilerResult(new IllegalStateException("Cannot load 'javac' compiler."));

		String className = arguments.getClassName();

		// Class input map
		VirtualUnitMap unitMap = new VirtualUnitMap();
		unitMap.addSource(className, arguments.getClassSource());

		// Create a file manager to track files in-memory rather than on-disk
		List<WorkspaceResource> virtualClassPath = workspace == null ?
				Collections.emptyList() : workspace.getAllResources(true);
		List<CompilerDiagnostic> diagnostics = new ArrayList<>();
		JavacListener listenerWrapper = createRecordingListener(listener, diagnostics);
		JavaFileManager fmFallback = compiler.getStandardFileManager(listenerWrapper, Locale.getDefault(), UTF_8);
		JavaFileManager fm = new VirtualFileManager(unitMap, virtualClassPath, fmFallback);

		// Populate arguments
		List<String> args = new ArrayList<>();

		// Classpath
		String cp = arguments.getClassPath();
		if (cp != null) {
			args.add("-classpath");
			args.add(cp);
			logger.debugging(l -> l.info("Compiler classpath: {}", cp));
		}

		// Target version
		int target = arguments.getVersionTarget();
		args.add("--release");
		args.add(Integer.toString(target));
		logger.debugging(l -> l.info("Compiler target: {}", target));

		// Debug info
		String debugArg = arguments.createDebugValue();
		args.add(debugArg);
		logger.debugging(l -> l.info("Compiler debug: {}", debugArg));

		// Invoke compiler
		try {
			JavaCompiler.CompilationTask task =
					compiler.getTask(null, fm, listenerWrapper, args, null, unitMap.getFiles());
			if (task.call()) {
				logger.debugging(l -> l.info("Compilation of '{}' finished", className));
			} else {
				logger.debugging(l -> l.error("Compilation of '{}' failed", className));
			}
			return new CompilerResult(unitMap.getCompilations(), diagnostics);
		} catch (RuntimeException ex) {
			logger.debugging(l -> l.error("Compilation of '{}' crashed: {}", className, ex));
			return new CompilerResult(ex);
		}
	}

	/**
	 * @return {@code true} when the compiler can be invoked.
	 */
	public static boolean isAvailable() {
		return compiler != null;
	}

	/**
	 * @param listener
	 * 		Optional listener to wrap.
	 * @param diagnostics
	 * 		List to add diagnostics to.
	 *
	 * @return Listener to encompass recording behavior and the user defined listener.
	 */
	private JavacListener createRecordingListener(@Nullable JavacListener listener,
												  @Nonnull List<CompilerDiagnostic> diagnostics) {
		return new ForwardingListener(listener) {
			@Override
			public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
				// Pass to user defined listener
				super.report(diagnostic);

				// Record the diagnostic to our output
				if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
					diagnostics.add(new CompilerDiagnostic(
							(int) diagnostic.getLineNumber(),
							diagnostic.getMessage(Locale.getDefault()),
							mapKind(diagnostic.getKind())
					));
				}
			}

			private CompilerDiagnostic.Level mapKind(Diagnostic.Kind kind) {
				switch (kind) {
					case ERROR:
						return CompilerDiagnostic.Level.ERROR;
					case WARNING:
					case MANDATORY_WARNING:
						return CompilerDiagnostic.Level.WARNING;
					case NOTE:
					case OTHER:
					default:
						return CompilerDiagnostic.Level.INFO;
				}
			}
		};
	}

	@Nonnull
	@Override
	public String getServiceId() {
		return SERVICE_ID;
	}

	@Nonnull
	@Override
	public JavacCompilerConfig getServiceConfig() {
		return config;
	}
}
