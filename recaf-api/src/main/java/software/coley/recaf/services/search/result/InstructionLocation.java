package software.coley.recaf.services.search.result;

import org.objectweb.asm.tree.AbstractInsnNode;
import software.coley.recaf.info.member.MethodMember;

/**
 * Outline of a location for an item matched on an instruction within a {@link MethodMember}.
 *
 * @author Matt Coley
 */
public interface InstructionLocation extends NestedLocation {
	/**
	 * @return Parent location for the {@link MethodMember}.
	 */
	default MemberDeclarationLocation getDeclaringMethod() {
		return (MemberDeclarationLocation) getParent();
	}

	/**
	 * @return Instruction value.
	 */
	AbstractInsnNode getInstruction();
}
