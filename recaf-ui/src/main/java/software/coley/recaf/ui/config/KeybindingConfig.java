package software.coley.recaf.ui.config;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.coley.observables.ObservableMap;
import software.coley.recaf.config.BasicConfigContainer;
import software.coley.recaf.config.BasicMapConfigValue;
import software.coley.recaf.config.ConfigGroups;
import software.coley.recaf.ui.pane.editing.ClassPane;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static javafx.scene.input.KeyCode.*;
import static software.coley.recaf.ui.config.Binding.newBind;

/**
 * Config for various keybinds.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class KeybindingConfig extends BasicConfigContainer {
	public static final String ID = "bind";
	private static final String ID_FIND = "editor.find";
	private static final String ID_SAVE = "editor.save";
	private final BindingBundle bundle;

	@Inject
	public KeybindingConfig() {
		super(ConfigGroups.SERVICE_UI, ID + CONFIG_SUFFIX);

		// We will only be storing one 'value' so that the UI can treat it as a singular element.
		bundle = new BindingBundle(Arrays.asList(
				newBind(ID_FIND, CONTROL, F),
				newBind(ID_SAVE, CONTROL, S)
		));
		addValue(new BasicMapConfigValue<>("bundle", Map.class, String.class, Binding.class, bundle));
	}

	/**
	 * @return Keybinding for opening find.
	 */
	@Nonnull
	public Binding getFind() {
		// TODO: Update javadocs to @link to component when made
		return Objects.requireNonNull(bundle.get(ID_FIND));
	}

	/**
	 * @return Keybinding to save within a {@link ClassPane} or {@link FilePane}.
	 */
	@Nonnull
	public Binding getSave() {
		// TODO: Update javadocs to @link to FilePane when made
		return Objects.requireNonNull(bundle.get(ID_SAVE));
	}

	/**
	 * Binding bundle containing all keys.
	 */
	public static class BindingBundle extends ObservableMap<String, Binding, Map<String, Binding>> {
		public BindingBundle(@Nonnull List<Binding> binds) {
			super(binds.stream().collect(Collectors.toMap(Binding::getId, Function.identity())), HashMap::new);
		}
	}
}
