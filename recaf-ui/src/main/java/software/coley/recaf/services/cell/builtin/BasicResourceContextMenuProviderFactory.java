package software.coley.recaf.services.cell.builtin;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javafx.scene.control.ContextMenu;
import software.coley.recaf.services.cell.ContextMenuProvider;
import software.coley.recaf.services.cell.IconProvider;
import software.coley.recaf.services.cell.IconProviderService;
import software.coley.recaf.services.cell.ResourceContextMenuProviderFactory;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

/**
 * Basic implementation for {@link ResourceContextMenuProviderFactory}.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class BasicResourceContextMenuProviderFactory implements ResourceContextMenuProviderFactory {
	private final IconProviderService iconService;

	@Inject
	public BasicResourceContextMenuProviderFactory(@Nonnull IconProviderService iconService) {
		this.iconService = iconService;
	}

	@Nonnull
	@Override
	public ContextMenuProvider getResourceContextMenuProvider(@Nonnull Workspace workspace,
															  @Nonnull WorkspaceResource resource) {
		return () -> {
			// TODO: Need to make 'thing to string' an injectable service too.
			//      Though for this, I don't think we need to make plugin support, so the impl can be much simpler than graphic/menu stuff
			String name = resource.getClass().getSimpleName();

			IconProvider iconProvider = iconService.getResourceIconProvider(workspace, resource);
			ContextMenu menu = new ContextMenu();
			addHeader(menu, name, iconProvider.makeIcon());
			// TODO: implement operations
			return menu;
		};
	}
}
