package software.coley.recaf.workspace.model.resource;

import software.coley.recaf.workspace.model.bundle.AndroidClassBundle;
import software.coley.recaf.workspace.model.bundle.FileBundle;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Basic workspace resource implementation.
 *
 * @author Matt Coley
 * @see WorkspaceResourceBuilder Helper for creating instances.
 */
public class BasicWorkspaceResource implements WorkspaceResource {
	private final JvmClassBundle primaryJvmClassBundle;
	private final FileBundle primaryFileBundle;
	private final Map<String, JvmClassBundle> jvmClassBundles;
	private final Map<String, AndroidClassBundle> androidClassBundles;
	private final Map<String, FileBundle> fileBundles;

	/**
	 * @param builder
	 * 		Builder to pull info from
	 */
	public BasicWorkspaceResource(WorkspaceResourceBuilder builder) {
		this(builder.getPrimaryJvmClassBundle(),
				builder.getPrimaryFileBundle(),
				builder.getJvmClassBundles(),
				builder.getAndroidClassBundles(),
				builder.getFileBundles());
	}

	/**
	 * @param primaryJvmClassBundle
	 * 		Immediate classes.
	 * @param primaryFileBundle
	 * 		Immediate files.
	 * @param jvmClassBundles
	 * 		Additional class bundles. May be {@code null} to ignore content.
	 * @param androidClassBundles
	 * 		Android bundles. May be {@code null} to ignore content.
	 * @param fileBundles
	 * 		Additional file bundles. May be {@code null} to ignore content.
	 */
	public BasicWorkspaceResource(JvmClassBundle primaryJvmClassBundle,
								  FileBundle primaryFileBundle,
								  Map<String, JvmClassBundle> jvmClassBundles,
								  Map<String, AndroidClassBundle> androidClassBundles,
								  Map<String, FileBundle> fileBundles) {
		this.primaryJvmClassBundle = primaryJvmClassBundle;
		this.primaryFileBundle = primaryFileBundle;
		this.jvmClassBundles = jvmClassBundles;
		this.androidClassBundles = androidClassBundles;
		this.fileBundles = fileBundles;
	}

	@Override
	public JvmClassBundle getPrimaryClassBundle() {
		return primaryJvmClassBundle;
	}

	@Override
	public FileBundle getPrimaryFileBundle() {
		return primaryFileBundle;
	}

	@Override
	public Map<String, JvmClassBundle> getJvmClassBundles() {
		if (jvmClassBundles == null) return Collections.emptyMap();
		return jvmClassBundles;
	}

	@Override
	public Map<String, AndroidClassBundle> getAndroidClassBundles() {
		if (androidClassBundles == null) return Collections.emptyMap();
		return androidClassBundles;
	}

	@Override
	public Map<String, FileBundle> getFileBundles() {
		if (fileBundles == null) return Collections.emptyMap();
		return fileBundles;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BasicWorkspaceResource resource = (BasicWorkspaceResource) o;

		if (!primaryJvmClassBundle.equals(resource.primaryJvmClassBundle)) return false;
		if (!primaryFileBundle.equals(resource.primaryFileBundle)) return false;
		if (!Objects.equals(jvmClassBundles, resource.jvmClassBundles)) return false;
		if (!Objects.equals(androidClassBundles, resource.androidClassBundles)) return false;
		return Objects.equals(fileBundles, resource.fileBundles);
	}

	@Override
	public int hashCode() {
		int result = primaryJvmClassBundle.hashCode();
		result = 31 * result + primaryFileBundle.hashCode();
		result = 31 * result + (jvmClassBundles != null ? jvmClassBundles.hashCode() : 0);
		result = 31 * result + (androidClassBundles != null ? androidClassBundles.hashCode() : 0);
		result = 31 * result + (fileBundles != null ? fileBundles.hashCode() : 0);
		return result;
	}
}
