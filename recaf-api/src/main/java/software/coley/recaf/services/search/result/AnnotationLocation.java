package software.coley.recaf.services.search.result;

import jakarta.annotation.Nonnull;
import software.coley.recaf.info.annotation.AnnotationInfo;

/**
 * Outline of a location for an item matched in/on a {@link AnnotationInfo}.
 *
 * @author Matt Coley
 */
public interface AnnotationLocation extends NestedLocation, AnnotatableLocation {
	/**
	 * @return Member the result resides within.
	 */
	@Nonnull
	AnnotationInfo getDeclaredAnnotation();
}
