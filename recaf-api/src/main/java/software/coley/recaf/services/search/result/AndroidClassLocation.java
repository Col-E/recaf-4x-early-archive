package software.coley.recaf.services.search.result;

import software.coley.recaf.info.AndroidClassInfo;
import software.coley.recaf.info.member.ClassMember;
import software.coley.recaf.workspace.model.bundle.AndroidClassBundle;

/**
 * Outline of a location for an item matched within a {@link AndroidClassInfo}.
 *
 * @author Matt Coley
 */
public interface AndroidClassLocation extends AnnotatableLocation, MemberDeclaringLocation {
	/**
	 * @return The bundle <i>(Within {@link #getContainingResource()})</i> containing the item matched.
	 */
	AndroidClassBundle getContainingBundle();

	/**
	 * @return The class containing the item matched.
	 */
	AndroidClassInfo getClassInfo();
}
