package software.coley.recaf.workspace.model.resource;

import software.coley.recaf.info.FileInfo;

import java.nio.file.Path;

/**
 * Basic implementation of a workspace resource sourced from a file.
 *
 * @author Matt Coley
 */
public class BasicWorkspaceFileResource extends BasicWorkspaceResource implements WorkspaceFileResource {
	private final Path filePath;
	private final FileInfo fileInfo;

	/**
	 * @param builder
	 * 		Builder to pull info from.
	 */
	public BasicWorkspaceFileResource(WorkspaceResourceBuilder builder) {
		super(builder);
		this.filePath = builder.getFilePath();
		this.fileInfo = null; // TODO: Parse data from file path, throw IOException on failure
	}

	@Override
	public FileInfo getFileInfo() {
		return fileInfo;
	}

	@Override
	public Path getFilePath() {
		return filePath;
	}
}
