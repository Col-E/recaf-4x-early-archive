package software.coley.recaf.path;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.recaf.info.annotation.Annotated;
import software.coley.recaf.info.annotation.AnnotationInfo;

/**
 * Path node for annotations on {@link Annotated} types such as classes, fields, and methods..
 *
 * @author Matt Coley
 */
public class AnnotationPathNode extends AbstractPathNode<Object, AnnotationInfo> {
	/**
	 * Node without parent.
	 *
	 * @param annotation
	 * 		Annotation.
	 */
	public AnnotationPathNode(@Nonnull AnnotationInfo annotation) {
		this(null, annotation);
	}

	/**
	 * Node with parent.
	 *
	 * @param parent
	 * 		Parent node.
	 * @param annotation
	 * 		Annotation.
	 *
	 * @see ClassMemberPathNode#childAnnotation(AnnotationInfo)
	 * @see ClassPathNode#child(AnnotationInfo)
	 */
	@SuppressWarnings("unchecked")
	public AnnotationPathNode(@Nullable PathNode<?> parent, @Nonnull AnnotationInfo annotation) {
		super("annotation", (PathNode<Object>) parent, AnnotationInfo.class, annotation);
	}

	@Override
	public int localCompare(PathNode<?> o) {
		if (o instanceof AnnotationPathNode node) {
			return getValue().getDescriptor().compareTo(node.getValue().getDescriptor());
		}
		return 0;
	}
}
