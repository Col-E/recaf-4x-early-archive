package software.coley.recaf.ui.pane.editing.jvm;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.coley.observables.ObservableInteger;
import software.coley.recaf.config.BasicConfigContainer;
import software.coley.recaf.config.BasicConfigValue;
import software.coley.recaf.config.ConfigGroups;

/**
 * Config for {@link JvmDecompilerPane}.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class JvmDecompilerPaneConfig extends BasicConfigContainer {
	private final ObservableInteger timeoutSeconds = new ObservableInteger(60);

	@Inject
	public JvmDecompilerPaneConfig() {
		super(ConfigGroups.SERVICE_UI, "decompile-pane" + CONFIG_SUFFIX);
		addValue(new BasicConfigValue<>("timeout-seconds", int.class, timeoutSeconds));
	}

	@Nonnull
	public ObservableInteger getTimeoutSeconds() {
		return timeoutSeconds;
	}
}
