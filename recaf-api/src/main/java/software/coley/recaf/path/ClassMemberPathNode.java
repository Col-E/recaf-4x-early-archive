package software.coley.recaf.path;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.member.ClassMember;

import java.util.List;

/**
 * Path node for {@link ClassMember} types.
 *
 * @author Matt Coley
 */
public class ClassMemberPathNode extends AbstractPathNode<ClassInfo, ClassMember> {
	/**
	 * Node without parent.
	 *
	 * @param member
	 * 		Member value.
	 */
	public ClassMemberPathNode(@Nonnull ClassMember member) {
		this(null, member);
	}

	/**
	 * Node with parent.
	 *
	 * @param parent
	 * 		Parent node.
	 * @param member
	 * 		Member value.
	 *
	 * @see ClassPathNode#child(ClassMember)
	 */
	public ClassMemberPathNode(@Nullable ClassPathNode parent, @Nonnull ClassMember member) {
		super(parent, ClassMember.class, member);
	}

	@Override
	public ClassPathNode getParent() {
		return (ClassPathNode) super.getParent();
	}

	@Override
	public int localCompare(PathNode<?> o) {
		if (o instanceof ClassMemberPathNode classMemberNode) {
			ClassMember member = getValue();
			ClassMember otherMember = classMemberNode.getValue();

			// Show fields first
			if (member.isField() && otherMember.isMethod()) {
				return -1;
			} else if (member.isMethod() && otherMember.isField()) {
				return 1;
			}

			ClassPathNode parent = getParent();
			if (parent != null) {
				// Sort by appearance order in parent.
				ClassInfo classInfo = parent.getValue();
				List<? extends ClassMember> list = member.isField() ?
						classInfo.getFields() : classInfo.getMethods();
				return Integer.compare(list.indexOf(member), list.indexOf(otherMember));
			} else {
				// Just sort alphabetically if parent not known.
				String key = member.getName() + member.getDescriptor();
				String otherKey = otherMember.getName() + member.getDescriptor();
				return String.CASE_INSENSITIVE_ORDER.compare(key, otherKey);
			}
		}
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ClassMemberPathNode node = (ClassMemberPathNode) o;

		return getValue().equals(node.getValue());
	}

	@Override
	public int hashCode() {
		return getValue().hashCode();
	}
}
