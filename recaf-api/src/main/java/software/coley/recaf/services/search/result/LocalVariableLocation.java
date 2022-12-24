package software.coley.recaf.services.search.result;

import org.objectweb.asm.tree.LocalVariableNode;
import software.coley.recaf.info.member.MethodMember;

/**
 * Outline of a location for an item matched on a local variable.
 *
 * @author Matt Coley
 */
public interface LocalVariableLocation extends NestedLocation {
	/**
	 * @return Parent location for the {@link MethodMember}.
	 */
	default MemberDeclarationLocation getDeclaringMethod() {
		return (MemberDeclarationLocation) getParent();
	}

	/**
	 * @return The variable.
	 */
	LocalVariableNode getVariable();
}
