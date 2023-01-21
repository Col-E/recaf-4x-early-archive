package software.coley.recaf.ui.config.factories;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SingleSelectionModel;
import software.coley.observables.AbstractObservable;
import software.coley.recaf.config.ConfigContainer;
import software.coley.recaf.config.ConfigValue;
import software.coley.recaf.ui.config.TypedConfigComponentFactory;
import software.coley.recaf.util.Lang;

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
		AbstractObservable<Enum> observable = value.getObservable();
		Enum[] enumConstants = value.getType().getEnumConstants();

		// Create the component.
		ComboBox<Enum> combo = new ComboBox<>();
		combo.getItems().addAll(enumConstants);
		combo.getSelectionModel().select(value.getValue());
		SingleSelectionModel<Enum> model = combo.getSelectionModel();
		observable.addChangeListener((ob, old, cur) -> {
			if (model.getSelectedItem() != cur)
				model.select(cur);
		});
		model.selectedItemProperty().addListener((ob, old, cur) -> {
			if (observable.getValue() != cur)
				observable.setValue(cur);
		});
		return combo;
	}
}
