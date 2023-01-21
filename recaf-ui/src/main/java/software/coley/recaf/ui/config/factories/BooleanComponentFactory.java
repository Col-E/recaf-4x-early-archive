package software.coley.recaf.ui.config.factories;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import software.coley.observables.AbstractObservable;
import software.coley.recaf.config.ConfigContainer;
import software.coley.recaf.config.ConfigValue;
import software.coley.recaf.ui.config.TypedConfigComponentFactory;
import software.coley.recaf.util.Lang;

/**
 * Factory for general {@link Boolean} values.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class BooleanComponentFactory extends TypedConfigComponentFactory<Boolean> {
	@Inject
	public BooleanComponentFactory() {
		super(true, Boolean.class);
	}

	@Override
	public Node create(ConfigContainer container, ConfigValue<Boolean> value) {
		AbstractObservable<Boolean> observable = value.getObservable();
		String translationKey = container.getGroup() + "." + value.getKey();

		// Create the component.
		CheckBox check = new CheckBox();
		check.setSelected(observable.getValue());
		check.textProperty().bind(Lang.getBinding(translationKey));
		observable.addChangeListener((ob, old, cur) -> {
			if (check.isSelected() != cur)
				check.setSelected(cur);
		});
		check.selectedProperty().addListener((ob, old, cur) -> {
			if (observable.getValue() != cur)
				observable.setValue(cur);
		});
		return check;
	}
}
