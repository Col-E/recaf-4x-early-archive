package software.coley.recaf.info;

import software.coley.recaf.info.builder.AndroidClassInfoBuilder;

/**
 * Basic Android class info implementation.
 *
 * @author Matt Coley
 */
public class BasicAndroidClassInfo extends BasicClassInfo implements AndroidClassInfo {
	/**
	 * @param builder
	 * 		Builder to pull info from.
	 */
	public BasicAndroidClassInfo(AndroidClassInfoBuilder builder) {
		super(builder);
	}
}
