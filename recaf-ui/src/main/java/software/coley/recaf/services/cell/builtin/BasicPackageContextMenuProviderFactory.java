package software.coley.recaf.services.cell.builtin;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javafx.scene.control.ContextMenu;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.services.cell.ContextMenuProvider;
import software.coley.recaf.services.cell.IconProvider;
import software.coley.recaf.services.cell.IconProviderService;
import software.coley.recaf.services.cell.PackageContextMenuProviderFactory;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.ClassBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

/**
 * Basic implementation for {@link PackageContextMenuProviderFactory}.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class BasicPackageContextMenuProviderFactory implements PackageContextMenuProviderFactory {
	private final IconProviderService iconService;

	@Inject
	public BasicPackageContextMenuProviderFactory(@Nonnull IconProviderService iconService) {
		this.iconService = iconService;
	}

	@Nonnull
	@Override
	public ContextMenuProvider getPackageContextMenuProvider(@Nonnull Workspace workspace,
															 @Nonnull WorkspaceResource resource,
															 @Nonnull ClassBundle<? extends ClassInfo> bundle,
															 @Nonnull String packageName) {
		return () -> {
			String name = packageName.substring(packageName.lastIndexOf('/') + 1); // TODO: escape name (configurable service)
			IconProvider iconProvider = iconService.getPackageIconProvider(workspace, resource, bundle, packageName);
			ContextMenu menu = new ContextMenu();
			addHeader(menu, name, iconProvider.makeIcon());
			// TODO: implement operations
			return menu;
		};
	}
}
