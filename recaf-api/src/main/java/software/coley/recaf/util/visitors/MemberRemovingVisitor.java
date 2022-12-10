package software.coley.recaf.util.visitors;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import software.coley.recaf.RecafConstants;
import software.coley.recaf.info.member.ClassMember;

/**
 * Simple visitor for removing a matched {@link ClassMember}.
 *
 * @author Matt Coley
 */
public class MemberRemovingVisitor extends ClassVisitor {
	private final ClassMember member;
	private boolean removed;

	/**
	 * @param cv
	 * 		Parent visitor where the removal will be applied in.
	 * @param member
	 * 		Member to remove.
	 */
	public MemberRemovingVisitor(ClassVisitor cv, ClassMember member) {
		super(RecafConstants.getAsmVersion(), cv);
		this.member = member;
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String sig, Object value) {
		if (member.isField() && member.getName().equals(name) && member.getDescriptor().equals(desc)) {
			removed = true;
			return null;
		}
		return super.visitField(access, name, desc, sig, value);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] exceptions) {
		if (member.isMethod() && member.getName().equals(name) && member.getDescriptor().equals(desc)) {
			removed = true;
			return null;
		}
		return super.visitMethod(access, name, desc, sig, exceptions);
	}

	/**
	 * @return {@code true} when a field or method was removed.
	 */
	public boolean isRemoved() {
		return removed;
	}
}
