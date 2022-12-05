package software.coley.recaf.workspace.model.resource;

import jakarta.annotation.Nonnull;

import java.nio.file.Path;

/**
 * A resource sourced from a file.
 *
 * @author Matt Coley
 */
public interface WorkspaceFileResource extends WorkspaceResource {
	/**
	 * @return Path of the file the contents of this resource originate from.
	 */
	@Nonnull
	Path getFilePath();
}
