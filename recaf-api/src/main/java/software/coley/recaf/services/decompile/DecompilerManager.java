package software.coley.recaf.services.decompile;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.coley.observables.ObservableObject;
import software.coley.recaf.services.Service;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Manager of multiple {@link Decompiler} instances.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class DecompilerManager implements Service {
	public static final String SERVICE_ID = "decompilers";
	private static final NoopJvmDecompiler NO_OP_JVM = NoopJvmDecompiler.getInstance();
	private static final NoopAndroidDecompiler NO_OP_ANDROID = NoopAndroidDecompiler.getInstance();
	private final Map<String, JvmDecompiler> jvmDecompilers = new TreeMap<>();
	private final Map<String, AndroidDecompiler> androidDecompilers = new TreeMap<>();
	private final DecompilerManagerConfig config;
	private final ObservableObject<JvmDecompiler> targetJvmDecompiler;
	private final ObservableObject<AndroidDecompiler> targetAndroidDecompiler;

	/**
	 * @param config
	 * 		Config to pull values from.
	 */
	@Inject
	public DecompilerManager(@Nonnull DecompilerManagerConfig config) {
		this.config = config;
		targetJvmDecompiler = config.getPreferredJvmDecompiler()
				.mapObject(key -> jvmDecompilers.getOrDefault(key == null ? "" : key, NO_OP_JVM));
		targetAndroidDecompiler = config.getPreferredAndroidDecompiler()
				.mapObject(key -> androidDecompilers.getOrDefault(key == null ? "" : key, NO_OP_ANDROID));
	}

	/**
	 * @return Preferred JVM decompiler.
	 */
	public JvmDecompiler getTargetJvmDecompiler() {
		return targetJvmDecompiler.getValue();
	}

	/**
	 * @return Preferred Android decompiler.
	 */
	public AndroidDecompiler getTargetAndroidDecompiler() {
		return targetAndroidDecompiler.getValue();
	}

	/**
	 * @param decompiler
	 * 		JVM decompiler to add.
	 */
	public void register(JvmDecompiler decompiler) {
		jvmDecompilers.put(decompiler.getName(), decompiler);
	}

	/**
	 * @param decompiler
	 * 		Android decompiler to add.
	 */
	public void register(AndroidDecompiler decompiler) {
		androidDecompilers.put(decompiler.getName(), decompiler);
	}

	/**
	 * @return Available JVM class decompilers.
	 */
	public Collection<JvmDecompiler> getJvmDecompilers() {
		return jvmDecompilers.values();
	}

	/**
	 * @return Available android class decompilers.
	 */
	public Collection<AndroidDecompiler> getAndroidDecompilers() {
		return androidDecompilers.values();
	}

	@Override
	public String getServiceId() {
		return SERVICE_ID;
	}

	@Override
	public DecompilerManagerConfig getServiceConfig() {
		return config;
	}
}
