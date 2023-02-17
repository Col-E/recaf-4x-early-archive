package software.coley.recaf.ui.control.tree.path;

import jakarta.annotation.Nonnull;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

/**
 * Path node for {@link Workspace} types.
 *
 * @author Matt Coley
 */
public class WorkspacePathNode extends AbstractPathNode<Object, Workspace> {
	/**
	 * Node without parent.
	 *
	 * @param value
	 * 		Workspace value.
	 */
	public WorkspacePathNode(@Nonnull Workspace value) {
		super(null, Workspace.class, value);
	}

	/**
	 * @param resource
	 * 		Resource to wrap into node.
	 *
	 * @return Path node of resource, with current workspace as parent.
	 */
	@Nonnull
	public ResourcePathNode child(@Nonnull WorkspaceResource resource) {
		return new ResourcePathNode(this, resource);
	}

	@Override
	public int localCompare(PathNode<?> o) {
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		WorkspacePathNode node = (WorkspacePathNode) o;

		return getValue() == node.getValue();
	}

	@Override
	public int hashCode() {
		return getValue().hashCode();
	}
}
