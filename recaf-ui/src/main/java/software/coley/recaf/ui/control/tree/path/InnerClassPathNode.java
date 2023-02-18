package software.coley.recaf.ui.control.tree.path;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.InnerClassInfo;

/**
 * Path node for {@link InnerClassInfo} types.
 *
 * @author Matt Coley
 */
public class InnerClassPathNode extends AbstractPathNode<ClassInfo, InnerClassInfo> {
	/**
	 * Node with parent.
	 *
	 * @param parent
	 * 		Optional parent node.
	 * @param innerClass
	 * 		Inner class instance.
	 *
	 * @see ClassPathNode#child(InnerClassInfo)
	 */
	public InnerClassPathNode(@Nullable ClassPathNode parent,
							  @Nonnull InnerClassInfo innerClass) {
		super(parent, InnerClassInfo.class, innerClass);
	}

	@Override
	public ClassPathNode getParent() {
		return (ClassPathNode) super.getParent();
	}

	@Override
	public int localCompare(PathNode<?> o) {
		if (o instanceof InnerClassPathNode innerClassPathNode) {
			String name = getValue().getInnerClassName();
			String otherName = innerClassPathNode.getValue().getInnerClassName();
			return String.CASE_INSENSITIVE_ORDER.compare(name, otherName);
		}
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		InnerClassPathNode node = (InnerClassPathNode) o;

		return getValue().equals(node.getValue());
	}

	@Override
	public int hashCode() {
		return getValue().hashCode();
	}
}
