package software.coley.recaf.ui.pane;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.scene.layout.BorderPane;
import software.coley.recaf.ui.control.tree.WorkspaceTree;
import software.coley.recaf.ui.control.tree.WorkspaceTreeFilterPane;
import software.coley.recaf.workspace.model.Workspace;

/**
 * Pane to display the current workspace in a navigable tree layout.
 *
 * @author Matt Coley
 * @see WorkspaceRootPane
 */
@Dependent
public class WorkspaceExplorerPane extends BorderPane {
	private final WorkspaceTree workspaceTree;

	/**
	 * @param workspace
	 * 		Workspace instance.
	 * @param workspaceTree
	 * 		Tree to display workspace with.
	 */
	@Inject
	public WorkspaceExplorerPane(@Nullable Workspace workspace, @Nonnull WorkspaceTree workspaceTree) {
		this.workspaceTree = workspaceTree;
		setCenter(workspaceTree);
		if (workspace != null)
			workspaceTree.createWorkspaceRoot(workspace);
		WorkspaceTreeFilterPane workspaceTreeFilterPane = new WorkspaceTreeFilterPane(workspaceTree);
		setBottom(workspaceTreeFilterPane);
	}

	/**
	 * @return Tree displaying a workspace.
	 */
	@Nonnull
	public WorkspaceTree getWorkspaceTree() {
		return workspaceTree;
	}
}
