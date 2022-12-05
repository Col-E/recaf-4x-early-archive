package software.coley.recaf.info.properties;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Map;

/**
 * Outline of a type with additional properties able to be assigned.
 *
 * @author Matt Coley
 */
public interface PropertyContainer {
	/**
	 * @param key
	 * 		Key of property to set.
	 * @param value
	 * 		Value of property to set.
	 * @param <V>
	 * 		Property value type.
	 */
	default <V> void setProperty(String key, V value) {
		setProperty(new BasicProperty<>(key, value));
	}

	/**
	 * @param property
	 * 		Property to set.
	 * @param <V>
	 * 		Property value type.
	 */
	<V> void setProperty(Property<V> property);

	/**
	 * @param key
	 * 		Property key.
	 * @param <V>
	 * 		Property value type.
	 *
	 * @return Property associated with key. May be {@code null} for unknown keys.
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	default <V> Property<V> getProperty(String key) {
		return (Property<V>) getProperties().get(key);
	}

	/**
	 * @param key
	 * 		Property key.
	 * @param <V>
	 * 		Property value type.
	 *
	 * @return Value of property, or {@code null} if the property is not set.
	 */
	@Nullable
	default <V> V getPropertyValueOrNull(String key) {
		Property<V> property = getProperty(key);
		if (property == null)
			return null;
		return property.value();
	}

	/**
	 * @return Properties.
	 */
	@Nonnull
	Map<String, Property<?>> getProperties();
}
