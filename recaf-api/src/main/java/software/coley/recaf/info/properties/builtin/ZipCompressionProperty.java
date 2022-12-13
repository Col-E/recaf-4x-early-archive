package software.coley.recaf.info.properties.builtin;

import jakarta.annotation.Nullable;
import software.coley.llzip.ZipCompressions;
import software.coley.recaf.info.Info;
import software.coley.recaf.info.properties.BasicProperty;
import software.coley.recaf.info.properties.Property;

/**
 * Built in property to track the original zip compression method used for an {@link Info}
 * value stored inside a ZIP container.
 *
 * @author Matt Coley
 */
public class ZipCompressionProperty extends BasicProperty<Integer> {
	public static final String KEY = "zip-compression";

	/**
	 * @param value
	 * 		Compression type. See {@link ZipCompressions} for values.
	 */
	public ZipCompressionProperty(int value) {
		super(KEY, value);
	}

	/**
	 * @param info
	 * 		Info instance.
	 *
	 * @return Compression type. See {@link ZipCompressions} for values.
	 * {@code null} when no property value is assigned.
	 */
	@Nullable
	public static Integer get(Info info) {
		Property<Integer> property = info.getProperty(KEY);
		if (property != null) {
			return property.value();
		}
		return null;
	}

	/**
	 * @param info
	 * 		Info instance.
	 * @param value
	 * 		Compression type. See {@link ZipCompressions} for values.
	 */
	public static void set(Info info, int value) {
		info.setProperty(new ZipCompressionProperty(value));
	}

	@Override
	public boolean persistent() {
		return true;
	}
}
