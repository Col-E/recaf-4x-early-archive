package software.coley.recaf.workspace;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.coley.recaf.workspace.io.WorkspaceExportOptions;
import software.coley.recaf.workspace.io.WorkspaceExporter;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.resource.WorkspaceDirectoryResource;
import software.coley.recaf.workspace.model.resource.WorkspaceFileResource;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Manager module handle exporting {@link Workspace} instances to {@link Path}s.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class PathExportingManager {
	private final WorkspaceManager workspaceManager;

	@Inject
	public PathExportingManager(WorkspaceManager workspaceManager) {
		this.workspaceManager = workspaceManager;
	}

	/**
	 * Export the current workspace.
	 */
	public void exportCurrent() {
		// Validate current workspace.
		Workspace current = workspaceManager.getCurrent();
		if (current == null) throw new IllegalStateException("Tried to export when no workspace was active!");

		// And export it.
		export(current);
	}

	/**
	 * Export the given workspace.
	 *
	 * @param workspace
	 * 		Workspace to export.
	 */
	public void export(Workspace workspace) {
		/*
		// TODO: Make configurable
		WorkspaceExportOptions.CompressType compression = WorkspaceExportOptions.CompressType.MATCH_ORIGINAL;

		WorkspaceResource primaryResource = workspace.getPrimaryResource();
		WorkspaceExportOptions options;
		if (primaryResource instanceof WorkspaceDirectoryResource) {
			options = new WorkspaceExportOptions(WorkspaceExportOptions.OutputType.DIRECTORY, path);
		} else if (primaryResource instanceof WorkspaceFileResource) {
			options = new WorkspaceExportOptions(compression, WorkspaceExportOptions.OutputType.FILE, path);
		} else {
			options = new WorkspaceExportOptions(compression, WorkspaceExportOptions.OutputType.FILE, path);
		}

		// Export the workspace to the selected path.
		WorkspaceExporter exporter = workspaceManager.createExporter(options);
		try {
			exporter.export(workspace);
		} catch (IOException ex) {
			// TODO: Handle
		}*/
	}
}
