package software.coley.recaf.ui.control.tree;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.scene.control.TreeView;
import software.coley.recaf.info.AndroidClassInfo;
import software.coley.recaf.info.FileInfo;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.services.cell.IconProviderService;
import software.coley.recaf.util.FxThreadUtil;
import software.coley.recaf.workspace.WorkspaceCloseListener;
import software.coley.recaf.workspace.WorkspaceModificationListener;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.AndroidClassBundle;
import software.coley.recaf.workspace.model.bundle.FileBundle;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;
import software.coley.recaf.workspace.model.resource.ResourceAndroidClassListener;
import software.coley.recaf.workspace.model.resource.ResourceFileListener;
import software.coley.recaf.workspace.model.resource.ResourceJvmClassListener;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.util.List;

/**
 * Tree view for items within a {@link Workspace}.
 *
 * @author Matt Coley
 */
@Dependent
public class WorkspaceTree extends TreeView<WorkspaceTreePath> implements
		WorkspaceModificationListener, WorkspaceCloseListener,
		ResourceJvmClassListener, ResourceAndroidClassListener, ResourceFileListener {
	private WorkspaceTreeNode root;
	private Workspace workspace;

	/**
	 * Initialize empty tree.
	 * @param iconService Icon provider for cells.
	 */
	@Inject
	public WorkspaceTree(IconProviderService iconService) {
		setShowRoot(false);
		setCellFactory(param -> new WorkspaceTreeCell(iconService));
	}

	/**
	 * Sets the workspace, and creates a complete model for it.
	 *
	 * @param workspace
	 * 		Workspace to represent.
	 */
	public void createWorkspaceRoot(@Nullable Workspace workspace) {
		this.workspace = workspace;
		if (workspace == null) {
			root = null;
		} else {
			// Create root
			root = new WorkspaceTreeNode(workspace);
			List<WorkspaceResource> resources = workspace.getAllResources(false);
			for (WorkspaceResource resource : resources)
				root.createResourceChild(resource);

			// Add listeners
			workspace.addWorkspaceModificationListener(this);
			for (WorkspaceResource resource : resources)
				resource.addListener(this);
		}
		FxThreadUtil.run(() -> setRoot(root));
	}

	/**
	 * @param workspace
	 * 		Workspace to check.
	 *
	 * @return {@code true} when it matches our current {@link #workspace}.
	 */
	private boolean isTarget(Workspace workspace) {
		return this.workspace == workspace;
	}

	@Override
	public void onWorkspaceClosed(@Nonnull Workspace workspace) {
		// Workspace closed, disable tree.
		if (isTarget(workspace))
			setDisable(true);
	}

	@Override
	public void onAddLibrary(Workspace workspace, WorkspaceResource library) {
		if (isTarget(workspace))
			root.createResourceChild(library);
	}

	@Override
	public void onRemoveLibrary(Workspace workspace, WorkspaceResource library) {
		// TODO: Remove path
	}

	@Override
	public void onNewClass(WorkspaceResource resource, AndroidClassBundle bundle, AndroidClassInfo cls) {
		// TODO: Add path
	}

	@Override
	public void onUpdateClass(WorkspaceResource resource, AndroidClassBundle bundle, AndroidClassInfo oldCls, AndroidClassInfo newCls) {
		// TODO: Refresh path
	}

	@Override
	public void onRemoveClass(WorkspaceResource resource, AndroidClassBundle bundle, AndroidClassInfo cls) {
		// TODO: Remove path
	}

	@Override
	public void onNewFile(WorkspaceResource resource, FileBundle bundle, FileInfo file) {
		// TODO: Add path
	}

	@Override
	public void onUpdateFile(WorkspaceResource resource, FileBundle bundle, FileInfo oldFile, FileInfo newFile) {
		// TODO: Refresh path
	}

	@Override
	public void onRemoveFile(WorkspaceResource resource, FileBundle bundle, FileInfo file) {
		// TODO: Remove path
	}

	@Override
	public void onNewClass(WorkspaceResource resource, JvmClassBundle bundle, JvmClassInfo cls) {
		// TODO: Add path
	}

	@Override
	public void onUpdateClass(WorkspaceResource resource, JvmClassBundle bundle, JvmClassInfo oldCls, JvmClassInfo newCls) {
		// TODO: Refresh path
	}

	@Override
	public void onRemoveClass(WorkspaceResource resource, JvmClassBundle bundle, JvmClassInfo cls) {
		// TODO: Remove path
	}
}
