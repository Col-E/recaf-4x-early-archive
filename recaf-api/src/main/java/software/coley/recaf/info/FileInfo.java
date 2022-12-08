package software.coley.recaf.info;

/**
 * Outline of a file.
 *
 * @author Matt Coley
 */
public interface FileInfo extends Info {
	/**
	 * @return Raw bytes of file content.
	 */
	byte[] getRawContent();
}
