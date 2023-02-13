package software.coley.recaf.services.cell;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.FileInfo;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.workspace.model.bundle.AndroidClassBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

/**
 * Base context menu provider factory.
 *
 * @author Matt Coley
 * @see ClassContextMenuProviderFactory For {@link JvmClassInfo} and {@link AndroidClassBundle} entries.
 * @see DirectoryContextMenuProviderFactory For directory entries, not linked to a specific {@link FileInfo}.
 * @see FileContextMenuProviderFactory For {@link FileInfo} entries.
 * @see PackageContextMenuProviderFactory  For package entries, not linked to a specific {@link ClassInfo}.
 * @see ResourceContextMenuProviderFactory For {@link WorkspaceResource} entries.
 */
public interface ContextMenuProviderFactory {
	/**
	 * @return Context menu provider that provides {@code null}.
	 */
	@Nonnull
	default ContextMenuProvider emptyProvider() {
		return () -> null;
	}

	/**
	 * Add a header item to the given menu.
	 *
	 * @param menu
	 * 		Menu to add to.
	 * @param title
	 * 		Header text content.
	 * @param graphic
	 * 		Header graphic.
	 */
	default void addHeader(@Nonnull ContextMenu menu, @Nullable String title, @Nullable Node graphic) {
		MenuItem header = new MenuItem(title, graphic);
		menu.getItems().add(header);
	}
}
