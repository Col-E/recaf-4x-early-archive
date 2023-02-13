package software.coley.recaf.services.cell.builtin;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javafx.scene.control.ContextMenu;
import software.coley.recaf.info.FileInfo;
import software.coley.recaf.services.cell.*;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.FileBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

/**
 * Basic implementation for {@link FileContextMenuProviderFactory}.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class BasicFileContextMenuProviderFactory implements FileContextMenuProviderFactory {
	private final TextProviderService textService;
	private final IconProviderService iconService;

	@Inject
	public BasicFileContextMenuProviderFactory(@Nonnull TextProviderService textService,
											   @Nonnull IconProviderService iconService) {
		this.textService = textService;
		this.iconService = iconService;
	}

	@Nonnull
	@Override
	public ContextMenuProvider getFileInfoContextMenuProvider(@Nonnull Workspace workspace,
															  @Nonnull WorkspaceResource resource,
															  @Nonnull FileBundle bundle,
															  @Nonnull FileInfo info) {
		return () -> {
			TextProvider nameProvider = textService.getFileInfoTextProvider(workspace, resource, bundle, info);
			IconProvider iconProvider = iconService.getFileInfoIconProvider(workspace, resource, bundle, info);
			ContextMenu menu = new ContextMenu();
			addHeader(menu, nameProvider.makeText(), iconProvider.makeIcon());
			// TODO: implement operations
			return menu;
		};
	}
}
