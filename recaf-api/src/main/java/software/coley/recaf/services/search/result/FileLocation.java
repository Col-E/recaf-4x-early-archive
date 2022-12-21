package software.coley.recaf.services.search.result;

import software.coley.recaf.info.FileInfo;
import software.coley.recaf.workspace.model.bundle.FileBundle;

/**
 * Outline of a location for an item matched within a {@link FileInfo}.
 *
 * @author Matt Coley
 */
public interface FileLocation extends Location {
	/**
	 * @return The bundle <i>(Within {@link #getContainingResource()})</i> containing the item matched.
	 */
	FileBundle getContainingBundle();

	/**
	 * @return The file containing the item matched.
	 */
	FileInfo getFileInfo();
}
