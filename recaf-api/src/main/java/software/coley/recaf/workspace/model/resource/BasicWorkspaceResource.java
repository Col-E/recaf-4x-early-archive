package software.coley.recaf.workspace.model.resource;

import software.coley.recaf.behavior.Closing;
import software.coley.recaf.info.AndroidClassInfo;
import software.coley.recaf.info.FileInfo;
import software.coley.recaf.info.Info;
import software.coley.recaf.info.JvmClassInfo;
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
	private final JvmClassBundle jvmClassBundle;
	private final NavigableMap<Integer, JvmClassBundle> versionedJvmClassBundles;
	private final Map<String, AndroidClassBundle> androidClassBundles;
	private final FileBundle fileBundle;
	private final Map<String, WorkspaceResource> embeddedResources;
	private WorkspaceResource containingResource;

	/**
	 * @param builder
	 * 		Builder to pull information from.
	 */
	public BasicWorkspaceResource(WorkspaceResourceBuilder builder) {
		this(builder.getJvmClassBundle(),
				builder.getFileBundle(),
				builder.getVersionedJvmClassBundles(),
				builder.getAndroidClassBundles(),
				builder.getEmbeddedResources(),
				builder.getContainingResource());
	}

	/**
	 * @param jvmClassBundle
	 * 		Immediate classes.
	 * @param fileBundle
	 * 		Immediate files.
	 * @param versionedJvmClassBundles
	 * 		Version specific classes.
	 * @param androidClassBundles
	 * 		Android bundles.
	 * @param embeddedResources
	 * 		Embedded resources <i>(like JAR in JAR)</i>
	 * @param containingResource
	 * 		Parent resource <i>(If we are the JAR within a JAR)</i>.
	 */
	public BasicWorkspaceResource(JvmClassBundle jvmClassBundle,
								  FileBundle fileBundle,
								  NavigableMap<Integer, JvmClassBundle> versionedJvmClassBundles,
								  Map<String, AndroidClassBundle> androidClassBundles,
								  Map<String, WorkspaceResource> embeddedResources,
								  WorkspaceResource containingResource) {
		this.jvmClassBundle = jvmClassBundle;
		this.fileBundle = fileBundle;
		this.versionedJvmClassBundles = versionedJvmClassBundles;
		this.androidClassBundles = androidClassBundles;
		this.embeddedResources = embeddedResources;
		this.containingResource = containingResource;
		setupListenerDelegation();
		linkContentsToResource();
		linkToEmbedded();
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
				.forEach(info -> ContainingResourceProperty.set(info, this));

		// Register listener to ensure all resources on this workspace have the built-in property
		// assigned for quick lookup of info-to-resource.
		WorkspaceResource resource = this;
		BundleListener<Info> bundleListener = new BundleListener<>() {
			@Override
			public void onNewItem(String key, Info info) {
				ContainingResourceProperty.set(info, resource);
			}

			@Override
			public void onUpdateItem(String key, Info oldInfo, Info newInfo) {
				ContainingResourceProperty.set(newInfo, resource);
			}

			@Override
			public void onRemoveItem(String key, Info info) {
				// no-op
			}
		};
		bundleStream().forEach(bundle -> bundle.addBundleListener(bundleListener));
	}

	/**
	 * Link all the embedded resources to the current instance as their container.
	 */
	private void linkToEmbedded() {
		embeddedResources.values().forEach(resource -> resource.setContainingResource(this));
	}

	@Override
	public JvmClassBundle getJvmClassBundle() {
		return jvmClassBundle;
	}

	@Override
	public NavigableMap<Integer, JvmClassBundle> getVersionedJvmClassBundles() {
		return versionedJvmClassBundles;
	}

	@Override
	public Map<String, AndroidClassBundle> getAndroidClassBundles() {
		if (androidClassBundles == null) return Collections.emptyMap();
		return androidClassBundles;
	}

	@Override
	public FileBundle getFileBundle() {
		return fileBundle;
	}

	@Override
	public Map<String, WorkspaceResource> getEmbeddedResources() {
		return embeddedResources;
	}

	@Override
	public WorkspaceResource getContainingResource() {
		return containingResource;
	}

	@Override
	public void setContainingResource(WorkspaceResource containingResource) {
		this.containingResource = containingResource;
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
		// Close embedded resources
		embeddedResources.values().forEach(WorkspaceResource::close);
		// Close all bundles
		bundleStream().forEach(Closing::close);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BasicWorkspaceResource resource = (BasicWorkspaceResource) o;

		if (!jvmClassListeners.equals(resource.jvmClassListeners)) return false;
		if (!androidClassListeners.equals(resource.androidClassListeners)) return false;
		if (!fileListeners.equals(resource.fileListeners)) return false;
		if (!jvmClassBundle.equals(resource.jvmClassBundle)) return false;
		if (!versionedJvmClassBundles.equals(resource.versionedJvmClassBundles)) return false;
		if (!androidClassBundles.equals(resource.androidClassBundles)) return false;
		if (!fileBundle.equals(resource.fileBundle)) return false;
		if (!embeddedResources.equals(resource.embeddedResources)) return false;
		return Objects.equals(containingResource, resource.containingResource);
	}

	@Override
	public int hashCode() {
		int result = jvmClassListeners.hashCode();
		result = 31 * result + androidClassListeners.hashCode();
		result = 31 * result + fileListeners.hashCode();
		result = 31 * result + jvmClassBundle.hashCode();
		result = 31 * result + versionedJvmClassBundles.hashCode();
		result = 31 * result + androidClassBundles.hashCode();
		result = 31 * result + fileBundle.hashCode();
		result = 31 * result + embeddedResources.hashCode();
		result = 31 * result + (containingResource != null ? containingResource.hashCode() : 0);
		return result;
	}
}
