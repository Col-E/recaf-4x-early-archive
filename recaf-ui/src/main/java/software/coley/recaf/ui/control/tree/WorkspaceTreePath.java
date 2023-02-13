package software.coley.recaf.ui.control.tree;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.FileInfo;
import software.coley.recaf.info.Info;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.Bundle;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.util.List;

/**
 * Wrapper item for {@link WorkspaceTree}.
 *
 * @param workspace
 * 		The workspace.
 * @param resource
 * 		Resource in the workspace.
 * @param bundle
 * 		Bundle object if representing a {@link Bundle} or {@link Info}.
 * @param localPath
 * 		Partial path of an {@link Info#getName()}.
 * 		Used to represent packages of {@link ClassInfo} and directories of {@link FileInfo}.
 * @param info
 * 		Info object if representing an {@link Info}.
 *
 * @author Matt Coley
 */
public record WorkspaceTreePath(@Nonnull Workspace workspace,
								@Nonnull WorkspaceResource resource,
								@Nullable Bundle<? extends Info> bundle,
								@Nullable String localPath,
								@Nullable Info info) implements Comparable<WorkspaceTreePath> {
	/**
	 * @return {@code true} when the path represents an item in the primary resource.
	 */
	public boolean isPrimary() {
		return workspace.getPrimaryResource() == resource;
	}

	/**
	 * @return {@code true} when the path wraps a bundle object.
	 */
	public boolean hasBundle() {
		return bundle != null;
	}

	/**
	 * @return {@code true} when the path wraps contains a local path for a {@link Info}.
	 */
	public boolean hasLocalPath() {
		return localPath != null;
	}

	/**
	 * @return {@code true} when the path wraps an info object.
	 */
	public boolean hasInfo() {
		return info != null;
	}

	/**
	 * @return {@code true} when the path is in the resource's immediate JVM class bundle.
	 */
	public boolean isInJvmBundle() {
		return resource.getJvmClassBundle() == bundle;
	}

	/**
	 * @return {@code true} when the path is in one of the resource's Android bundles.
	 */
	@SuppressWarnings("all")
	public boolean isInAndroidBundle() {
		return resource.getAndroidClassBundles().containsValue(bundle);
	}

	/**
	 * @return {@code true} when the path is in the resource's immediate file bundle.
	 */
	public boolean isInFileBundle() {
		return resource.getFileBundle() == bundle;
	}

	/**
	 * @return {@code true} when the path is in one of the resource's versioned class bundles.
	 */
	public boolean isInVersionedJvmBundle() {
		if (!(bundle instanceof JvmClassBundle)) return false;
		return resource.getVersionedJvmClassBundles().containsValue((JvmClassBundle) bundle);
	}

	/**
	 * @return {@code true} when the path is an embedded container in {@link WorkspaceResource#getEmbeddedResources()}.
	 */
	public boolean isEmbeddedContainer() {
		if (bundle != null || info == null) return false;
		return resource.getEmbeddedResources().containsKey(info.getName());
	}

	/**
	 * @return Mask for {@link #compareTo(WorkspaceTreePath)} against bundles.
	 */
	private int bundleMask() {
		return ((isInJvmBundle() ? 1 : 0) << 16) |
				((isInVersionedJvmBundle() ? 1 : 0) << 14) |
				((isInAndroidBundle() ? 1 : 0) << 12) |
				((isInFileBundle() ? 1 : 0) << 10) |
				((isEmbeddedContainer() ? 1 : 0) << 8);
	}

	@Override
	public int compareTo(@Nonnull WorkspaceTreePath other) {
		int cmp = 0;

		// Local paths, and Info
		if (info != null) {
			if (other.info != null) {
				// Show local path values in order by name.
				cmp = String.CASE_INSENSITIVE_ORDER.compare(info.getName(), other.info.getName());
			} else {
				// We have a file path but the other does not.
				// This means we represent a file while the other represents a directory.
				// Thus, we should always show last.
				cmp = 1;
			}
		}
		if (cmp == 0 && other.info != null) {
			if (info != null) {
				// Show local path values in order by name.
				cmp = String.CASE_INSENSITIVE_ORDER.compare(info.getName(), other.info.getName());
			} else {
				// We have a local path but the other does not.
				// This means we represent a directory while the other represents a file.
				// Thus, we should always show first.
				cmp = -1;
			}
		}
		if (cmp == 0 && localPath != null && other.localPath != null) {
			cmp = String.CASE_INSENSITIVE_ORDER.compare(localPath, other.localPath);
		}

		// Bundles
		if (cmp == 0 && bundle != null && other.bundle != null) {
			// Show bundles in order.
			cmp = -Integer.compare(bundleMask(), other.bundleMask());
		}

		// Resources
		if (cmp == 0 && bundle == null && other.bundle == null) {
			// Show primary resource first.
			cmp = Boolean.compare(isPrimary(), other.isPrimary());
			if (cmp == 0) {
				// Show in order as in the workspace.
				List<WorkspaceResource> resources = workspace().getSupportingResources();
				cmp = Integer.compare(resources.indexOf(resource), resources.indexOf(other.resource));
			}
		}
		return cmp;
	}
}
