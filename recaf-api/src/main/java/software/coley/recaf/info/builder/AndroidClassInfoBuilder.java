package software.coley.recaf.info.builder;

import software.coley.recaf.info.AndroidClassInfo;
import software.coley.recaf.info.BasicAndroidClassInfo;

/**
 * Builder for {@link AndroidClassInfo}.
 *
 * @author Matt Coley
 */
public class AndroidClassInfoBuilder extends AbstractClassInfoBuilder<AndroidClassInfoBuilder> {
	/**
	 * Create empty builder.
	 */
	public AndroidClassInfoBuilder() {
		super();
	}

	/**
	 * Create a builder with data pulled from the given class.
	 *
	 * @param classInfo
	 * 		Class to pull data from.
	 */
	public AndroidClassInfoBuilder(AndroidClassInfo classInfo) {
		super(classInfo);
	}

	@Override
	public AndroidClassInfo build() {
		verify();
		return new BasicAndroidClassInfo(this);
	}
}
