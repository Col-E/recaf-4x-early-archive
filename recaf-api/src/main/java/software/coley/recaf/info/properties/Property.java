package software.coley.recaf.info.properties;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Basic property outline.
 *
 * @param <V>
 * 		Value type.
 *
 * @author Matt Coley
 */
public interface Property<V> {
	/**
	 * @return Property key.
	 */
	@Nonnull
	String key();

	/**
	 * @return Property value.
	 */
	@Nullable
	V value();
}
