package software.coley.recaf.services.search.result;

import software.coley.recaf.info.member.MethodMember;

/**
 * Outline of a location for a thrown exception on a method.
 *
 * @author Matt Coley
 */
public interface ThrowsLocation extends NestedLocation {
	/**
	 * @return Parent location for the {@link MethodMember}.
	 */
	default MemberDeclarationLocation getDeclaringMethod() {
		return (MemberDeclarationLocation) getParent();
	}

	/**
	 * @return The thrown exception type.
	 */
	String getThrownException();
}
