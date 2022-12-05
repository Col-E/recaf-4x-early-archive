package software.coley.recaf.info.annotation;

import org.objectweb.asm.TypePath;

/**
 * Basic implementation of type annotation info.
 *
 * @author Matt Coley
 */
public class BasicTypeAnnotationInfo extends BasicAnnotationInfo implements TypeAnnotationInfo {
	private final int typeRef;
	private final TypePath typePath;

	/**
	 * @param typeRef
	 * 		Constant denoting where the annotation is applied.
	 * @param typePath
	 * 		Path to a type argument.
	 * @param visible
	 * 		Annotation runtime visibility.
	 * @param descriptor
	 * 		Annotation descriptor.
	 */
	public BasicTypeAnnotationInfo(int typeRef, TypePath typePath,
								   boolean visible, String descriptor) {
		super(visible, descriptor);
		this.typeRef = typeRef;
		this.typePath = typePath;
	}

	@Override
	public int getTypeRef() {
		return typeRef;
	}

	@Override
	public TypePath getTypePath() {
		return typePath;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		BasicTypeAnnotationInfo that = (BasicTypeAnnotationInfo) o;

		if (typeRef != that.typeRef) return false;
		return typePath.equals(that.typePath);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + typeRef;
		result = 31 * result + typePath.hashCode();
		return result;
	}
}
