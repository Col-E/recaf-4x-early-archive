package software.coley.recaf.ui.control.tree;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import javafx.scene.control.TreeItem;
import software.coley.recaf.ui.control.tree.path.PathNode;

/**
 * Tree item subtype for more convenience tree building operations.
 *
 * @author Matt Coley
 */
public class WorkspaceTreeNode extends FilterableTreeItem<PathNode<?>> implements Comparable<WorkspaceTreeNode> {
	/**
	 * Create new node with path value.
	 *
	 * @param path
	 * 		Path of represented item.
	 */
	public WorkspaceTreeNode(PathNode<?> path) {
		setValue(path);
	}

	/**
	 * Removes a tree node from the tree by its {@link PathNode} equality.
	 *
	 * @param path
	 * 		Path to remove from the tree.
	 *
	 * @return {@code true} when removal is a success.
	 * {@code false} if nothing was removed.
	 */
	public boolean removeNodeByPath(@Nonnull PathNode<?> path) {
		// Call from root node only.
		WorkspaceTreeNode root = this;
		while (root.getParent() instanceof WorkspaceTreeNode parentNode)
			root = parentNode;

		// Get node by path.
		WorkspaceTreeNode nodeByPath = root.getOrCreateNodeByPath(path);

		// Get that node's parent, remove the child.
		if (nodeByPath.getParent() instanceof WorkspaceTreeNode parentNode)
			return parentNode.removeSourceChild(nodeByPath);

		// No known node by path.
		return false;
	}

	/**
	 * Gets or creates a tree node by the given {@link PathNode}.
	 *
	 * @param path
	 * 		Path associated with node to look for in tree.
	 *
	 * @return Node containing the path in the tree.
	 */
	@Nonnull
	public WorkspaceTreeNode getOrCreateNodeByPath(@Nonnull PathNode<?> path) {
		// Call from root node only.
		WorkspaceTreeNode root = this;
		while (root.getParent() instanceof WorkspaceTreeNode parentNode)
			root = parentNode;

		// Lookup and/or create nodes for path.
		return path.getOrInsertIntoTree(root);
	}

	/**
	 * @param path
	 * 		Path associated with node to look for in tree.
	 *
	 * @return Node containing the path in the tree.
	 */
	@Nullable
	public WorkspaceTreeNode getNodeByPath(@Nonnull PathNode<?> path) {
		PathNode<?> value = getValue();
		if (path.equals(value))
			return this;

		for (TreeItem<PathNode<?>> child : getChildren())
			if (path.isDescendantOf(child.getValue()) && child instanceof WorkspaceTreeNode childNode)
				return childNode.getNodeByPath(path);

		return null;
	}

	/**
	 * @param path
	 * 		Path to check against.
	 *
	 * @return {@code true} when the current node's path matches.
	 */
	public boolean matches(@Nonnull PathNode<?> path) {
		return path.equals(getValue());
	}

	/**
	 * @return {@link #getParent()} but cast to {@link WorkspaceTreeNode}.
	 */
	@Nullable
	public WorkspaceTreeNode getParentNode() {
		return (WorkspaceTreeNode) getParent();
	}

	@Override
	public int compareTo(@Nonnull WorkspaceTreeNode o) {
		return getValue().compareTo(o.getValue());
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + getValue().toString() + "]";
	}
}
