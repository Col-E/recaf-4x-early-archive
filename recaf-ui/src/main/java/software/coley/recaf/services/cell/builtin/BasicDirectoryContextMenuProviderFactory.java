package software.coley.recaf.services.cell.builtin;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javafx.scene.control.ContextMenu;
import software.coley.recaf.services.cell.*;
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
	private final TextProviderService textService;
	private final IconProviderService iconService;

	@Inject
	public BasicDirectoryContextMenuProviderFactory(@Nonnull TextProviderService textService,
													@Nonnull IconProviderService iconService) {
		this.textService = textService;
		this.iconService = iconService;
	}

	@Nonnull
	@Override
	public ContextMenuProvider getDirectoryContextMenuProvider(@Nonnull Workspace workspace,
															   @Nonnull WorkspaceResource resource,
															   @Nonnull FileBundle bundle,
															   @Nonnull String directoryName) {
		return () -> {
			TextProvider nameProvider = textService.getDirectoryTextProvider(workspace, resource, bundle, directoryName);
			IconProvider iconProvider = iconService.getDirectoryIconProvider(workspace, resource, bundle, directoryName);
			ContextMenu menu = new ContextMenu();
			addHeader(menu, nameProvider.makeText(), iconProvider.makeIcon());
			// TODO: implement operations
			return menu;
		};
	}
}
