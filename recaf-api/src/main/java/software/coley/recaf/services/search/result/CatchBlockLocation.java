package software.coley.recaf.services.search.result;

import org.objectweb.asm.tree.TryCatchBlockNode;
import software.coley.recaf.info.member.MethodMember;

/**
 * Outline of a location for an item matched on a catch block.
 *
 * @author Matt Coley
 */
public interface CatchBlockLocation extends NestedLocation {
	/**
	 * @return Parent location for the {@link MethodMember}.
	 */
	default MemberDeclarationLocation getDeclaringMethod() {
		return (MemberDeclarationLocation) getParent();
	}

	/**
	 * @return The catch block.
	 */
	TryCatchBlockNode getCatchBlock();
}
