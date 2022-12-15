package software.coley.recaf.workspace.model;

import jakarta.enterprise.context.Dependent;
import software.coley.recaf.workspace.WorkspaceModificationListener;
import software.coley.recaf.workspace.model.resource.RuntimeWorkspaceResource;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Basic workspace implementation.
 *
 * @author Matt Coley
 */
public class BasicWorkspace implements Workspace {
	private final List<WorkspaceModificationListener> modificationListeners = new ArrayList<>();
	private final WorkspaceResource primary;
	private final List<WorkspaceResource> supporting = new ArrayList<>();
	private final List<WorkspaceResource> internal = Collections.singletonList(RuntimeWorkspaceResource.getInstance());

	/**
	 * @param primary
	 * 		Primary resource.
	 */
	public BasicWorkspace(WorkspaceResource primary) {
		this(primary, Collections.emptyList());
	}

	/**
	 * @param primary
	 * 		Primary resource.
	 * @param supporting
	 * 		Provided supporting resources.
	 */
	public BasicWorkspace(WorkspaceResource primary, Collection<WorkspaceResource> supporting) {
		this.primary = primary;
		this.supporting.addAll(supporting);
	}

	@Override
	public WorkspaceResource getPrimaryResource() {
		return primary;
	}

	@Override
	public List<WorkspaceResource> getSupportingResources() {
		return supporting;
	}

	@Override
	public List<WorkspaceResource> getInternalSupportingResources() {
		return internal;
	}

	@Override
	public List<WorkspaceModificationListener> getWorkspaceModificationListeners() {
		return modificationListeners;
	}

	@Override
	public void addWorkspaceModificationListener(WorkspaceModificationListener listener) {
		modificationListeners.add(listener);
	}

	@Override
	public void removeWorkspaceModificationListener(WorkspaceModificationListener listener) {
		modificationListeners.remove(listener);
	}

	@Override
	public String toString() {
		return "BasicWorkspace{" +
				"primary=" + primary +
				", supporting=" + supporting +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Workspace other = (Workspace) o;

		if (!primary.equals(other.getPrimaryResource())) return false;
		return supporting.equals(other.getSupportingResources());
	}

	@Override
	public int hashCode() {
		int result = primary.hashCode();
		result = 31 * result + supporting.hashCode();
		return result;
	}
}
