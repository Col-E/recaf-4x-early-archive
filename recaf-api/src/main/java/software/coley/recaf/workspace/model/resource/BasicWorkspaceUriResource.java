package software.coley.recaf.workspace.model.resource;

import java.net.URI;

/**
 * Basic implementation of a workspace resource sourced from a uri.
 *
 * @author Matt Coley
 */
public class BasicWorkspaceUriResource extends BasicWorkspaceResource implements WorkspaceUriResource {
	private final URI uri;

	/**
	 * @param builder
	 * 		Builder to pull info from.
	 */
	public BasicWorkspaceUriResource(WorkspaceResourceBuilder builder) {
		super(builder);
		this.uri = builder.getUri();
	}

	@Override
	public URI getUri() {
		return uri;
	}
}
