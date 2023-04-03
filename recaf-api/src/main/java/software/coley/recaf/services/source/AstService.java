package software.coley.recaf.services.source;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import org.openrewrite.ExecutionContext;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.internal.JavaTypeCache;
import org.openrewrite.java.tree.J;
import software.coley.recaf.cdi.WorkspaceScoped;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.services.Service;
import software.coley.recaf.util.ReflectUtil;
import software.coley.recaf.util.Unchecked;
import software.coley.recaf.workspace.WorkspaceManager;
import software.coley.recaf.workspace.WorkspaceModificationListener;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.Bundle;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;
import software.coley.recaf.workspace.model.resource.ResourceJvmClassListener;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service for tracking shared data for AST parsing.
 *
 * @author Matt Coley
 */
@WorkspaceScoped
public class AstService implements Service {
	public static final String ID = "ast";
	private final AstServiceConfig config;
	private final JavaTypeCache javaTypeCache = new JavaTypeCacheExt();
	private final Workspace workspace;

	@Inject
	public AstService(@Nonnull AstServiceConfig config,
					  @Nonnull Workspace workspace) {
		this.config = config;
		this.workspace = workspace;
	}

	// TODO: Expose code-formatting system, which we can use to post-process code in decompilers
	//  - We may be able to allow the user to tweak styles like the IntelliJ format preview
	//  - See: 'org.openrewrite.java.style'

	/**
	 * Allocates a parser with the class-path of the complete workspace.
	 *
	 * @return New parser instance to handle any class in the workspace.
	 *
	 * @see #newParser(JvmClassInfo) Creates a parser with a smaller classpath <i>(Only classes referenced by the given info type)</i>
	 */
	@Nonnull
	public JavaParser newParser() {
		// Collect bytes of all classes in the workspace.
		byte[][] classpath = workspace.getAllResources(false).stream()
				.flatMap(WorkspaceResource::jvmClassBundleStream)
				.flatMap(Bundle::stream)
				.map(JvmClassInfo::getBytecode)
				.toArray(byte[][]::new);
		return JavaParser.fromJavaVersion()
				.classpath(classpath)
				.typeCache(javaTypeCache)
				.build();
	}

	/**
	 * Allocates a parser with the class-path of just classes referenced by the given class.
	 *
	 * @param target
	 * 		Class to target.
	 *
	 * @return New parser instance to handle source of the given class.
	 */
	@Nonnull
	public JavaParser newParser(@Nonnull JvmClassInfo target) {
		// Collect names of classes referenced.
		Set<String> classNames = target.getReferencedClasses();

		// Collect bytes of all referenced classes.
		byte[][] classpath = workspace.getAllResources(false).stream()
				.flatMap(WorkspaceResource::jvmClassBundleStream)
				.flatMap(Bundle::stream)
				.filter(info -> classNames.contains(info.getName()))
				.map(JvmClassInfo::getBytecode)
				.toArray(byte[][]::new);
		JavaParser parser = JavaParser.fromJavaVersion()
				.classpath(classpath)
				.typeCache(javaTypeCache)
				.build();
		return new DelegatingJavaParser(parser);
	}

	@Nonnull
	@Override
	public String getServiceId() {
		return ID;
	}

	@Nonnull
	@Override
	public AstServiceConfig getServiceConfig() {
		return config;
	}

	/**
	 * Modified cache impl that does not compress keys.
	 * For more memory cost, we get some additional performance.
	 */
	private static class JavaTypeCacheExt extends JavaTypeCache {
		private final Map<Object, Object> internalCache;

		@SuppressWarnings("unchecked")
		private JavaTypeCacheExt() {
			internalCache = (Map<Object, Object>) Unchecked.get(() -> ReflectUtil.getDeclaredField(JavaTypeCache.class, "typeCache").get(this));
		}

		@Override
		@Nullable
		@SuppressWarnings("unchecked")
		public <T> T get(@Nonnull String signature) {
			return (T) internalCache.get(signature);
		}

		@Override
		public void put(@Nonnull String signature, @Nonnull Object o) {
			internalCache.put(signature, o);
		}
	}
}
