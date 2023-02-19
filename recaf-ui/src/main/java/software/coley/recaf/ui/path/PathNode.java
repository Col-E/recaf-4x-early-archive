package software.coley.recaf.ui.path;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import javafx.scene.control.TreeItem;
import software.coley.recaf.ui.control.tree.WorkspaceTree;
import software.coley.recaf.ui.control.tree.WorkspaceTreeNode;
import software.coley.recaf.workspace.model.Workspace;

/**
 * A <i>"modular"</i> value type for representing <i>"paths"</i> to content in a {@link Workspace}.
 * The path must contain all data in a <i>"chain"</i> such that it can have access from most specific portion
 * all the way up to the {@link Workspace} portion.
 *
 * @param <V>
 * 		Path value type.
 *
 * @author Matt Coley
 * @see WorkspaceTree Usage for representing a complete {@link Workspace}
 * // TODO: Usage for member tree, showing that you don't have to represent a complete workspace with this model.
 */
public interface PathNode<V> extends Comparable<PathNode<?>> {
	/**
	 * The parent node of this node. This value does not have to be present in the actual {@link WorkspaceTreeNode}
	 * model. The parent linkage is so that child types like {@link ClassPathNode} can access their full scope,
	 * including their containing {@link DirectoryPathNode package}, {@link BundlePathNode bundle},
	 * {@link ResourcePathNode resource}, and {@link WorkspacePathNode workspace}.
	 * <br>
	 * This allows child-types such as {@link ClassPathNode} to be passed around to consuming APIs and retain access
	 * to the mentioned scoped values.
	 *
	 * @return Parent node.
	 *
	 * @see #getValueOfType(Class) Used by child-types to look up values in themselves, and their parents.
	 */
	@Nullable
	@SuppressWarnings("rawtypes")
	PathNode getParent();

	/**
	 * @return Wrapped value.
	 */
	@Nonnull
	V getValue();

	/**
	 * @return The type of this path node's {@link #getValue() wrapped value}.
	 */
	@Nonnull
	Class<V> getValueType();

	/**
	 * @param type
	 * 		Some type contained in the full path.
	 * 		This includes the current {@link PathNode} and any {@link #getParent() parent}.
	 * @param <T>
	 * 		Implied value type.
	 *
	 * @return Instance of value from the path, or {@code null} if not found in this path.
	 */
	@Nullable
	<T> T getValueOfType(@Nonnull Class<T> type);

	/**
	 * @param other
	 * 		Some other path node.
	 *
	 * @return {@code true} when our path represents a more specific path than the given one.
	 * {@code false} when our path does not belong to a potential sub-path of the given item.
	 */
	boolean isDescendantOf(@Nonnull PathNode<?> other);

	/**
	 * @param o
	 * 		Some other path node.
	 *
	 * @return Comparison for visual sorting purposes.
	 */
	int localCompare(PathNode<?> o);

	/**
	 * Get/insert a {@link WorkspaceTreeNode} holding this {@link PathNode} from/to the tree model.
	 *
	 * @param node
	 * 		Tree node to insert into.
	 *
	 * @return Inserted node.
	 */
	@Nonnull
	default WorkspaceTreeNode getOrInsertIntoTree(WorkspaceTreeNode node) {
		// If we have parent links in our path, insert those first.
		// We should generate up to whatever context our parent is.
		PathNode<?> parent = getParent();
		if (parent != null)
			node = parent.getOrInsertIntoTree(node);
		else if (getValueType() == node.getValue().getValueType()) {
			// We are the root link in the path. This check ensures that as the root type we do not
			// insert a new tree-node of the same value, to the children list of the root tree node.
			return node;
		}

		// Check if already inserted.
		for (TreeItem<PathNode<?>> child : node.getChildren())
			if (equals(child.getValue()))
				return (WorkspaceTreeNode) child;

		// Not already inserted, create a new node and insert it.
		WorkspaceTreeNode inserted = new WorkspaceTreeNode(this);
		node.addAndSortChild(inserted);
		return inserted;
	}
}
