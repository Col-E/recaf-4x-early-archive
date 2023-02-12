package software.coley.recaf.services.cell.builtin;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import software.coley.recaf.info.AndroidClassInfo;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.services.cell.ClassIconProviderFactory;
import software.coley.recaf.services.cell.IconProvider;
import software.coley.recaf.util.Icons;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.AndroidClassBundle;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

/**
 * Basic implementation for {@link ClassIconProviderFactory}.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class BasicClassIconProviderFactory implements ClassIconProviderFactory {
	private static final IconProvider CLASS = Icons.createProvider(Icons.CLASS);
	private static final IconProvider INTERFACE = Icons.createProvider(Icons.INTERFACE);
	private static final IconProvider ANNO = Icons.createProvider(Icons.ANNOTATION);
	private static final IconProvider ENUM = Icons.createProvider(Icons.ENUM);

	@Nonnull
	@Override
	public IconProvider getJvmClassInfoIconProvider(@Nonnull Workspace workspace,
													@Nonnull WorkspaceResource resource,
													@Nonnull JvmClassBundle bundle,
													@Nonnull JvmClassInfo info) {
		return classIconProvider(info);
	}

	@Nonnull
	@Override
	public IconProvider getAndroidClassInfoIconProvider(@Nonnull Workspace workspace,
														@Nonnull WorkspaceResource resource,
														@Nonnull AndroidClassBundle bundle,
														@Nonnull AndroidClassInfo info) {
		return classIconProvider(info);
	}

	private static IconProvider classIconProvider(ClassInfo info) {
		if (info.hasEnumModifier()) return ENUM;
		if (info.hasAnnotationModifier()) return ANNO;
		if (info.hasInterfaceModifier()) return INTERFACE;
		return CLASS;
	}
}
