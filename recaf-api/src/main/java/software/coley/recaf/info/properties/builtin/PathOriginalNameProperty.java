package software.coley.recaf.info.properties.builtin;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.Info;
import software.coley.recaf.info.properties.BasicProperty;

/**
 * Built in property to track the original name of an {@link Info} type, primarily {@link ClassInfo} items.
 *
 * @author Matt Coley
 * @see PathPrefixProperty
 * @see PathSuffixProperty
 */
public class PathOriginalNameProperty extends BasicProperty<String> {
	public static final String KEY = "path-original-full";

	/**
	 * @param value
	 * 		Original path name.
	 */
	public PathOriginalNameProperty(String value) {
		super(KEY, value);
	}

	/**
	 * @param info
	 * 		Info instance.
	 *
	 * @return Original name of the info if set, otherwise the existing info name.
	 */
	@Nonnull
	public static String map(Info info) {
		String name = info.getName();
		String original = info.getPropertyValueOrNull(KEY);
		if (original != null)
			return original + name;
		return name;
	}

	/**
	 * @param info
	 * 		Info instance.
	 *
	 * @return Prefix associated with instance.
	 */
	@Nullable
	public static String get(Info info) {
		return info.getPropertyValueOrNull(KEY);
	}

	/**
	 * @param info
	 * 		Info instance.
	 * @param original
	 * 		Original name to associate with the item.
	 */
	public static void set(Info info, String original) {
		info.setProperty(new PathOriginalNameProperty(original));
	}
}
