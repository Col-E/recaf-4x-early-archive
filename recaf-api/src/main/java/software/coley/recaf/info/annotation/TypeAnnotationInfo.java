package software.coley.recaf.info.annotation;

import org.objectweb.asm.TypePath;

/**
 * Outline of type annotation data.
 *
 * @author Matt Coley
 */
public interface TypeAnnotationInfo extends AnnotationInfo {
	/**
	 * @return Constant denoting where the annotation is applied.
	 */
	int getTypeRef();

	/**
	 * @return Path to a type argument.
	 */
	TypePath getTypePath();
}
