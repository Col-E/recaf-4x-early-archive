package software.coley.recaf.ui.pane.editing.jvm;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.coley.observables.ObservableBoolean;
import software.coley.observables.ObservableInteger;
import software.coley.recaf.config.BasicConfigContainer;
import software.coley.recaf.config.BasicConfigValue;
import software.coley.recaf.config.ConfigGroups;

import java.io.File;

/**
 * Config for {@link JvmDecompilerPane}.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class JvmDecompilerPaneConfig extends BasicConfigContainer {
	private final ObservableInteger timeoutSeconds = new ObservableInteger(60);
	private final ObservableBoolean acknowledgedSaveWithErrors = new ObservableBoolean(isNotDevEnv());

	@Inject
	public JvmDecompilerPaneConfig() {
		super(ConfigGroups.SERVICE_UI, "decompile-pane" + CONFIG_SUFFIX);
		addValue(new BasicConfigValue<>("timeout-seconds", int.class, timeoutSeconds));
		addValue(new BasicConfigValue<>("acknowledged-save-with-errors", boolean.class, acknowledgedSaveWithErrors, true));
	}

	/**
	 * @return Decompilation timeout in seconds.
	 */
	@Nonnull
	public ObservableInteger getTimeoutSeconds() {
		return timeoutSeconds;
	}

	/**
	 * @return Flag indicating if the user has acknowledged they cannot save with errors.
	 */
	@Nonnull
	public ObservableBoolean getAcknowledgedSaveWithErrors() {
		return acknowledgedSaveWithErrors;
	}

	private static boolean isNotDevEnv() {
		// Should only be true when building Recaf from source/build-system.
		return System.getProperty("java.class.path").contains("recaf-ui" + File.separator + "build");
	}
}
