package software.coley.recaf.workspace;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.recaf.workspace.model.BasicWorkspace;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.util.Collections;
import java.util.List;

/**
 * Manages the current workspace and creation of new ones.
 *
 * @author Matt Coley
 */
public interface WorkspaceManager {
	/**
	 * @return The current active workspace.
	 */
	@Nullable
	Workspace getCurrent();

	/**
	 * @param workspace
	 * 		New workspace to set as the active workspace.
	 *
	 * @return {@code true} when the workspace assignment is a success.
	 * {@code false} if the assignment was blocked for some reason.
	 */
	default boolean setCurrent(@Nullable Workspace workspace) {
		Workspace current = getCurrent();
		if (current == null) {
			// If there is no current workspace, then just assign it.
			setCurrentIgnoringConditions(workspace);
			if (workspace != null)
				getWorkspaceOpenListeners().forEach(listener -> listener.onWorkspaceOpened(workspace));
			return true;
		} else if (getWorkspaceCloseConditions().stream()
				.allMatch(condition -> condition.canClose(current))) {
			// Otherwise, check if the conditions allow for closing the prior workspace.
			// If so, then assign the new workspace.
			current.close();
			getWorkspaceCloseListeners().forEach(listener -> listener.onWorkspaceClosed(current));
			setCurrentIgnoringConditions(workspace);
			if (workspace != null)
				getWorkspaceOpenListeners().forEach(listener -> listener.onWorkspaceOpened(workspace));
			return true;
		}
		// Workspace closure conditions not met, assignment denied.
		return false;
	}

	/**
	 * Effectively {@link #setCurrent(Workspace)} except any blocking conditions are bypassed.
	 *
	 * @param workspace
	 * 		New workspace to set as the active workspace.
	 */
	void setCurrentIgnoringConditions(@Nullable Workspace workspace);

	/**
	 * @param primary
	 * 		Primary resource for editing.
	 *
	 * @return New workspace of resource.
	 */
	@Nonnull
	default Workspace createWorkspace(@Nonnull WorkspaceResource primary) {
		return createWorkspace(primary, Collections.emptyList());
	}

	/**
	 * @param primary
	 * 		Primary resource for editing.
	 * @param libraries
	 * 		Supporting resources.
	 *
	 * @return New workspace of resources
	 */
	@Nonnull
	default Workspace createWorkspace(@Nonnull WorkspaceResource primary, @Nonnull List<WorkspaceResource> libraries) {
		return new BasicWorkspace(primary, libraries);
	}

	/**
	 * @return Conditions in the manager that can prevent {@link #setCurrent(Workspace)} from going through.
	 */
	@Nonnull
	List<WorkspaceCloseCondition> getWorkspaceCloseConditions();

	/**
	 * @param condition
	 * 		Condition to add.
	 */
	void addWorkspaceCloseCondition(WorkspaceCloseCondition condition);

	/**
	 * @param condition
	 * 		Condition to remove.
	 */
	void removeWorkspaceCloseCondition(WorkspaceCloseCondition condition);

	/**
	 * @return Listeners for when a new workspace is assigned as the current one.
	 */
	@Nonnull
	List<WorkspaceOpenListener> getWorkspaceOpenListeners();

	/**
	 * @param listener
	 * 		Listener to add.
	 */
	void addWorkspaceOpenListener(WorkspaceOpenListener listener);

	/**
	 * @param listener
	 * 		Listener to remove.
	 */
	void removeWorkspaceOpenListener(WorkspaceOpenListener listener);

	/**
	 * @return Listeners for when the current workspace is removed as being current.
	 */
	@Nonnull
	List<WorkspaceCloseListener> getWorkspaceCloseListeners();

	/**
	 * @param listener
	 * 		Listener to add.
	 */
	void addWorkspaceCloseListener(WorkspaceCloseListener listener);

	/**
	 * @param listener
	 * 		Listener to remove.
	 */
	void removeWorkspaceCloseListener(WorkspaceCloseListener listener);
}
