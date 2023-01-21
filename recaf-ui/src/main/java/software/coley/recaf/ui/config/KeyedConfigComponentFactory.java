package software.coley.recaf.ui.config;

import software.coley.recaf.config.ConfigValue;

/**
 * Factory for a specific {@link ConfigValue} by its {@link ConfigValue#getKey()}.
 *
 * @param <T>
 * 		Value type of {@link ConfigValue} to create a component for.
 *
 * @author Matt Coley
 */
public abstract class KeyedConfigComponentFactory<T> extends ConfigComponentFactory<T> {
	private final String key;

	/**
	 * @param createLabel
	 * 		See {@link #isStandAlone()}. Determines if label is automatically added.
	 * @param key
	 * 		Value of a {@link ConfigValue#getKey()} to associate with.
	 */
	protected KeyedConfigComponentFactory(boolean createLabel, String key) {
		super(createLabel);
		this.key = key;
	}

	/**
	 * @return The {@link ConfigValue#getKey()} this factory is for.
	 */
	public String getKey() {
		return key;
	}
}
