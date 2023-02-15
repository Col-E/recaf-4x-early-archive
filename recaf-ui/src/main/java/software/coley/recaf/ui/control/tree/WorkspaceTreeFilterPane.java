package software.coley.recaf.ui.control.tree;

import jakarta.annotation.Nonnull;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import software.coley.recaf.util.FxThreadUtil;
import software.coley.recaf.util.Lang;

/**
 * Pane component to filter what is visible in a given {@link WorkspaceTree}.
 *
 * @author Matt Coley
 */
public class WorkspaceTreeFilterPane extends BorderPane {
	/**
	 * @param tree
	 * 		Tree to filter.
	 */
	public WorkspaceTreeFilterPane(@Nonnull WorkspaceTree tree) {
		TextField textField = new TextField();
		textField.promptTextProperty().bind(Lang.getBinding("workspace.filter-prompt"));
		setCenter(textField);

		// TODO:
		//  - option to hide supporting resources
		//  - case sensitivity toggle

		// Setup tree item predicate property on FX thread.
		// The root is assigned on the FX thread, it won't be available if we call it immediately.
		FxThreadUtil.run(() -> {
			WorkspaceTreeNode root = (WorkspaceTreeNode) tree.getRoot();
			// We're not binding from the root's property since that will trigger immediately.
			// That will force-expand the entire workspace, which we do not want to do.
			textField.textProperty().addListener((ob, old, cur) -> {
				root.predicateProperty().set(item -> {
					WorkspaceTreePath path = item.getValue();
					return path.localPath() == null || path.localPath().contains(cur);
				});
			});
		});
	}
}
