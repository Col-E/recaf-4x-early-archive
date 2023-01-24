package software.coley.recaf.ui.config;

import atlantafx.base.theme.Styles;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import javafx.scene.Node;
import javafx.scene.control.Label;
import software.coley.recaf.config.ConfigContainer;
import software.coley.recaf.config.ConfigValue;
import software.coley.recaf.ui.pane.ConfigPane;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages which {@link ConfigComponentFactory} instances to use to create editor components in the {@link ConfigPane}
 * for {@link ConfigValue} instances.
 *
 * @author Matt Coley
 * @see ConfigPane UI where this manager is used.
 * @see ConfigComponentFactory Factory base.
 */
@ApplicationScoped
public class ConfigComponentManager {
	private final ConfigComponentFactory<Object> DEFAULT_FACTORY = new ConfigComponentFactory<>(false) {
		@Override
		public Node create(ConfigContainer container, ConfigValue<Object> value) {
			Label label = new Label("Unsupported: " + value.getType().getName());
			label.getStyleClass().add(Styles.WARNING);
			return label;
		}
	};
	private final Map<String, ConfigComponentFactory<?>> keyToConfigurator = new HashMap<>();
	private final Map<Class<?>, ConfigComponentFactory<?>> typeToConfigurator = new HashMap<>();

	@Inject
	public ConfigComponentManager(Instance<KeyedConfigComponentFactory<?>> keyedFactories,
								  Instance<TypedConfigComponentFactory<?>> typedFactories) {
		// Register implementations
		for (KeyedConfigComponentFactory<?> factory : keyedFactories)
			register(factory.getId(), factory);
		for (TypedConfigComponentFactory<?> factory : typedFactories)
			register(factory.getType(), factory);
	}

	/**
	 * @param id
	 * 		A {@link ConfigValue#getId()}, used to create factories to generate components for a specific value.
	 * @param factory
	 * 		Factory to generate components to support the given type.
	 */
	public void register(String id, ConfigComponentFactory<?> factory) {
		keyToConfigurator.put(id, factory);
	}

	/**
	 * @param type
	 * 		Class type.
	 * @param factory
	 * 		Factory to generate components to support the given type.
	 */
	public void register(Class<?> type, ConfigComponentFactory<?> factory) {
		typeToConfigurator.put(type, factory);
	}

	/**
	 * @param value
	 * 		Value to get factory for.
	 * @param <T>
	 * 		Value type.
	 *
	 * @return Component factory for value.
	 */
	@SuppressWarnings("unchecked")
	public <T> ConfigComponentFactory<T> getFactory(ConfigValue<T> value) {
		// Get factory for config value ID.
		String id = value.getId();
		ConfigComponentFactory<?> factory = keyToConfigurator.get(id);
		if (factory != null)
			return (ConfigComponentFactory<T>) factory;

		// Get factory for config value type.
		Class<T> type = value.getType();
		factory = typeToConfigurator.get(type);
		if (factory != null)
			return (ConfigComponentFactory<T>) factory;

		// Check for common generic types.
		if (type.isEnum())
			factory = typeToConfigurator.get(Enum.class);
		if (factory != null)
			return (ConfigComponentFactory<T>) factory;

		// Fallback factory.
		return (ConfigComponentFactory<T>) DEFAULT_FACTORY;
	}
}
