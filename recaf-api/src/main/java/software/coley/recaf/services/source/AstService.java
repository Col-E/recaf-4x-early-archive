package software.coley.recaf.services.source;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import me.coley.cafedude.classfile.ConstantPoolConstants;
import org.objectweb.asm.ClassReader;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.internal.JavaTypeCache;
import software.coley.recaf.cdi.WorkspaceScoped;
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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Service for tracking shared data for AST parsing.
 *
 * @author Matt Coley
 */
@WorkspaceScoped
public class AstService implements Service, WorkspaceModificationListener, ResourceJvmClassListener {
	public static final String ID = "ast";
	private final AstServiceConfig config;
	private final JavaTypeCacheExt javaTypeCache = new JavaTypeCacheExt();
	private final Workspace workspace;

	@Inject
	public AstService(@Nonnull AstServiceConfig config,
					  @Nonnull Workspace workspace,
					  @Nonnull WorkspaceManager workspaceManager) {
		this.config = config;
		this.workspace = workspace;
		workspaceManager.addDefaultWorkspaceModificationListeners(this);
		workspace.getPrimaryResource().addListener(this);
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
		Set<String> classNames = new HashSet<>();
		ClassReader reader = target.getClassReader();
		int itemCount = reader.getItemCount();
		char[] buffer = new char[reader.getMaxStringLength()];
		for (int i = 1; i < itemCount; i++) {
			int offset = reader.getItem(i);
			if (offset >= 10) {
				int itemTag = reader.readByte(offset - 1);
				if (itemTag == ConstantPoolConstants.CLASS) {
					String className = reader.readUTF8(offset, buffer);
					classNames.add(className);
				}
			}
		}

		// Collect bytes of all referenced classes.
		byte[][] classpath = workspace.getAllResources(false).stream()
				.flatMap(WorkspaceResource::jvmClassBundleStream)
				.flatMap(Bundle::stream)
				.filter(info -> classNames.contains(info.getName()))
				.map(JvmClassInfo::getBytecode)
				.toArray(byte[][]::new);
		return JavaParser.fromJavaVersion()
				.classpath(classpath)
				.typeCache(javaTypeCache)
				.build();
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

	@Override
	public void onAddLibrary(@Nonnull Workspace workspace, @Nonnull WorkspaceResource library) {
		// no-op
	}

	@Override
	public void onRemoveLibrary(@Nonnull Workspace workspace, @Nonnull WorkspaceResource library) {
		for (String name : library.getJvmClassBundle().keySet())
			javaTypeCache.remove(name);
	}

	@Override
	public void onNewClass(@Nonnull WorkspaceResource resource, @Nonnull JvmClassBundle bundle, @Nonnull JvmClassInfo cls) {
		// no-op
	}

	@Override
	public void onUpdateClass(@Nonnull WorkspaceResource resource, @Nonnull JvmClassBundle bundle, @Nonnull JvmClassInfo oldCls, @Nonnull JvmClassInfo newCls) {
		javaTypeCache.remove(oldCls.getName());
	}

	@Override
	public void onRemoveClass(@Nonnull WorkspaceResource resource, @Nonnull JvmClassBundle bundle, @Nonnull JvmClassInfo cls) {
		javaTypeCache.remove(cls.getName());
	}

	private static class JavaTypeCacheExt extends JavaTypeCache {
		private final Map<String, ?> internalCache;

		@SuppressWarnings("unchecked")
		private JavaTypeCacheExt() {
			internalCache = (Map<String, ?>) Unchecked.get(() -> ReflectUtil.getDeclaredField(JavaTypeCache.class, "typeCache").get(this));
		}

		private void remove(@Nonnull String key) {
			internalCache.remove(key);
		}
	}
}
