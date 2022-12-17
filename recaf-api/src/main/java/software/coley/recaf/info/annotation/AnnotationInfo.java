package software.coley.recaf.info.annotation;


import java.util.Map;

/**
 * Outline of annotation data.
 *
 * @author Matt Coley
 */
public interface AnnotationInfo {
	/**
	 * @return {@code true} if the annotation is visible at runtime.
	 */
	boolean isVisible();

	/**
	 * @return Annotation descriptor.
	 */
	String getDescriptor();

	/**
	 * @return Annotation elements.
	 */
	Map<String, AnnotationElement> getElements();
}
