package software.coley.recaf.ui.control.tree;

import jakarta.annotation.Nonnull;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeCell;

/**
 * Call for rendering {@link WorkspaceTreePath} items.
 *
 * @author Matt Coley
 */
public class WorkspaceTreeCell extends TreeCell<WorkspaceTreePath> {
	@Override
	protected void updateItem(WorkspaceTreePath item, boolean empty) {
		super.updateItem(item, empty);
		if (empty || item == null) {
			setText(null);
			setGraphic(null);
			setContextMenu(null);
		} else {
			setText(textOf(item));
			setGraphic(graphicOf(item));
			setContextMenu(contextMenuOf(item));
		}
	}

	@SuppressWarnings("all") // for the NPE, doesn't understand 'hasX' contracts.
	public static String textOf(@Nonnull WorkspaceTreePath item) {
		if (item.hasInfo()) {
			String name = item.info().getName();
			return name.substring(name.lastIndexOf('/') + 1);
		} else if (item.hasLocalPath()) {
			String path = item.localPath();
			return path.substring(path.lastIndexOf('/') + 1);
		} else if (item.hasBundle()) {
			return item.bundle().getClass().getName();
		} else {
			return item.resource().getClass().getName();
		}
	}

	public static Node graphicOf(@Nonnull WorkspaceTreePath item) {
		return null;
	}

	public static ContextMenu contextMenuOf(@Nonnull WorkspaceTreePath item) {
		return null;
	}
}
