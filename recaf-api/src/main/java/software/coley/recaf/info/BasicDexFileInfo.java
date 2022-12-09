package software.coley.recaf.info;

import software.coley.recaf.info.builder.FileInfoBuilder;

/**
 * Basic implementation of an Android DEX file info.
 *
 * @author Matt Coley
 */
public class BasicDexFileInfo extends BasicFileInfo implements DexFileInfo {
	/**
	 * @param builder
	 * 		Builder to pull information from.
	 */
	public BasicDexFileInfo(FileInfoBuilder<?> builder) {
		super(builder);
	}
}
