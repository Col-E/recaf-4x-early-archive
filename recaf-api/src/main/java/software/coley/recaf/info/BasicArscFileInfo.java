package software.coley.recaf.info;

import software.coley.recaf.info.builder.ArscFileInfoBuilder;

/**
 * Basic implementation of ARSC file info.
 *
 * @author Matt Coley
 */
public class BasicArscFileInfo extends BasicAndroidChunkFileInfo implements ArscFileInfo {
	/**
	 * @param builder
	 * 		Builder to pull information from.
	 */
	public BasicArscFileInfo(ArscFileInfoBuilder builder) {
		super(builder);
	}
}
