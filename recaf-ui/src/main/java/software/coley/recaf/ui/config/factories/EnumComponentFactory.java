package software.coley.recaf.ui.config.factories;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SingleSelectionModel;
import software.coley.observables.AbstractObservable;
import software.coley.observables.ObservableObject;
import software.coley.recaf.config.ConfigContainer;
import software.coley.recaf.config.ConfigValue;
import software.coley.recaf.ui.config.TypedConfigComponentFactory;
import software.coley.recaf.ui.control.ObservableComboBox;

import java.util.Arrays;

/**
 * Factory for general {@link Enum} values.
 *
 * @author Matt Coley
 */
@ApplicationScoped
@SuppressWarnings("rawtypes")
public class EnumComponentFactory extends TypedConfigComponentFactory<Enum> {
	@Inject
	public EnumComponentFactory() {
		super(false, Enum.class);
	}

	@Override
	public Node create(ConfigContainer container, ConfigValue<Enum> value) {
		Enum[] enumConstants = value.getType().getEnumConstants();
		return new ObservableComboBox<>(value.getObservable(), Arrays.asList(enumConstants));
	}
}
