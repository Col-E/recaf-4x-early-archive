package software.coley.recaf.workspace.model.resource;

import software.coley.recaf.behavior.Closing;
import software.coley.recaf.info.AndroidClassInfo;
import software.coley.recaf.info.FileInfo;
import software.coley.recaf.info.Info;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.info.properties.PropertyContainer;
import software.coley.recaf.info.properties.builtin.ContainingResourceProperty;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.AndroidClassBundle;
import software.coley.recaf.workspace.model.bundle.BundleListener;
import software.coley.recaf.workspace.model.bundle.FileBundle;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;

import java.util.*;

/**
 * Basic workspace resource implementation.
 *
 * @author Matt Coley
 * @see WorkspaceResourceBuilder Helper for creating instances.
 */
public class BasicWorkspaceResource implements WorkspaceResource {
	private final List<ResourceJvmClassListener> jvmClassListeners = new ArrayList<>();
	private final List<ResourceAndroidClassListener> androidClassListeners = new ArrayList<>();
	private final List<ResourceFileListener> fileListeners = new ArrayList<>();
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
		setupListenerDelegation();
		linkContentsToResource();
	}

	/**
	 * Add listeners to all bundles contained in the resource,
	 * which forward to the appropriate resource-level listener types.
	 */
	private void setupListenerDelegation() {
		WorkspaceResource resource = this;
		jvmClassBundleStream().forEach(bundle -> bundle.addBundleListener(new BundleListener<>() {
			@Override
			public void onNewItem(String key, JvmClassInfo cls) {
				jvmClassListeners.forEach(l -> l.onNewClass(resource, bundle, cls));
			}

			@Override
			public void onUpdateItem(String key, JvmClassInfo oldCls, JvmClassInfo newCls) {
				jvmClassListeners.forEach(l -> l.onUpdateClass(resource, bundle, oldCls, newCls));
			}

			@Override
			public void onRemoveItem(String key, JvmClassInfo cls) {
				jvmClassListeners.forEach(l -> l.onRemoveClass(resource, bundle, cls));
			}
		}));
		androidClassBundleStream().forEach(bundle -> bundle.addBundleListener(new BundleListener<>() {
			@Override
			public void onNewItem(String key, AndroidClassInfo cls) {
				androidClassListeners.forEach(l -> l.onNewClass(resource, bundle, cls));
			}

			@Override
			public void onUpdateItem(String key, AndroidClassInfo oldCls, AndroidClassInfo newCls) {
				androidClassListeners.forEach(l -> l.onUpdateClass(resource, bundle, oldCls, newCls));
			}

			@Override
			public void onRemoveItem(String key, AndroidClassInfo cls) {
				androidClassListeners.forEach(l -> l.onRemoveClass(resource, bundle, cls));
			}
		}));
		fileBundleStream().forEach(bundle -> bundle.addBundleListener(new BundleListener<>() {
			@Override
			public void onNewItem(String key, FileInfo file) {
				fileListeners.forEach(l -> l.onNewFile(resource, bundle, file));
			}

			@Override
			public void onUpdateItem(String key, FileInfo oldFile, FileInfo newFile) {
				fileListeners.forEach(l -> l.onUpdateFile(resource, bundle, oldFile, newFile));
			}

			@Override
			public void onRemoveItem(String key, FileInfo file) {
				fileListeners.forEach(l -> l.onRemoveFile(resource, bundle, file));
			}
		}));
	}

	/**
	 * Ensure {@link ContainingResourceProperty} is assigned to all {@link Info} values within this resource.
	 */
	private void linkContentsToResource() {
		// Link all existing items in all contained bundles.
		bundleStream()
				.flatMap(bundle -> bundle.values().stream())
				.filter(info -> info instanceof PropertyContainer)
				.forEach(info -> ContainingResourceProperty.set((PropertyContainer) info, this));

		// Register listener to ensure all resources on this workspace have the built-in property
		// assigned for quick lookup of info-to-resource.
		WorkspaceResource resource = this;
		BundleListener<Info> bundleListener = new BundleListener<>() {
			@Override
			public void onNewItem(String key, Info info) {
				if (info instanceof PropertyContainer)
					ContainingResourceProperty.set((PropertyContainer) info, resource);
			}

			@Override
			public void onUpdateItem(String key, Info oldInfo, Info newInfo) {
				if (newInfo instanceof PropertyContainer)
					ContainingResourceProperty.set((PropertyContainer) newInfo, resource);
			}

			@Override
			public void onRemoveItem(String key, Info info) {
				// no-op
			}
		};
		bundleStream().forEach(bundle -> bundle.addBundleListener(bundleListener));
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
	public void addResourceJvmClassListener(ResourceJvmClassListener listener) {
		jvmClassListeners.add(listener);
	}

	@Override
	public void removeResourceJvmClassListener(ResourceJvmClassListener listener) {
		jvmClassListeners.remove(listener);
	}

	@Override
	public void addResourceAndroidClassListener(ResourceAndroidClassListener listener) {
		androidClassListeners.add(listener);
	}

	@Override
	public void removeResourceAndroidClassListener(ResourceAndroidClassListener listener) {
		androidClassListeners.remove(listener);
	}

	@Override
	public void addResourceFileListener(ResourceFileListener listener) {
		fileListeners.add(listener);
	}

	@Override
	public void removeResourceFileListener(ResourceFileListener listener) {
		fileListeners.remove(listener);
	}

	/**
	 * Called by containing {@link Workspace#close()}.
	 */
	@Override
	public void close() {
		// Clear all listeners
		jvmClassListeners.clear();
		androidClassListeners.clear();
		fileListeners.clear();
		bundleStream().forEach(Closing::close);
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
