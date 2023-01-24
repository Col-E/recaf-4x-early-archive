package software.coley.recaf.ui.config;

import software.coley.recaf.config.ConfigValue;

/**
 * Factory for a specific {@link ConfigValue} by its {@link ConfigValue#getId()}.
 *
 * @param <T>
 * 		Value type of {@link ConfigValue} to create a component for.
 *
 * @author Matt Coley
 */
public abstract class KeyedConfigComponentFactory<T> extends ConfigComponentFactory<T> {
	private final String id;

	/**
	 * @param createLabel
	 * 		See {@link #isStandAlone()}. Determines if label is automatically added.
	 * @param id
	 * 		Value of a {@link ConfigValue#getId()} to associate with.
	 */
	protected KeyedConfigComponentFactory(boolean createLabel, String id) {
		super(createLabel);
		this.id = id;
	}

	/**
	 * @return The {@link ConfigValue#getId()} this factory is for.
	 */
	public String getId() {
		return id;
	}
}
