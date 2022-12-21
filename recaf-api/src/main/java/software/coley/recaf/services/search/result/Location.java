package software.coley.recaf.services.search.result;

import jakarta.annotation.Nonnull;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

/**
 * Outline of location information of a parent {@link Result}.
 *
 * @author Matt Coley
 */
public interface Location extends Comparable<Location> {
	/**
	 * @return The workspace containing the item matched.
	 */
	@Nonnull
	Workspace getContainingWorkspace();

	/**
	 * @return The resource containing the item matched.
	 */
	@Nonnull
	WorkspaceResource getContainingResource();

	/**
	 * @return String format of the location path, allowing items to be sorted.
	 */
	@Nonnull
	String comparableString();

	@Override
	default int compareTo(Location other) {
		return comparableString().compareTo(other.comparableString());
	}
}