package software.coley.recaf.ui.control.tree.path;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.recaf.workspace.model.bundle.Bundle;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

/**
 * Path node for {@link Bundle} types.
 *
 * @author Matt Coley
 */
@SuppressWarnings("rawtypes")
public class BundlePathNode extends AbstractPathNode<WorkspaceResource, Bundle> {
	/**
	 * Node without parent.
	 *
	 * @param bundle
	 * 		Bundle value.
	 */
	public BundlePathNode(@Nonnull Bundle<?> bundle) {
		this(null, bundle);
	}

	/**
	 * Node with parent.
	 *
	 * @param parent
	 * 		Parent node.
	 * @param bundle
	 * 		Bundle value.
	 *
	 * @see ResourcePathNode#child(Bundle)
	 */
	public BundlePathNode(@Nullable ResourcePathNode parent, @Nonnull Bundle<?> bundle) {
		super(parent, Bundle.class, bundle);
	}

	/**
	 * @param directory
	 * 		Directory to wrap in path node.
	 *
	 * @return Path node of directory, with current bundle as parent.
	 */
	@Nonnull
	public DirectoryPathNode child(@Nullable String directory) {
		return new DirectoryPathNode(this, directory == null ? "" : directory);
	}

	/**
	 * @return {@code true} when the path is in the resource's immediate JVM class bundle.
	 */
	public boolean isInJvmBundle() {
		WorkspaceResource resource = parentValue();
		return resource != null && resource.getJvmClassBundle() == getValue();
	}

	/**
	 * @return {@code true} when the path is in one of the resource's Android bundles.
	 */
	@SuppressWarnings("all")
	public boolean isInAndroidBundle() {
		WorkspaceResource resource = parentValue();
		return resource != null && resource.getAndroidClassBundles().containsValue(getValue());
	}

	/**
	 * @return {@code true} when the path is in the resource's immediate file bundle.
	 */
	public boolean isInFileBundle() {
		WorkspaceResource resource = parentValue();
		return resource != null && resource.getFileBundle() == getValue();
	}

	/**
	 * @return {@code true} when the path is in one of the resource's versioned class bundles.
	 */
	public boolean isInVersionedJvmBundle() {
		WorkspaceResource resource = parentValue();
		if (resource != null && getValue() instanceof JvmClassBundle jvmBundle)
			return resource.getVersionedJvmClassBundles().containsValue(jvmBundle);
		return false;
	}

	/**
	 * @return Bit-mask used for ordering in {@link #compareTo(PathNode)}.
	 */
	private int bundleMask() {
		return ((isInJvmBundle() ? 1 : 0) << 16) |
				((isInVersionedJvmBundle() ? 1 : 0) << 14) |
				((isInAndroidBundle() ? 1 : 0) << 12) |
				((isInFileBundle() ? 1 : 0) << 10);
	}

	@Override
	public ResourcePathNode getParent() {
		return (ResourcePathNode) super.getParent();
	}

	@Override
	public int localCompare(PathNode<?> o) {
		if (o instanceof BundlePathNode bundlePathNode) {
			return -Integer.compare(bundleMask(), bundlePathNode.bundleMask());
		}
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BundlePathNode node = (BundlePathNode) o;

		return getValue() == node.getValue();
	}

	@Override
	public int hashCode() {
		return getValue().hashCode();
	}
}
