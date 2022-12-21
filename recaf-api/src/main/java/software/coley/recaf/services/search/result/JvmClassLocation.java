package software.coley.recaf.services.search.result;

import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;

/**
 * Outline of a location for an item matched within a {@link JvmClassInfo}.
 *
 * @author Matt Coley
 */
public interface JvmClassLocation extends AnnotatableLocation, MemberDeclaringLocation {
	/**
	 * @return The bundle <i>(Within {@link #getContainingResource()})</i> containing the item matched.
	 */
	JvmClassBundle getContainingBundle();

	/**
	 * @return The class containing the item matched.
	 */
	JvmClassInfo getClassInfo();
}
