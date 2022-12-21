package software.coley.recaf.services.search.result;

import software.coley.recaf.info.member.ClassMember;

/**
 * Outline of a location for an item matched in/on a {@link ClassMember}.
 *
 * @author Matt Coley
 */
public interface MemberDeclarationLocation extends NestedLocation, AnnotatableLocation {
	/**
	 * @return Member the result resides within.
	 */
	ClassMember getDeclaredMember();

	/**
	 * @return Parent location of {@link AndroidClassLocation} or {@link JvmClassLocation}.
	 */
	default Location getDeclaringClassLocation() {
		return getParent();
	}
}
