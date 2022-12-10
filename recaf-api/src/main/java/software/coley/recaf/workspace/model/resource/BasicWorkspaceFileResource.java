package software.coley.recaf.workspace.model.resource;

import software.coley.recaf.info.FileInfo;

import java.nio.file.Path;

/**
 * Basic implementation of a workspace resource sourced from a file.
 *
 * @author Matt Coley
 */
public class BasicWorkspaceFileResource extends BasicWorkspaceResource implements WorkspaceFileResource {
	private final FileInfo fileInfo;

	/**
	 * @param builder
	 * 		Builder to pull info from.
	 */
	public BasicWorkspaceFileResource(WorkspaceFileResourceBuilder builder) {
		super(builder);
		this.fileInfo = builder.getFileInfo();
	}

	@Override
	public FileInfo getFileInfo() {
		return fileInfo;
	}
}
