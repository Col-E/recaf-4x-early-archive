package software.coley.recaf.ui.pane;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.ui.control.tree.WorkspaceTree;
import software.coley.recaf.ui.control.tree.WorkspaceTreeFilterPane;
import software.coley.recaf.ui.dnd.DragAndDrop;
import software.coley.recaf.ui.dnd.FileDropListener;
import software.coley.recaf.workspace.PathLoadingManager;
import software.coley.recaf.workspace.model.Workspace;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Pane to display the current workspace in a navigable tree layout.
 *
 * @author Matt Coley
 * @see WorkspaceRootPane
 */
@Dependent
public class WorkspaceExplorerPane extends BorderPane implements FileDropListener {
	private static final Logger logger = Logging.get(WorkspaceExplorerPane.class);
	private final PathLoadingManager pathLoadingManager;
	private final WorkspaceTree workspaceTree;

	/**
	 * @param workspace
	 * 		Workspace instance.
	 * @param workspaceTree
	 * 		Tree to display workspace with.
	 */
	@Inject
	public WorkspaceExplorerPane(@Nonnull PathLoadingManager pathLoadingManager,
								 @Nullable Workspace workspace,
								 @Nonnull WorkspaceTree workspaceTree) {
		this.pathLoadingManager = pathLoadingManager;
		this.workspaceTree = workspaceTree;
		setCenter(workspaceTree);
		if (workspace != null)
			workspaceTree.createWorkspaceRoot(workspace);
		WorkspaceTreeFilterPane workspaceTreeFilterPane = new WorkspaceTreeFilterPane(workspaceTree);
		setBottom(workspaceTreeFilterPane);
		DragAndDrop.installFileSupport(this, this);
	}

	/**
	 * @return Tree displaying a workspace.
	 */
	@Nonnull
	public WorkspaceTree getWorkspaceTree() {
		return workspaceTree;
	}

	@Override
	public void onDragDrop(@Nonnull Region region, @Nonnull DragEvent event, @Nonnull List<Path> files) throws IOException {
		// Sanity check input
		if (files.isEmpty()) return;

		// Create new workspace from files
		Path primary = files.get(0);
		List<Path> supporting = files.size() > 1 ? files.subList(1, files.size()) : Collections.emptyList();
		pathLoadingManager.asyncNewWorkspace(primary, supporting, err -> {
			logger.error("Failed to create new workspace from dropped files", err);
		});

		// TODO: Config to determine what drag-drop operation does (new workspace, append to current)
		//  // Append files to current workspace
		//  pathLoadingManager.asyncAddSupportingResourcesToWorkspace(workspace, files, err -> {
		//  	logger.error("Failed to add supporting resources from dropped files", err);
		//  });
	}
}
