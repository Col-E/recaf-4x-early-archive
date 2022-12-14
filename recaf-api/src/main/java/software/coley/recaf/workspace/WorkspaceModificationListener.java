package software.coley.recaf.workspace;

import software.coley.recaf.cdi.AutoRegisterWorkspaceListeners;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

/**
 * Listener for receiving workspace update events.
 *
 * @author Matt Coley
 * @see AutoRegisterWorkspaceListeners Can be used to automatically register components implementing this type.
 */
public interface WorkspaceModificationListener {
	/**
	 * @param workspace
	 * 		The workspace.
	 * @param library
	 * 		Library added to the workspace.
	 */
	void onAddLibrary(Workspace workspace, WorkspaceResource library);

	/**
	 * @param workspace
	 * 		The workspace.
	 * @param library
	 * 		Library removed from the workspace.
	 */
	void onRemoveLibrary(Workspace workspace, WorkspaceResource library);
}