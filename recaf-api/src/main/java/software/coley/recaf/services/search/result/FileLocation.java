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

	/**
	 * @param lineNumber
	 * 		Line number to add.
	 *
	 * @return New location for the line number in the text.
	 */
	default TextFileLocation withTextLineNumber(int lineNumber) {
		return new BasicTextFileLocation(getContainingWorkspace(), getContainingResource(),
				getContainingBundle(), getFileInfo(), lineNumber);
	}
}
