package software.coley.recaf.info.properties.builtin;

import software.coley.recaf.info.Info;
import software.coley.recaf.info.properties.BasicProperty;

/**
 * Built in property to
 *
 * @author Matt Coley
 */
public class OriginalPathProperty extends BasicProperty<OriginalPathProperty.Value> {
	public static final String KEY = "original-path-name";

	private OriginalPathProperty(Value value) {
		super(KEY, value);
	}

	/**
	 * @param info
	 * 		Info instance.
	 *
	 * @return Name of info type, with updated format to include the prefix or original name of the item
	 * if it has a {@link OriginalPathProperty} value set. Otherwise, returns the {@link Info#getName()}.
	 */
	public static String mapName(Info info) {
		String name = info.getName();
		Value value = info.getPropertyValueOrNull(KEY);
		if (value != null)
			return value.map(name);
		return name;
	}

	/**
	 * @param info
	 * 		Info instance.
	 * @param prefix
	 * 		Prefix to associate with the item.
	 */
	public static void setPrefix(Info info, String prefix) {
		info.setProperty(KEY, new OriginalPathProperty(new Value(prefix, null)));
	}

	/**
	 * @param info
	 * 		Info instance.
	 * @param originalName
	 * 		Full name replacement to associate with the item.
	 */
	public static void setOriginalName(Info info, String originalName) {
		info.setProperty(KEY, new OriginalPathProperty(new Value(null, originalName)));
	}

	@Override
	public boolean persistent() {
		return true;
	}

	public static class Value {
		private final String prefix;
		private final String original;

		private Value(String prefix, String original) {
			this.prefix = prefix;
			this.original = original;
		}

		public boolean isPrefixed() {
			return prefix != null;
		}

		public String map(String name) {
			if (isPrefixed())
				return prefix + name;
			else
				return original;
		}
	}
}
