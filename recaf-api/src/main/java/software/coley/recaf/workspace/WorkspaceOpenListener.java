package software.coley.recaf.workspace;

import software.coley.recaf.workspace.model.Workspace;

/**
 * Listener for when new workspaces are opened.
 *
 * @author Matt Coley
 */
public interface WorkspaceOpenListener {
	/**
	 * Called when {@link WorkspaceManager#setCurrent(Workspace)} passes.
	 *
	 * @param workspace
	 * 		New workspace assigned.
	 */
	void onWorkspaceOpened(Workspace workspace);
}