package software.coley.recaf.ui.control.tree.path;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.member.ClassMember;

/**
 * Path node for {@link ClassInfo} types.
 *
 * @author Matt Coley
 */
public class ClassPathNode extends AbstractPathNode<String, ClassInfo> {
	/**
	 * Node without parent.
	 *
	 * @param info
	 * 		Class value.
	 */
	public ClassPathNode(@Nonnull ClassInfo info) {
		this(null, info);
	}

	/**
	 * Node with parent.
	 *
	 * @param parent
	 * 		Parent node.
	 * @param info
	 * 		Class value.
	 *
	 * @see DirectoryPathNode#child(ClassInfo)
	 */
	public ClassPathNode(@Nullable DirectoryPathNode parent, @Nonnull ClassInfo info) {
		super(parent, ClassInfo.class, info);
	}

	/**
	 * @param member
	 * 		Member to wrap into node.
	 *
	 * @return Path node of member, with current class as parent.
	 */
	@Nonnull
	public ClassMemberPathNode child(@Nonnull ClassMember member) {
		return new ClassMemberPathNode(this, member);
	}

	@Override
	public DirectoryPathNode getParent() {
		return (DirectoryPathNode) super.getParent();
	}

	@Override
	public int localCompare(PathNode<?> o) {
		if (o instanceof ClassPathNode classPathNode) {
			String name = getValue().getName();
			String otherName = classPathNode.getValue().getName();
			return String.CASE_INSENSITIVE_ORDER.compare(name, otherName);
		}
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ClassPathNode node = (ClassPathNode) o;

		return getValue().equals(node.getValue());
	}

	@Override
	public int hashCode() {
		return getValue().hashCode();
	}
}