package software.coley.recaf.services.cell.builtin;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javafx.scene.control.ContextMenu;
import software.coley.recaf.services.cell.ContextMenuProvider;
import software.coley.recaf.services.cell.DirectoryContextMenuProviderFactory;
import software.coley.recaf.services.cell.IconProvider;
import software.coley.recaf.services.cell.IconProviderService;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.FileBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

/**
 * Basic implementation for {@link DirectoryContextMenuProviderFactory}.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class BasicDirectoryContextMenuProviderFactory implements DirectoryContextMenuProviderFactory {
	private final IconProviderService iconService;

	@Inject
	public BasicDirectoryContextMenuProviderFactory(@Nonnull IconProviderService iconService) {
		this.iconService = iconService;
	}

	@Nonnull
	@Override
	public ContextMenuProvider getDirectoryContextMenuProvider(@Nonnull Workspace workspace,
															   @Nonnull WorkspaceResource resource,
															   @Nonnull FileBundle bundle,
															   @Nonnull String directoryName) {
		return () -> {
			String name = directoryName.substring(directoryName.lastIndexOf('/') + 1); // TODO: escape name (configurable service)
			IconProvider iconProvider = iconService.getDirectoryIconProvider(workspace, resource, bundle, directoryName);
			ContextMenu menu = new ContextMenu();
			addHeader(menu, name, iconProvider.makeIcon());
			// TODO: implement operations
			return menu;
		};
	}
}
