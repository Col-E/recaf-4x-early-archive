package software.coley.recaf.info;

import software.coley.recaf.info.builder.FileInfoBuilder;

/**
 * Basic implementation of ZIP file info.
 *
 * @author Matt Coley
 */
public class BasicZipFileInfo extends BasicFileInfo implements ZipFileInfo {
	/**
	 * @param builder
	 * 		Builder to pull information from.
	 */
	public BasicZipFileInfo(FileInfoBuilder<?> builder) {
		super(builder);
	}
}
