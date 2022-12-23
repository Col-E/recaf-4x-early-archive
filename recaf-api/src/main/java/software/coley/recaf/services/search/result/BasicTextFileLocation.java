package software.coley.recaf.services.search.result;

import jakarta.annotation.Nonnull;
import software.coley.recaf.info.FileInfo;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.FileBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

/**
 * Basic implementation of {@link TextFileLocation}.
 *
 * @author Matt Coley
 */
public class BasicTextFileLocation extends BasicFileLocation implements TextFileLocation {
	private final int lineNumber;

	/**
	 * @param workspace
	 * 		Target workspace.
	 * @param resource
	 * 		Target resource.
	 * @param bundle
	 * 		Target bundle.
	 * @param fileInfo
	 * 		Target file containing the result.
	 * @param lineNumber
	 * 		The line number of the matched text.
	 */
	public BasicTextFileLocation(@Nonnull Workspace workspace, @Nonnull WorkspaceResource resource,
								 @Nonnull FileBundle bundle, @Nonnull FileInfo fileInfo, int lineNumber) {
		super(workspace, resource, bundle, fileInfo);
		this.lineNumber = lineNumber;
	}

	@Override
	public int getLineNumber() {
		return lineNumber;
	}
}
