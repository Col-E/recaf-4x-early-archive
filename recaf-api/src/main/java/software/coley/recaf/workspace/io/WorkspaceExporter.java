package software.coley.recaf.workspace.io;

import software.coley.recaf.workspace.WorkspaceManager;
import software.coley.recaf.workspace.model.Workspace;

/**
 * Outline for supporting exporting of {@link Workspace} back into files, based on some
 * {@link #getExportOptions() configured options} given by {@link WorkspaceManager#createExporter(WorkspaceExportOptions)}.
 *
 * @author Matt Coley
 */
public interface WorkspaceExporter {
	/**
	 * @return Exporting options, includes details on where to export, how to repackage content, etc.
	 */
	WorkspaceExportOptions getExportOptions();

	/**
	 * @param workspace
	 * 		The workspace to export.
	 */
	void export(Workspace workspace);
}
