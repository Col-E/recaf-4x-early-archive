package software.coley.recaf.services.search.result;

import software.coley.recaf.info.TextFileInfo;

/**
 * Outline of a location for an item matched within a {@link TextFileInfo}.
 *
 * @author Matt Coley
 */
public interface TextFileLocation extends FileLocation {
	/**
	 * @return The line number of the matched text.
	 */
	int getLineNumber();
}
