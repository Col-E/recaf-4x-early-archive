package software.coley.recaf.info;

import software.coley.recaf.info.builder.FileInfoBuilder;

/**
 * Basic implementation of JAR file info.
 *
 * @author Matt Coley
 */
public class BasicJarFileInfo extends BasicZipFileInfo implements JarFileInfo {
	/**
	 * @param builder
	 * 		Builder to pull information from.
	 */
	public BasicJarFileInfo(FileInfoBuilder<?> builder) {
		super(builder);
	}
}
