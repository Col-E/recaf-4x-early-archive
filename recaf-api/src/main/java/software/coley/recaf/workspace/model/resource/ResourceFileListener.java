package software.coley.recaf.workspace.model.resource;

import software.coley.recaf.info.FileInfo;
import software.coley.recaf.workspace.model.bundle.FileBundle;

/**
 * Listener for handling updates to {@link FileInfo} values within a {@link FileBundle}
 * contained in a {@link WorkspaceResource}.
 *
 * @author Matt Coley
 */
public interface ResourceFileListener {
	/**
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param file
	 * 		The new file.
	 */
	void onNewFile(WorkspaceResource resource, FileBundle bundle, FileInfo file);

	/**
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param oldFile
	 * 		The old file value.
	 * @param newFile
	 * 		The new file value.
	 */
	void onUpdateFile(WorkspaceResource resource, FileBundle bundle, FileInfo oldFile, FileInfo newFile);

	/**
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param file
	 * 		The removed file.
	 */
	void onRemoveFile(WorkspaceResource resource, FileBundle bundle, FileInfo file);
}
