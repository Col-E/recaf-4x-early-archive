package software.coley.recaf.services.search.result;

import jakarta.annotation.Nonnull;

/**
 * Outline of a nested location.
 *
 * @author Matt Coley
 * @see MemberDeclarationLocation
 * @see AnnotationLocation
 */
public interface NestedLocation extends Location {
	/**
	 * @return Parent location.
	 */
	@Nonnull
	Location getParent();
}
