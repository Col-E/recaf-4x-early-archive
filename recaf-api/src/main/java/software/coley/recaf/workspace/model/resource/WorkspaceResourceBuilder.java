package software.coley.recaf.workspace.model.resource;

import software.coley.recaf.workspace.model.bundle.AndroidClassBundle;
import software.coley.recaf.workspace.model.bundle.FileBundle;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class WorkspaceResourceBuilder {
	private JvmClassBundle jvmClassBundle;
	private NavigableMap<Integer, JvmClassBundle> versionedJvmClassBundles = new TreeMap<>();
	private Map<String, AndroidClassBundle> androidClassBundles = Collections.emptyMap();
	private FileBundle fileBundle;
	private Map<String, WorkspaceResource> embeddedResources = Collections.emptyMap();
	private WorkspaceResource containingResource;
	private Path filePath;
	private URI uri;

	/**
	 * Empty builder.
	 */
	public WorkspaceResourceBuilder() {
		// default
	}

	/**
	 * Builder with required inputs.
	 *
	 * @param classes
	 * 		Primary classes.
	 * @param files
	 * 		Primary files.
	 */
	public WorkspaceResourceBuilder(JvmClassBundle classes, FileBundle files) {
		withJvmClassBundle(classes);
		withFileBundle(files);
	}

	private WorkspaceResourceBuilder(WorkspaceResourceBuilder other) {
		withJvmClassBundle(other.getJvmClassBundle());
		withAndroidClassBundles(other.getAndroidClassBundles());
		withVersionedJvmClassBundles(other.getVersionedJvmClassBundles());
		withFileBundle(other.getFileBundle());
		withEmbeddedResources(other.getEmbeddedResources());
		withContainingResource(other.getContainingResource());
	}

	public WorkspaceResourceBuilder withJvmClassBundle(JvmClassBundle primaryJvmClassBundle) {
		this.jvmClassBundle = primaryJvmClassBundle;
		return this;
	}

	public WorkspaceResourceBuilder withVersionedJvmClassBundles(NavigableMap<Integer, JvmClassBundle> versionedJvmClassBundles) {
		this.versionedJvmClassBundles = versionedJvmClassBundles;
		return this;
	}

	public WorkspaceResourceBuilder withAndroidClassBundles(Map<String, AndroidClassBundle> androidClassBundles) {
		this.androidClassBundles = androidClassBundles;
		return this;
	}

	public WorkspaceResourceBuilder withFileBundle(FileBundle primaryFileBundle) {
		this.fileBundle = primaryFileBundle;
		return this;
	}

	public WorkspaceResourceBuilder withEmbeddedResources(Map<String, WorkspaceResource> embeddedResources) {
		this.embeddedResources = embeddedResources;
		return this;
	}

	public WorkspaceResourceBuilder withContainingResource(WorkspaceResource containingResource) {
		this.containingResource = containingResource;
		return this;
	}

	public WorkspaceResourceBuilder withFilePath(Path path) {
		this.filePath = path;
		return new WorkspaceResourceBuilder(this) {
			@Override
			public WorkspaceResource build() {
				return new BasicWorkspaceFileResource(this);
			}
		};
	}

	public WorkspaceResourceBuilder withUri(URI uri) {
		this.uri = uri;
		return new WorkspaceResourceBuilder(this) {
			@Override
			public WorkspaceResource build() {
				return new BasicWorkspaceUriResource(this);
			}
		};
	}

	public JvmClassBundle getJvmClassBundle() {
		return jvmClassBundle;
	}

	public NavigableMap<Integer, JvmClassBundle> getVersionedJvmClassBundles() {
		return versionedJvmClassBundles;
	}

	public Map<String, AndroidClassBundle> getAndroidClassBundles() {
		return androidClassBundles;
	}

	public FileBundle getFileBundle() {
		return fileBundle;
	}

	public Map<String, WorkspaceResource> getEmbeddedResources() {
		return embeddedResources;
	}

	public WorkspaceResource getContainingResource() {
		return containingResource;
	}

	public Path getFilePath() {
		return filePath;
	}

	public URI getUri() {
		return uri;
	}

	/**
	 * @return New resource from builder.
	 * Implementation type overridden when {@link #withFilePath(Path)} or {@link #withUri(URI)} are used.
	 */
	public WorkspaceResource build() {
		return new BasicWorkspaceResource(this);
	}
}
