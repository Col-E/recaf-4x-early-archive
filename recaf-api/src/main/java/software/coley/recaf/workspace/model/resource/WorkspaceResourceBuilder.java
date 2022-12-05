package software.coley.recaf.workspace.model.resource;

import software.coley.recaf.workspace.model.bundle.AndroidClassBundle;
import software.coley.recaf.workspace.model.bundle.FileBundle;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

/**
 * Builder for {@link WorkspaceResource}.
 *
 * @author Matt Coley
 */
public class WorkspaceResourceBuilder {
	private JvmClassBundle primaryJvmClassBundle;
	private FileBundle primaryFileBundle;
	private Map<String, JvmClassBundle> jvmClassBundles = Collections.emptyMap();
	private Map<String, AndroidClassBundle> androidClassBundles = Collections.emptyMap();
	private Map<String, FileBundle> fileBundles = Collections.emptyMap();
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
		withPrimaryJvmClassBundle(classes);
		withPrimaryFileBundle(files);
	}

	private WorkspaceResourceBuilder(WorkspaceResourceBuilder other) {
		withPrimaryJvmClassBundle(other.getPrimaryJvmClassBundle());
		withPrimaryFileBundle(other.getPrimaryFileBundle());
		withJvmClassBundles(other.getJvmClassBundles());
		withAndroidClassBundles(other.getAndroidClassBundles());
		withFileBundles(other.getFileBundles());
	}

	public WorkspaceResourceBuilder withPrimaryJvmClassBundle(JvmClassBundle primaryJvmClassBundle) {
		this.primaryJvmClassBundle = primaryJvmClassBundle;
		return this;
	}

	public WorkspaceResourceBuilder withPrimaryFileBundle(FileBundle primaryFileBundle) {
		this.primaryFileBundle = primaryFileBundle;
		return this;
	}

	public WorkspaceResourceBuilder withJvmClassBundles(Map<String, JvmClassBundle> jvmClassBundles) {
		this.jvmClassBundles = jvmClassBundles;
		return this;
	}

	public WorkspaceResourceBuilder withAndroidClassBundles(Map<String, AndroidClassBundle> androidClassBundles) {
		this.androidClassBundles = androidClassBundles;
		return this;
	}

	public WorkspaceResourceBuilder withFileBundles(Map<String, FileBundle> fileBundles) {
		this.fileBundles = fileBundles;
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

	public JvmClassBundle getPrimaryJvmClassBundle() {
		return primaryJvmClassBundle;
	}

	public FileBundle getPrimaryFileBundle() {
		return primaryFileBundle;
	}

	public Map<String, JvmClassBundle> getJvmClassBundles() {
		return jvmClassBundles;
	}

	public Map<String, AndroidClassBundle> getAndroidClassBundles() {
		return androidClassBundles;
	}

	public Map<String, FileBundle> getFileBundles() {
		return fileBundles;
	}

	public Path getFilePath() {
		return filePath;
	}

	public URI getUri() {
		return uri;
	}

	public WorkspaceResource build() {
		return new BasicWorkspaceResource(this);
	}
}
