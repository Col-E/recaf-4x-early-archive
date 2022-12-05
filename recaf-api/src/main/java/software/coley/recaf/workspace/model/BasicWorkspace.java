package software.coley.recaf.workspace.model;

import software.coley.recaf.workspace.WorkspaceModificationListener;
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

	/**
	 * @param primary Primary resource.
	 */
	public BasicWorkspace(WorkspaceResource primary) {
		this(primary, Collections.emptyList());
	}

	/**
	 * @param primary Primary resource.
	 * @param supporting Provided supporting resources.
	 */
	public BasicWorkspace(WorkspaceResource primary, Collection<WorkspaceResource> supporting) {
		this.primary = primary;
		this.supporting.addAll(supporting);
		// TODO: Add internal supporting resources
		//  - runtime resource
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
}
