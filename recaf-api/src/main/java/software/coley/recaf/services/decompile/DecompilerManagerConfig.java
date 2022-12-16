package software.coley.recaf.services.decompile;

import jakarta.enterprise.context.ApplicationScoped;
import software.coley.observables.ObservableString;
import software.coley.recaf.services.ServiceConfig;

/**
 * Config for {@link DecompilerManager}
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class DecompilerManagerConfig implements ServiceConfig {
	private final ObservableString preferredJvmDecompiler = new ObservableString(null);
	private final ObservableString preferredAndroidDecompiler = new ObservableString(null);

	public ObservableString getPreferredJvmDecompiler() {
		return preferredJvmDecompiler;
	}

	public ObservableString getPreferredAndroidDecompiler() {
		return preferredAndroidDecompiler;
	}
}
