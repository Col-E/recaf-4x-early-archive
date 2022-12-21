package software.coley.recaf.services.search.result;

import software.coley.recaf.info.annotation.AnnotationInfo;

/**
 * Outline of a location that can have annotations declared on it, such as a class or member.
 *
 * @author Matt Coley
 */
public interface AnnotatableLocation extends Location {
	/**
	 * @param annotationInfo
	 * 		Annotation to target.
	 *
	 * @return New location for the annotation with the
	 * {@link AnnotationLocation#getParent()} being the current location.
	 */
	default AnnotationLocation withAnnotation(AnnotationInfo annotationInfo) {
		return new BasicAnnotationLocation(getContainingWorkspace(), getContainingResource(), this, annotationInfo);
	}
}
