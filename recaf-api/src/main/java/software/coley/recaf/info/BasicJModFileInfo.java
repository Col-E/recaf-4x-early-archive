package software.coley.recaf.info;

import software.coley.recaf.info.builder.FileInfoBuilder;

/**
 * Basic implementation of JMod file info.
 *
 * @author Matt Coley
 */
public class BasicJModFileInfo extends BasicZipFileInfo implements JModFileInfo {
	/**
	 * @param builder
	 * 		Builder to pull information from.
	 */
	public BasicJModFileInfo(FileInfoBuilder<?> builder) {
		super(builder);
	}
}
