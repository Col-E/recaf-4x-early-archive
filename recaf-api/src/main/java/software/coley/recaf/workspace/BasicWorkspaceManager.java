package software.coley.recaf.workspace;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import software.coley.recaf.workspace.io.ResourceImporter;
import software.coley.recaf.workspace.model.Workspace;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic workspace manager implementation.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class BasicWorkspaceManager implements WorkspaceManager {
	private final List<WorkspaceCloseCondition> closeConditions = new ArrayList<>();
	private final List<WorkspaceOpenListener> openListeners = new ArrayList<>();
	private final List<WorkspaceCloseListener> closeListeners = new ArrayList<>();
	private final List<WorkspaceModificationListener> defaultModificationListeners = new ArrayList<>();
	private final ResourceImporter resourceImporter;
	private Workspace current;

	@Inject
	public BasicWorkspaceManager(ResourceImporter resourceImporter) {
		this.resourceImporter = resourceImporter;
	}

	@Override
	public ResourceImporter getResourceImporter() {
		return resourceImporter;
	}

	@Override
	@Produces
	@Dependent
	public Workspace getCurrent() {
		return current;
	}

	@Override
	public void setCurrentIgnoringConditions(Workspace workspace) {
		if (current != null) {
			current.close();
			getWorkspaceCloseListeners().forEach(listener -> listener.onWorkspaceClosed(current));
		}
		current = workspace;
		if (workspace != null) {
			defaultModificationListeners.forEach(workspace::addWorkspaceModificationListener);
			getWorkspaceOpenListeners().forEach(listener -> listener.onWorkspaceOpened(workspace));
		}
	}


	@Override
	public List<WorkspaceCloseCondition> getWorkspaceCloseConditions() {
		return closeConditions;
	}

	@Override
	public void addWorkspaceCloseCondition(WorkspaceCloseCondition condition) {
		closeConditions.add(condition);
	}

	@Override
	public void removeWorkspaceCloseCondition(WorkspaceCloseCondition condition) {
		closeConditions.remove(condition);
	}

	@Override
	public List<WorkspaceOpenListener> getWorkspaceOpenListeners() {
		return openListeners;
	}

	@Override
	public void addWorkspaceOpenListener(WorkspaceOpenListener listener) {
		openListeners.add(listener);
	}

	@Override
	public void removeWorkspaceOpenListener(WorkspaceOpenListener listener) {
		openListeners.remove(listener);
	}

	@Override
	public List<WorkspaceCloseListener> getWorkspaceCloseListeners() {
		return closeListeners;
	}

	@Override
	public void addWorkspaceCloseListener(WorkspaceCloseListener listener) {
		closeListeners.add(listener);
	}

	@Override
	public void removeWorkspaceCloseListener(WorkspaceCloseListener listener) {
		closeListeners.add(listener);
	}

	@Override
	public List<WorkspaceModificationListener> getDefaultWorkspaceModificationListeners() {
		return defaultModificationListeners;
	}

	@Override
	public void addDefaultWorkspaceModificationListeners(WorkspaceModificationListener listener) {
		defaultModificationListeners.add(listener);
	}

	@Override
	public void removeDefaultWorkspaceModificationListeners(WorkspaceModificationListener listener) {
		defaultModificationListeners.remove(listener);
	}
}
