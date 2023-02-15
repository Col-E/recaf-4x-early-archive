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

	/**
	 * @param other
	 * 		Other path to check.
	 *
	 * @return {@code true} when the given path is a parent of this one
	 * <i>(Having equal data, up to the point where is has missing fields)</i> or equal.
	 * {@code false} when the given path has fields that differ from us and both are non-null.
	 */
	public boolean isDescendantOf(WorkspaceTreePath other) {
		// Must be same workspace (never null)
		if (workspace != other.workspace)
			return false;

		// Must be same resource (never null)
		if (resource != other.resource)
			return false;

		// If the other has no bundle, and we do, we're a descendant.
		// If the other has a bundle, and we do not, we're not a descendant.
		// If we both have bundles, they must be the same.
		if (other.bundle == null && bundle == null)
			return true;
		if (other.bundle == null)
			return true;
		if (bundle == null)
			return false;
		if (bundle != other.bundle)
			return false;

		// If the other has no local-path, and we do, we're a descendant.
		// If the other has a local-path, and we do not, we're not a descendant.
		// If we both have local-paths, they must be the same.
		if (other.localPath == null && localPath == null)
			return true;
		if (other.localPath == null)
			return true;
		if (localPath == null)
			return false;
		if (!localPath.startsWith(other.localPath))
			return false;

		// If the other has no path, and we do not, we're equal.
		// If the other has no path, and we do, we're a descendant.
		// If the other has a path, and we do not, we're not a descendant.
		if (other.info == null && info == null)
			return true;
		if (other.info == null)
			return true;
		if (info == null)
			return false;
		return info.getName().equals(other.info.getName());
	}

	@Override
	public int compareTo(@Nonnull WorkspaceTreePath other) {
		// Show primary resource first.
		int cmp = Boolean.compare(isPrimary(), other.isPrimary());

		// Show in order as in the workspace.
		if (cmp == 0) {
			if (resource != other.resource) {
				List<WorkspaceResource> resources = workspace().getSupportingResources();
				cmp = Integer.compare(resources.indexOf(resource), resources.indexOf(other.resource));
			}
		}

		// Show bundles in order.
		if (cmp == 0) {
			if (bundle == null && other.bundle == null) {
				// Both do not have paths, so must be the same.
				return 0;
			}
			if (bundle == null) {
				// We do not have a path, but the other does.
				// Show us first.
				return -1;
			}
			if (other.bundle == null) {
				// We have a path, but the other does not.
				// Show them first.
				return 1;
			}
			cmp = -Integer.compare(bundleMask(), other.bundleMask());
		}

		// Show paths in case-insensitive order.
		if (cmp == 0) {
			if (localPath == null && other.localPath == null) {
				// Both do not have paths, so must be the same.
				return 0;
			}
			if (localPath == null) {
				// We do not have a path, but the other does.
				// Show us first.
				return -1;
			}
			if (other.localPath == null) {
				// We have a path, but the other does not.
				// Show them first.
				return 1;
			}

			// We both have paths, show in insensitive order (unless one of us has an info object).
			if (info != null && other.info == null) {
				// Info values go last.
				return 1;
			}
			if (info == null && other.info != null) {
				// Info values go last.
				return -1;
			}
			cmp = String.CASE_INSENSITIVE_ORDER.compare(localPath, other.localPath);
		}

		// Show paths in case-insensitive order.
		if (cmp == 0) {
			String infoName = info == null ? null : info.getName();
			String otherInfoName = other.info == null ? null : other.info.getName();
			if (infoName == null) {
				// Both do not have infos, so we must be the same.
				return 0;
			}

			// We both have paths, show in insensitive order.
			cmp = String.CASE_INSENSITIVE_ORDER.compare(infoName, otherInfoName);
		}
		return cmp;
	}
}
