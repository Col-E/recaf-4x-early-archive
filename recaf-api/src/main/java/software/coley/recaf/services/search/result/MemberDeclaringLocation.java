package software.coley.recaf.services.search.result;

import software.coley.recaf.info.member.ClassMember;

/**
 * Outline for locations that can contain sub-locations of {@link ClassMember}.
 *
 * @author Matt Coley
 * @see AndroidClassLocation
 * @see JvmClassLocation
 */
public interface MemberDeclaringLocation extends Location {
	/**
	 * @param member
	 * 		Member to target.
	 *
	 * @return New location for the member with the
	 * {@link MemberDeclarationLocation#getParent()} being the current location.
	 */
	default MemberDeclarationLocation withMember(ClassMember member) {
		return new BasicMemberDeclarationLocation(getContainingWorkspace(), getContainingResource(), this, member);
	}
}
