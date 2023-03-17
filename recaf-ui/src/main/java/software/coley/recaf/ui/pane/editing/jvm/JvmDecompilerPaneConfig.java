package software.coley.recaf.ui.pane.editing.jvm;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.coley.observables.ObservableLong;
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
	private final ObservableLong timeoutSeconds = new ObservableLong(60);

	@Inject
	public JvmDecompilerPaneConfig() {
		super(ConfigGroups.SERVICE_UI, "decompile-pane" + CONFIG_SUFFIX);
		addValue(new BasicConfigValue<>("timeout-seconds", long.class, timeoutSeconds));
	}

	@Nonnull
	public ObservableLong getTimeoutSeconds() {
		return timeoutSeconds;
	}
}
