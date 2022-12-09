package software.coley.recaf.info.properties.builtin;

import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.Info;
import software.coley.recaf.info.properties.BasicProperty;

/**
 * Built in property to track the suffix of an {@link Info} type, primarily the extension of {@link ClassInfo} files.
 * In most cases the value will be {@code .class}.
 *
 * @author Matt Coley
 * @see PathPrefixProperty
 * @see PathOriginalNameProperty
 */
public class PathSuffixProperty extends BasicProperty<String> {
	public static final String KEY = "path-suffix";

	/**
	 * @param value
	 * 		Suffix.
	 */
	public PathSuffixProperty(String value) {
		super(KEY, value);
	}

	/**
	 * @param info
	 * 		Info instance.
	 *
	 * @return Name of the info, with the suffix applied if any exist.
	 */
	public static String map(Info info) {
		String name = info.getName();
		String suffix = info.getPropertyValueOrNull(KEY);
		if (suffix != null)
			return name + suffix;
		return name;
	}

	/**
	 * @param info
	 * 		Info instance.
	 * @param suffix
	 * 		Suffix to associate with the item.
	 */
	public static void set(Info info, String suffix) {
		info.setProperty(KEY, new PathSuffixProperty(suffix));
	}

	@Override
	public boolean persistent() {
		return true;
	}
}
