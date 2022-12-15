package software.coley.recaf.test;

import software.coley.recaf.workspace.model.BasicWorkspace;
import software.coley.recaf.workspace.model.resource.WorkspaceResourceBuilder;

/**
 * Empty workspace for testing.
 */
public class EmptyWorkspace extends BasicWorkspace {
	/**
	 * New empty workspace.
	 */
	public EmptyWorkspace() {
		super(new WorkspaceResourceBuilder().build());
	}
}
