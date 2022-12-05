package software.coley.recaf.info.annotation;

/**
 * Outline of an annotation member.
 *
 * @author Matt Coley
 */
public interface AnnotationElement {
	/**
	 * @return Element name.
	 */
	String getElementName();

	/**
	 * @return Element value.
	 */
	Object getElementValue();
}
