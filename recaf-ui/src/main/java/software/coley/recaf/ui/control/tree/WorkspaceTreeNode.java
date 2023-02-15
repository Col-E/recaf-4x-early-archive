package software.coley.recaf.ui.control.tree;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.info.Info;
import software.coley.recaf.util.StringUtil;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.Bundle;
import software.coley.recaf.workspace.model.resource.BasicWorkspaceResource;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;
import software.coley.recaf.workspace.model.resource.WorkspaceResourceBuilder;

/**
 * Tree item subtype for more convenience tree building operations.
 *
 * @author Matt Coley
 */
public class WorkspaceTreeNode extends FilterableTreeItem<WorkspaceTreePath> implements Comparable<WorkspaceTreeNode> {
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
	 * Removes an item from the tree by its {@link WorkspaceTreePath} equality.
	 *
	 * @param path
	 * 		Path to remove from the tree.
	 *
	 * @return {@code true} when removal is a success.
	 * {@code false} if nothing was removed.
	 */
	public boolean removeNodeByPath(@Nonnull WorkspaceTreePath path) {
		// Get node by path.
		WorkspaceTreeNode nodeByPath = getNodeByPath(path);

		// Get that node's parent, remove the child.
		if (nodeByPath != null && nodeByPath.getParent() instanceof WorkspaceTreeNode parentNode)
			return parentNode.removeSourceChild(nodeByPath);

		// No known node by path.
		return false;
	}

	/**
	 * <b>Requirements</b>
	 * <ul>
	 * <li>Works for {@link WorkspaceResource}, {@link Bundle}, and {@link Info} paths.
	 * But if you want to make a tree-path for just a directory <i>(with no child {@link Info})</i>
	 * this is not supported.</li>
	 * <li>Must always be called on the root node.</li>
	 * </ul>
	 *
	 * @param path
	 * 		Path to use to get or create the associated node.
	 *
	 * @return Node derived from the given path.
	 */
	@SuppressWarnings("unchecked")
	public WorkspaceTreeNode getOrCreateNodeByPath(@Nonnull WorkspaceTreePath path) {
		if (resource() == ROOT_RESOURCE) {
			WorkspaceTreeNode node = this;
			// Get or create required nodes to create node for the given path.
			// Now if you look at 'getOrCreateResourceChild' you will see if it does need to be created, it creates
			// child nodes for all of its bundles and their classes & files.
			//
			// You may think: "Wow isn't that creating unrelated data for this operation?"
			//
			// Normally, yes. But this method is used after the model has already been populated.
			// These operations are almost always going to just be getting existing instances.
			// Some situations to consider:
			//  - Getting path for new class added to the bundle
			//  - Getting path for any item from a newly added resource to the workspace
			//
			// Even in these circumstances, those new things should have already been created.
			// Logically, the workspace tree should listen to events and create/remove things.
			// Thus, if you call this method for the 'get' part of the logic, it will just be that.
			node = node.getOrCreateResourceChild(path.resource());
			if (path.bundle() != null)
				node = node.getOrCreateBundleChild((Bundle<Info>) path.bundle());
			if (path.info() != null)
				node = node.getOrCreateInfoChild(path.info());
			return node;
		} else {
			return error("Should not call 'insertNodeByPath' on non-root node");
		}
	}

	/**
	 * @param path
	 * 		Path associated with node to look for in tree.
	 *
	 * @return Node containing the path in the tree.
	 */
	@Nullable
	public WorkspaceTreeNode getNodeByPath(@Nonnull WorkspaceTreePath path) {
		if (matches(path))
			return this;

		// Recursively search children for path, if their path is a descendant of the passed path.
		for (TreeItem<WorkspaceTreePath> child : getChildren()) {
			if (child instanceof WorkspaceTreeNode childNode) {
				// Only recurse if the child belongs within the given path.
				if (path.isDescendantOf(childNode.getValue())) {
					WorkspaceTreeNode nodeByPath = childNode.getNodeByPath(path);
					if (nodeByPath != null) return nodeByPath;
				}
			}
		}

		// Not found.
		return null;
	}

	/**
	 * @param path
	 * 		Path to check against.
	 *
	 * @return {@code true} when the current node's path matches.
	 */
	public boolean matches(@Nonnull WorkspaceTreePath path) {
		return path.equals(getValue());
	}

	/**
	 * Adds a resource to the tree structure. Descendants are created too.
	 *
	 * @param resource
	 * 		Resource to add.
	 *
	 * @return Inserted or existing child.
	 */
	@Nonnull
	public WorkspaceTreeNode getOrCreateResourceChild(@Nonnull WorkspaceResource resource) {
		if (resource() == ROOT_RESOURCE) {
			// Get existing node if it exists.
			WorkspaceTreePath path = new WorkspaceTreePath(workspace(), resource, null, null, null);
			for (TreeItem<WorkspaceTreePath> child : getChildren()) {
				if (path.equals(child.getValue()))
					return (WorkspaceTreeNode) child;
			}

			// Create new node.
			WorkspaceTreeNode node = new WorkspaceTreeNode(path);
			resource.bundleStream().forEach(node::getOrCreateBundleChild);
			addAndSortChild(node);
			return node;
		} else {
			return error("Should not add resource to non-root node");
		}
	}

	/**
	 * Adds a bundle to the tree structure, descendants are created too.
	 *
	 * @param bundle
	 * 		Bundle to add.
	 *
	 * @return Inserted or existing child.
	 */
	@Nonnull
	private WorkspaceTreeNode getOrCreateBundleChild(@Nonnull Bundle<Info> bundle) {
		if (resource() != ROOT_RESOURCE && bundle() == null) {
			// Get existing node if it exists.
			WorkspaceTreePath path = new WorkspaceTreePath(workspace(), resource(), bundle, null, null);
			for (TreeItem<WorkspaceTreePath> child : getChildren()) {
				if (path.equals(child.getValue()))
					return (WorkspaceTreeNode) child;
			}

			// Create new node.
			WorkspaceTreeNode node = new WorkspaceTreeNode(path);
			bundle.forEach((key, info) -> node.getOrCreateInfoChild(info));
			addAndSortChild(node);
			return node;
		} else {
			return error("Should not add bundle to non-resource node");
		}
	}

	/**
	 * Adds an info to the tree structure.
	 *
	 * @param info
	 * 		Info to add.
	 *
	 * @return Inserted or existing child.
	 */
	@Nonnull
	private WorkspaceTreeNode getOrCreateInfoChild(@Nonnull Info info) {
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
						target.addAndSortChild(child);
					}

					// Prepare for next part.
					target = child;
				}

				// Get existing node if it exists.
				WorkspaceTreePath path = new WorkspaceTreePath(workspace(), resource(), bundle(), info.getName(), info);
				for (TreeItem<WorkspaceTreePath> child : target.getChildren()) {
					if (path.equals(child.getValue()))
						return (WorkspaceTreeNode) child;
				}

				// Add the target info item.
				WorkspaceTreeNode child = new WorkspaceTreeNode(path);
				target.addAndSortChild(child);
				return child;
			} catch (Throwable t) {
				// In case something is broken here, we want to be notified
				return error("Failed inserting info object: " + StringUtil.traceToString(t));
			}
		} else {
			return error("Should not add bundle to non-resource node");
		}
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
	private static <T> T error(String message) {
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
