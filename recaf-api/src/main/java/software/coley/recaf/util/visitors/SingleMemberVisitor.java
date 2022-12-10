package software.coley.recaf.util.visitors;

import org.objectweb.asm.*;
import software.coley.recaf.RecafConstants;
import software.coley.recaf.info.member.ClassMember;

/**
 * A visitor that visits only the targeted member. Everything else is stripped.
 *
 * @author Matt Coley
 */
public class SingleMemberVisitor extends ClassVisitor {
	private final ClassMember member;
	private boolean finished;

	/**
	 * @param cv
	 * 		Parent visitor.
	 * @param member
	 * 		Target member to visit.
	 */
	public SingleMemberVisitor(ClassVisitor cv, ClassMember member) {
		super(RecafConstants.getAsmVersion(), cv);
		this.member = member;
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String sig, Object value) {
		// Only visit if matching the target info and searching
		if (finished)
			return null;
		if (member.isField() && member.getName().equals(name) && member.getDescriptor().equals(desc)) {
			finished = true;
			return super.visitField(access, name, desc, sig, value);
		}
		return null;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] exceptions) {
		// Only visit if matching the target info and searching
		if (finished)
			return null;
		if (member.isMethod() && member.getName().equals(name) && member.getDescriptor().equals(desc)) {
			finished = true;
			return super.visitMethod(access, name, desc, sig, exceptions);
		}
		return null;
	}

	@Override
	public ModuleVisitor visitModule(String name, int access, String version) {
		// Skip
		return null;
	}

	@Override
	public void visitNestHost(String nestHost) {
		// Skip
	}

	@Override
	public void visitOuterClass(String owner, String name, String desc) {
		// Skip
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		// Skip
		return null;
	}

	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
		// Skip
		return null;
	}

	@Override
	public void visitAttribute(Attribute attribute) {
		// Skip
	}

	@Override
	public void visitNestMember(String nestMember) {
		// Skip
	}

	@Override
	public void visitPermittedSubclass(String permittedSubclass) {
		// Skip
	}

	@Override
	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		// Skip
	}

	@Override
	public RecordComponentVisitor visitRecordComponent(String name, String desc, String sig) {
		// Skip
		return null;
	}
}
