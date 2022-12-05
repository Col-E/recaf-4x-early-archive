package software.coley.recaf.workspace.model.resource;

import java.nio.file.Path;

/**
 * Basic implementation of a workspace resource sourced from a file.
 *
 * @author Matt Coley
 */
public class BasicWorkspaceFileResource extends BasicWorkspaceResource implements WorkspaceFileResource {
	private final Path filePath;

	/**
	 * @param builder
	 * 		Builder to pull info from.
	 */
	public BasicWorkspaceFileResource(WorkspaceResourceBuilder builder) {
		super(builder);
		this.filePath = builder.getFilePath();
	}

	@Override
	public Path getFilePath() {
		return filePath;
	}
}
