package software.coley.recaf.ui.control.tree.path;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.recaf.info.FileInfo;

/**
 * Path node for {@link FileInfo} types.
 *
 * @author Matt Coley
 */
public class FilePathNode extends AbstractPathNode<String, FileInfo> {
	/**
	 * Node without parent.
	 *
	 * @param info
	 * 		File value.
	 */
	public FilePathNode(@Nonnull FileInfo info) {
		this(null, info);
	}

	/**
	 * Node with parent.
	 *
	 * @param parent
	 * 		Parent node.
	 * @param info
	 * 		File value.
	 *
	 * @see DirectoryPathNode#child(FileInfo)
	 */
	public FilePathNode(@Nullable DirectoryPathNode parent, @Nonnull FileInfo info) {
		super(parent, FileInfo.class, info);
	}

	@Override
	public DirectoryPathNode getParent() {
		return (DirectoryPathNode) super.getParent();
	}

	@Override
	public int localCompare(PathNode<?> o) {
		if (o instanceof FilePathNode fileNode) {
			String name = getValue().getName();
			String otherName = fileNode.getValue().getName();
			return String.CASE_INSENSITIVE_ORDER.compare(name, otherName);
		}
		return 0;
	}

	@Override
	public int compareTo(@Nonnull PathNode<?> o) {
		int cmp = cmpParent(o);
		if (cmp == 0) return localCompare(o);
		return cmpHierarchy(o);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		FilePathNode node = (FilePathNode) o;

		return getValue().equals(node.getValue());
	}

	@Override
	public int hashCode() {
		return getValue().hashCode();
	}
}
