package software.coley.recaf.services.cell.builtin;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javafx.scene.control.ContextMenu;
import software.coley.recaf.info.FileInfo;
import software.coley.recaf.services.cell.*;
import software.coley.recaf.services.navigation.Actions;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.FileBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

/**
 * Basic implementation for {@link FileContextMenuProviderFactory}.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class BasicFileContextMenuProviderFactory extends AbstractContextMenuProviderFactory
		implements FileContextMenuProviderFactory {
	@Inject
	public BasicFileContextMenuProviderFactory(@Nonnull TextProviderService textService,
											   @Nonnull IconProviderService iconService,
											   @Nonnull Actions actions) {
		super(textService, iconService, actions);
	}

	@Nonnull
	@Override
	public ContextMenuProvider getFileInfoContextMenuProvider(@Nonnull ContextSource source,
															  @Nonnull Workspace workspace,
															  @Nonnull WorkspaceResource resource,
															  @Nonnull FileBundle bundle,
															  @Nonnull FileInfo info) {
		return () -> {
			TextProvider nameProvider = textService.getFileInfoTextProvider(workspace, resource, bundle, info);
			IconProvider iconProvider = iconService.getFileInfoIconProvider(workspace, resource, bundle, info);
			ContextMenu menu = new ContextMenu();
			addHeader(menu, nameProvider.makeText(), iconProvider.makeIcon());
			// TODO: implement operations
			//  - Go to
			//  - Copy
			//  - Delete
			//  - Refactor
			//    - Rename
			//    - Move
			//  - Search references
			//  - Override text-view language
			return menu;
		};
	}
}
