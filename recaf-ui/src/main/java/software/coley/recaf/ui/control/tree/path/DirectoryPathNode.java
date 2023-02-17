package software.coley.recaf.ui.control.tree.path;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import javafx.scene.control.TreeItem;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.FileInfo;
import software.coley.recaf.ui.control.tree.WorkspaceTreeNode;
import software.coley.recaf.workspace.model.bundle.Bundle;

/**
 * Path node for packages of {@link ClassInfo} and directories of {@link FileInfo} types.
 *
 * @author Matt Coley
 */
@SuppressWarnings("rawtypes")
public class DirectoryPathNode extends AbstractPathNode<Bundle, String> {
	/**
	 * Node without parent.
	 *
	 * @param directory
	 * 		Directory name.
	 */
	public DirectoryPathNode(@Nonnull String directory) {
		this(null, directory);
	}

	/**
	 * Node with parent.
	 *
	 * @param parent
	 * 		Parent node.
	 * @param directory
	 * 		Directory name.
	 *
	 * @see BundlePathNode#child(String)
	 */
	public DirectoryPathNode(@Nullable BundlePathNode parent, @Nonnull String directory) {
		super(parent, String.class, directory);
	}

	/**
	 * @param directory
	 * 		New directory name.
	 *
	 * @return New node with same parent, but different directory name value.
	 */
	@Nonnull
	public DirectoryPathNode withDirectory(@Nonnull String directory) {
		return new DirectoryPathNode(getParent(), directory);
	}

	/**
	 * @param classInfo
	 * 		Class to wrap into node.
	 *
	 * @return Path node of class, with current package as parent.
	 */
	@Nonnull
	public ClassPathNode child(@Nonnull ClassInfo classInfo) {
		return new ClassPathNode(this, classInfo);
	}

	/**
	 * @param fileInfo
	 * 		File to wrap into node.
	 *
	 * @return Path node of file, with current directory as parent.
	 */
	@Nonnull
	public FilePathNode child(@Nonnull FileInfo fileInfo) {
		return new FilePathNode(this, fileInfo);
	}

	@Override
	@SuppressWarnings("all")
	public BundlePathNode getParent() {
		return (BundlePathNode) super.getParent();
	}

	@Override
	public int localCompare(PathNode<?> o) {
		if (o instanceof DirectoryPathNode classNode) {
			String name = getValue();
			String otherName = classNode.getValue();
			return String.CASE_INSENSITIVE_ORDER.compare(name, otherName);
		}
		return 0;
	}

	@Nonnull
	@Override
	public WorkspaceTreeNode getOrInsertIntoTree(WorkspaceTreeNode node) {
		// If we have parent links in our path, insert those first.
		// We should generate up to whatever context our parent is.
		BundlePathNode parent = getParent();
		if (parent != null)
			node = parent.getOrInsertIntoTree(node);

		// Work off of the first node that does NOT contain a directory value.
		while (node.getValue() instanceof DirectoryPathNode) {
			node = (WorkspaceTreeNode) node.getParent();
		}

		// Insert the directory path, separated by '/'.
		// Update 'node' as we build/fetch the directory path items.
		String fullDirectory = getValue();
		String[] directoryParts = fullDirectory.split("/");
		StringBuilder directoryBuilder = new StringBuilder();
		for (String directoryPart : directoryParts) {
			// Build up directory path.
			directoryBuilder.append(directoryPart).append('/');
			String directoryName = directoryBuilder.substring(0, directoryBuilder.length() - 1);
			DirectoryPathNode localPathNode = withDirectory(directoryName);

			// Get existing tree node, or create child if non-existant
			WorkspaceTreeNode childNode = null;
			for (TreeItem<PathNode<?>> child : node.getChildren())
				if (child.getValue().equals(localPathNode)) {
					childNode = (WorkspaceTreeNode) child;
					break;
				}
			if (childNode == null) {
				childNode = new WorkspaceTreeNode(localPathNode);
				node.addAndSortChild(childNode);
			}

			// Prepare for next directory path entry.
			node = childNode;
		}
		return node;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DirectoryPathNode node = (DirectoryPathNode) o;

		return getValue().equals(node.getValue());
	}

	@Override
	public int hashCode() {
		return getValue().hashCode();
	}
}
