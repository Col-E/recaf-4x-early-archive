package software.coley.recaf.services.search.result;

import org.objectweb.asm.tree.AbstractInsnNode;
import software.coley.recaf.info.member.ClassMember;

/**
 * Outline of a location for an item matched in/on a {@link ClassMember}.
 *
 * @author Matt Coley
 */
public interface MemberDeclarationLocation extends NestedLocation, AnnotatableLocation {
	/**
	 * @return Member the result resides within.
	 */
	ClassMember getDeclaredMember();

	/**
	 * @return Parent location of {@link AndroidClassLocation} or {@link JvmClassLocation}.
	 */
	default MemberDeclaringLocation getDeclaringClassLocation() {
		return (MemberDeclaringLocation) getParent();
	}

	/**
	 * @param instruction
	 * 		Instruction to add.
	 *
	 * @return New location for the instruction within this member <i>(method)</i> declaration.
	 */
	default InstructionLocation withInstruction(AbstractInsnNode instruction) {
		return new BasicInstructionValue(instruction, this);
	}
}
