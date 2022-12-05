package software.coley.recaf.workspace;

import software.coley.recaf.workspace.model.resource.WorkspaceResource;
import software.coley.recaf.workspace.model.Workspace;

/**
 * Listener for receiving workspace update events.
 *
 * @author Matt Coley
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