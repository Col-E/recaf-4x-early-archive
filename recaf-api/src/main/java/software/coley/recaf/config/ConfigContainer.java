package software.coley.recaf.config;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;

/**
 * Configurable object. Implementations will almost always want to use {@link ApplicationScoped} for their scope.
 * This is so that the config container is shared between <i>all</i> instances of the item it is a config for.
 *
 * @author Matt Coley.
 * @see ConfigValue Values within this container.
 */
public interface ConfigContainer {
	String CONFIG_SUFFIX = "-config";

	/**
	 * @return Unique ID of this container.
	 */
	String getId();

	/**
	 * @return Values in the container.
	 */
	Map<String, ConfigValue<?>> getValues();
}
