package software.coley.recaf.ui.control.tree.path;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Base implementation of {@link PathNode}.
 *
 * @param <P>
 * 		Expected parent path node value type.
 * @param <V>
 * 		Wrapped path value type.
 *
 * @author Matt Coley
 */
public abstract class AbstractPathNode<P, V> implements PathNode<V> {
	private final PathNode<P> parent;
	private final Class<V> valueType;
	private final V value;

	/**
	 * @param parent
	 * 		Optional parent node.
	 * @param valueType
	 * 		Type of value.
	 * @param value
	 * 		Value instance.
	 */
	protected AbstractPathNode(@Nullable PathNode<P> parent, @Nonnull Class<V> valueType, @Nonnull V value) {
		this.parent = parent;
		this.valueType = valueType;
		this.value = value;
	}

	/**
	 * Convenient parent value getter.
	 *
	 * @return Parent value, or {@code null}.
	 */
	@Nullable
	protected P parentValue() {
		return parent == null ? null : parent.getValue();
	}

	/**
	 * @param path
	 * 		Some other path.
	 *
	 * @return Comparing our parent value type to the given path,
	 * and the other path parent value type to our own.
	 * If we are the child type, then {@code -1} or {@link 1} if the parent type.
	 * Otherwise {@code 0}.
	 */
	protected int cmpHierarchy(@Nonnull PathNode<?> path) {
		// We are the child type, show last.
		if (parent != null && parent.getValueType() == path.getValueType())
			return 1;

		// We are the parent type, show first.
		if (path.getParent() != null && path.getParent().getValueType() == getValueType())
			return -1;

		// Unknown
		return 0;
	}

	/**
	 * @param path
	 * 		Some other path.
	 *
	 * @return Comparing {@link #getParent() the parent} to the given value.
	 */
	protected int cmpParent(@Nonnull PathNode<?> path) {
		if (parent != null)
			return parent.compareTo(path);
		return 0;
	}

	@Override
	public PathNode<P> getParent() {
		return parent;
	}

	@Nonnull
	@Override
	public Class<V> getValueType() {
		return valueType;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getValueOfType(@Nonnull Class<T> type) {
		if (getValueType().isAssignableFrom(type))
			return (T) getValue();
		if (parent == null) return null;
		return parent.getValueOfType(type);
	}

	@Nonnull
	@Override
	@SuppressWarnings("all")
	public V getValue() {
		return value;
	}

	@Override
	public boolean isDescendantOf(@Nonnull PathNode<?> other) {
		if (getValueType() == other.getValueType()) {
			return localCompare(other) >= 0;
		}
		if (parent != null) {
			if (parent.getValueType() == other.getValueType()) {
				return parent.localCompare(other) >= 0;
			}
			return parent.isDescendantOf(other);
		}
		return false;
	}

	@Override
	public int compareTo(@Nonnull PathNode<?> o) {
		if (this == o) return 0;
		int cmp = localCompare(o);
		if (cmp == 0) cmp = cmpHierarchy(o);
		if (cmp == 0) cmp = cmpParent(o);
		return cmp;
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
