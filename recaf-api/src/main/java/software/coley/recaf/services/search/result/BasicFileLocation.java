package software.coley.recaf.services.search.result;

import jakarta.annotation.Nonnull;
import software.coley.recaf.info.FileInfo;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.FileBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

/**
 * Basic implementation of {@link FileLocation}.
 *
 * @author Matt Coley
 */
public class BasicFileLocation extends AbstractLocation implements FileLocation {
	private final FileBundle bundle;
	private final FileInfo fileInfo;

	/**
	 * @param workspace
	 * 		Target workspace.
	 * @param resource
	 * 		Target resource.
	 * @param bundle
	 * 		Target bundle.
	 * @param fileInfo
	 * 		Target file containing the result.
	 */
	public BasicFileLocation(@Nonnull Workspace workspace, @Nonnull WorkspaceResource resource,
							 @Nonnull FileBundle bundle, @Nonnull FileInfo fileInfo) {
		super(workspace, resource);
		this.bundle = bundle;
		this.fileInfo = fileInfo;
	}

	@Override
	public FileBundle getContainingBundle() {
		return bundle;
	}

	@Override
	public FileInfo getFileInfo() {
		return fileInfo;
	}

	@Nonnull
	@Override
	public String comparableString() {
		return fileInfo.getName();
	}
}
