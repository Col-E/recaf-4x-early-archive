package software.coley.recaf.config;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;

import static software.coley.recaf.config.ConfigGroups.PACKAGE_SPLIT;

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
	 * @return Group ID the container belongs to.
	 *
	 * @see ConfigGroups For constant values.
	 */
	String getGroup();

	/**
	 * The unique ID of this container should be <i>globally</i> unique.
	 * The {@link #getGroup() group} does not act as an identifier prefix.
	 *
	 * @return Unique ID of this container.
	 */
	String getId();

	/**
	 * @return Combined {@link #getGroup()} and {@link #getId()}.
	 */
	default String getGroupAndId() {
		return getGroup() + PACKAGE_SPLIT + getId();
	}

	/**
	 * @return Values in the container.
	 */
	Map<String, ConfigValue<?>> getValues();

	/**
	 * @param value
	 * 		Value to get path of.
	 *
	 * @return Full path, scoped to this container.
	 */
	default String getScopedId(ConfigValue<?> value) {
		return getGroupAndId() + PACKAGE_SPLIT + value.getId();
	}
}
