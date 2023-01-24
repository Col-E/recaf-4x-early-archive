package software.coley.recaf.config;

import software.coley.observables.AbstractObservable;

/**
 * An option stored in a {@link ConfigContainer} object.
 *
 * @param <T>
 * 		Value type.
 *
 * @author Matt Coley
 */
public interface ConfigValue<T> {
	/**
	 * @return Unique ID of this value.
	 */
	String getId();

	/**
	 * @return Value type class.
	 */
	Class<T> getType();

	/**
	 * @return Observable of value.
	 */
	AbstractObservable<T> getObservable();

	/**
	 * @param value
	 * 		Value to set.
	 */
	default void setValue(T value) {
		getObservable().setValue(value);
	}

	/**
	 * @return Current value.
	 */
	default T getValue() {
		return getObservable().getValue();
	}
}
