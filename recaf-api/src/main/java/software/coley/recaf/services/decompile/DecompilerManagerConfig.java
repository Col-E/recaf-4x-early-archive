package software.coley.recaf.services.decompile;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.coley.observables.ObservableString;
import software.coley.recaf.config.BasicConfigContainer;
import software.coley.recaf.config.BasicConfigValue;
import software.coley.recaf.config.ConfigGroups;
import software.coley.recaf.services.ServiceConfig;

/**
 * Config for {@link DecompilerManager}
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class DecompilerManagerConfig extends BasicConfigContainer implements ServiceConfig {
	private final ObservableString preferredJvmDecompiler = new ObservableString(null);
	private final ObservableString preferredAndroidDecompiler = new ObservableString(null);

	@Inject
	public DecompilerManagerConfig() {
		super(ConfigGroups.SERVICE_DECOMPILE, DecompilerManager.SERVICE_ID + CONFIG_SUFFIX);
		// Add values
		addValue(new BasicConfigValue<>("pref-jvm-decompiler", String.class, preferredJvmDecompiler));
		addValue(new BasicConfigValue<>("pref-android-decompiler", String.class, preferredAndroidDecompiler));
	}

	/**
	 * @return {@link JvmDecompiler#getName()} for preferred JVM decompiler to use in {@link DecompilerManager}.
	 */
	public ObservableString getPreferredJvmDecompiler() {
		return preferredJvmDecompiler;
	}

	/**
	 * @return {@link AndroidDecompiler#getName()} for preferred JVM decompiler to use in {@link DecompilerManager}.
	 */
	public ObservableString getPreferredAndroidDecompiler() {
		return preferredAndroidDecompiler;
	}
}
