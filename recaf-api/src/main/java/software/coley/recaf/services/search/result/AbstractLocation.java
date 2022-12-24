package software.coley.recaf.services.search.result;

import jakarta.annotation.Nonnull;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.util.Objects;

/**
 * Base for other location types.
 *
 * @author Matt Coley
 */
public abstract class AbstractLocation implements Location {
	private final Workspace workspace;
	private final WorkspaceResource resource;

	protected AbstractLocation(@Nonnull Workspace workspace, @Nonnull WorkspaceResource resource) {
		this.workspace = workspace;
		this.resource = resource;
	}

	@Nonnull
	@Override
	public Workspace getContainingWorkspace() {
		return workspace;
	}

	@Nonnull
	@Override
	public WorkspaceResource getContainingResource() {
		return resource;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Location other = (Location) o;
		return Objects.equals(comparableString(), other.comparableString());
	}

	@Override
	public int hashCode() {
		return comparableString().hashCode();
	}

	@Override
	public String toString() {
		return comparableString();
	}
}
