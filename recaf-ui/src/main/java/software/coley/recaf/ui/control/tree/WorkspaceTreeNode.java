package software.coley.recaf.ui.control.tree;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.info.Info;
import software.coley.recaf.util.CollectionUtil;
import software.coley.recaf.util.StringUtil;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.Bundle;
import software.coley.recaf.workspace.model.resource.BasicWorkspaceResource;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;
import software.coley.recaf.workspace.model.resource.WorkspaceResourceBuilder;

import java.util.List;

/**
 * Tree item subtype for more convenience tree building operations.
 *
 * @author Matt Coley
 */
public class WorkspaceTreeNode extends TreeItem<WorkspaceTreePath> implements Comparable<WorkspaceTreeNode> {
	private static final WorkspaceResource ROOT_RESOURCE = new DummyResource();
	private static final Logger logger = Logging.get(WorkspaceTreeNode.class);

	/**
	 * Create new root node.
	 *
	 * @param workspace
	 * 		Root workspace.
	 */
	public WorkspaceTreeNode(Workspace workspace) {
		setValue(new WorkspaceTreePath(workspace, ROOT_RESOURCE, null, null, null));
	}

	/**
	 * Create new node with path value.
	 *
	 * @param path
	 * 		Path of represented item.
	 */
	public WorkspaceTreeNode(WorkspaceTreePath path) {
		setValue(path);
	}

	/**
	 * @return {@link WorkspaceTreePath#workspace()} of current value.
	 */
	@Nonnull
	private Workspace workspace() {
		return getValue().workspace();
	}

	/**
	 * @return {@link WorkspaceTreePath#resource()} of current value.
	 */
	@Nonnull
	private WorkspaceResource resource() {
		return getValue().resource();
	}

	/**
	 * @return {@link WorkspaceTreePath#bundle()} of current value.
	 */
	@Nullable
	private Bundle<? extends Info> bundle() {
		return getValue().bundle();
	}

	/**
	 * @return {@link WorkspaceTreePath#info()} of current value.
	 */
	@Nullable
	private Info info() {
		return getValue().info();
	}

	/**
	 * Adds a resource to the tree structure. Descendants are created too.
	 *
	 * @param resource
	 * 		Resource to add.
	 */
	public void createResourceChild(@Nonnull WorkspaceResource resource) {
		if (resource() == ROOT_RESOURCE) {
			WorkspaceTreeNode node = new WorkspaceTreeNode(new WorkspaceTreePath(workspace(), resource, null, null, null));
			resource.bundleStream().forEach(node::createBundleChild);
			insertSorted(node);
		} else {
			error("Should not add resource to non-root node");

		}
	}

	/**
	 * Adds a bundle to the tree structure, descendants are created too.
	 *
	 * @param bundle
	 * 		Bundle to add.
	 */
	private void createBundleChild(@Nonnull Bundle<Info> bundle) {
		if (resource() != ROOT_RESOURCE && bundle() == null) {
			WorkspaceTreeNode node = new WorkspaceTreeNode(new WorkspaceTreePath(workspace(), resource(), bundle, null, null));
			bundle.forEach((key, info) -> node.createInfoChild(info));
			insertSorted(node);
		} else {
			error("Should not add bundle to non-resource node");
		}
	}

	/**
	 * Adds an info to the tree structure.
	 *
	 * @param info
	 * 		Info to add.
	 */
	private void createInfoChild(@Nonnull Info info) {
		if (bundle() != null && info() == null) {
			// Info names are separated by '/' for packages/directories.
			String name = info.getName();
			String[] nameParts = name.split("/");
			StringBuilder namePartBuilder = new StringBuilder();
			try {
				WorkspaceTreeNode target = this;
				for (int i = 0; i < nameParts.length - 1; i++) {
					namePartBuilder.append(nameParts[i]).append('/');
					String directoryName = namePartBuilder.substring(0, namePartBuilder.length() - 1);

					// Get child by directory name, if one exists.
					WorkspaceTreeNode child = null;
					for (TreeItem<WorkspaceTreePath> existingChild : target.getChildren()) {
						if (directoryName.equals(existingChild.getValue().localPath())) {
							child = (WorkspaceTreeNode) existingChild;
							break;
						}
					}

					// No matching child, we must create and insert one.
					if (child == null) {
						child = new WorkspaceTreeNode(new WorkspaceTreePath(workspace(), resource(), bundle(), directoryName, null));
						target.insertSorted(child);
					}

					// Prepare for next part.
					target = child;
				}

				// Add the target info item.
				WorkspaceTreeNode child = new WorkspaceTreeNode(new WorkspaceTreePath(workspace(), resource(), bundle(), null, info));
				target.insertSorted(child);
			} catch (Throwable t) {
				// In case something is broken here, we want to be notified
				error("Failed inserting info object: " + StringUtil.traceToString(t));
			}
		} else {
			error("Should not add bundle to non-resource node");
		}
	}

	@SuppressWarnings("unchecked")
	private void insertSorted(@Nonnull WorkspaceTreeNode node) {
		List<WorkspaceTreeNode> children = (List<WorkspaceTreeNode>) (List<?>) getChildren();
		int i = CollectionUtil.sortedInsertIndex(children, node);
		children.add(i, node);
	}

	@Override
	public int compareTo(@Nonnull WorkspaceTreeNode o) {
		return getValue().compareTo(o.getValue());
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + getValue().toString() + "]";
	}

	/**
	 * @param message
	 * 		Error message to log.
	 */
	private static void error(String message) {
		logger.error(message);
		throw new IllegalStateException(message);
	}


	/**
	 * Used to fill {@link WorkspaceResource} in {@link WorkspaceTreePath} for top-level tree node.
	 * Ensures non-null contract is fulfilled.
	 */
	private static class DummyResource extends BasicWorkspaceResource {
		private DummyResource() {
			super(new WorkspaceResourceBuilder());
		}

		@Override
		protected void setup() {
			// no-op
		}
	}
}
