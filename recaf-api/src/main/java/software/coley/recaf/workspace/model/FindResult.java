package software.coley.recaf.workspace.model;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.recaf.info.Info;
import software.coley.recaf.workspace.model.bundle.Bundle;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.util.Objects;

/**
 * Result for looking for an item.
 *
 * @param <T>
 * 		Item type.
 *
 * @author Matt Coley
 */
public class FindResult<T extends Info> {
	private final Workspace workspace;
	private final WorkspaceResource resource;
	private final Bundle<T> bundle;
	private final T item;

	/**
	 * @param workspace
	 * 		Workspace containing the matched item.
	 * @param resource
	 * 		Resource containing the matched item.
	 * 		Can be {@code null} when no result was found.
	 * @param bundle
	 * 		Bundle containing the matched item.
	 * 		Can be {@code null} when no result was found, or when the item is a container
	 * 		held by {@link WorkspaceResource#getEmbeddedResources()}.
	 * @param item
	 * 		Matched item.
	 * 		Can be {@code null} when no result was found.
	 */
	public FindResult(@Nonnull Workspace workspace, @Nullable WorkspaceResource resource,
					  @Nullable Bundle<T> bundle, @Nullable T item) {
		this.workspace = workspace;
		this.resource = resource;
		this.bundle = bundle;
		this.item = item;
	}

	/**
	 * @return {@code true} when {@link #getItem()} is {@code null}.
	 */
	public boolean isEmpty() {
		return item == null;
	}

	/**
	 * @return {@code true} when {@link #getContainingResource()} is the
	 * {@link Workspace#getPrimaryResource() primary resource}.
	 */
	public boolean isPrimary() {
		return workspace.getPrimaryResource() == resource;
	}

	/**
	 * @return {@code true} when the result is in the resource's immediate JVM class bundle.
	 */
	public boolean isInJvmBundle() {
		if (resource == null) return false;
		return resource.getJvmClassBundle() == bundle;
	}

	/**
	 * @return {@code true} when the result is in the resource's immediate file bundle.
	 */
	public boolean isInFileBundle() {
		if (resource == null) return false;
		return resource.getFileBundle() == bundle;
	}

	/**
	 * @return {@code true} when the result is in one of the resource's versioned class bundles.
	 */
	public boolean isInVersionedJvmBundle() {
		if (resource == null || !(bundle instanceof JvmClassBundle)) return false;
		return resource.getVersionedJvmClassBundles().containsValue((JvmClassBundle) bundle);
	}

	/**
	 * @return {@code true} when the result is an embedded container in {@link WorkspaceResource#getEmbeddedResources()}.
	 */
	public boolean isEmbeddedContainer() {
		if (resource == null || bundle != null || item == null) return false;
		return resource.getEmbeddedResources().containsKey(item.getName());
	}

	/**
	 * @return Workspace containing the matched item.
	 */
	@Nonnull
	public Workspace getContainingWorkspace() {
		return workspace;
	}

	/**
	 * @return Resource containing the matched item.
	 * Can be {@code null} when no result was found.
	 */
	@Nullable
	public WorkspaceResource getContainingResource() {
		return resource;
	}

	/**
	 * @return Bundle containing the matched item.
	 * Can be {@code null} when no result was found, or when the item is a container
	 * held by {@link WorkspaceResource#getEmbeddedResources()}.
	 */
	@Nullable
	public Bundle<T> getContainingBundle() {
		return bundle;
	}

	/**
	 * @return Matched item.
	 * Can be {@code null} when no result was found.
	 */
	@Nullable
	public T getItem() {
		return item;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		FindResult<?> that = (FindResult<?>) o;

		if (!workspace.equals(that.workspace)) return false;
		if (!Objects.equals(resource, that.resource)) return false;
		if (!Objects.equals(bundle, that.bundle)) return false;
		return Objects.equals(item, that.item);
	}

	@Override
	public int hashCode() {
		int result = workspace.hashCode();
		result = 31 * result + (resource != null ? resource.hashCode() : 0);
		result = 31 * result + (bundle != null ? bundle.hashCode() : 0);
		result = 31 * result + (item != null ? item.hashCode() : 0);
		return result;
	}
}
