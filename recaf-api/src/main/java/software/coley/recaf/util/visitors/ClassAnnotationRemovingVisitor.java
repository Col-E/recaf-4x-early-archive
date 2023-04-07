package software.coley.recaf.util.visitors;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.tree.AnnotationNode;
import software.coley.recaf.RecafConstants;

/**
 * Simple visitor for inserting a annotation.
 *
 * @author Matt Coley
 */
public class ClassAnnotationRemovingVisitor extends ClassVisitor {
	private final String annotationType;

	/**
	 * @param cv
	 * 		Parent visitor.
	 * @param annotationType
	 * 		Annotation type to remove.
	 */
	public ClassAnnotationRemovingVisitor(@Nullable ClassVisitor cv,
										  @Nonnull String annotationType) {
		super(RecafConstants.getAsmVersion(), cv);
		this.annotationType = annotationType;
	}

	@Override
	public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
		if (annotationType.equals(descriptor.substring(1, descriptor.length() - 1)))
			return null;
		return super.visitAnnotation(descriptor, visible);
	}

	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
		if (annotationType.equals(descriptor.substring(1, descriptor.length() - 1)))
			return null;
		return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
	}
}