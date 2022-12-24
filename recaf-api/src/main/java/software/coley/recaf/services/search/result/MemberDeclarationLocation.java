package software.coley.recaf.services.search.result;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
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
		return new BasicInstructionLocation(instruction, this);
	}

	/**
	 * @param variable
	 * 		Local variable to add.
	 *
	 * @return New location for the variable within this member <i>(method)</i> declaration.
	 */
	default LocalVariableLocation withLocalVariable(LocalVariableNode variable) {
		return new BasicLocalVariableLocation(variable, this);
	}

	/**
	 * @param catchBlock
	 * 		Try-catch block to add.
	 *
	 * @return New location for the catch block within this member <i>(method)</i> declaration.
	 */
	default CatchBlockLocation withCatchBlock(TryCatchBlockNode catchBlock) {
		return new BasicCatchBlockLocation(catchBlock, this);
	}

	/**
	 * @param thrownException
	 * 		Thrown exception type.
	 *
	 * @return New location for the thrown exception on this member <i>(method)</i> declaration.
	 */
	default ThrowsLocation withThrownException(String thrownException) {
		return new BasicThrowsLocation(thrownException, this);
	}
}
