package software.coley.recaf.workspace.model.resource;

import software.coley.recaf.info.FileInfo;

import java.net.URI;

/**
 * Basic implementation of a workspace resource sourced from a uri.
 *
 * @author Matt Coley
 */
public class BasicWorkspaceUriResource extends BasicWorkspaceResource implements WorkspaceUriResource {
	private final URI uri;
	private final FileInfo fileInfo;

	/**
	 * @param builder
	 * 		Builder to pull info from.
	 */
	public BasicWorkspaceUriResource(WorkspaceResourceBuilder builder) {
		super(builder);
		this.uri = builder.getUri();
		this.fileInfo = builder.getFileInfo();
	}

	@Override
	public FileInfo getFileInfo() {
		return fileInfo;
	}

	@Override
	public URI getUri() {
		return uri;
	}
}
