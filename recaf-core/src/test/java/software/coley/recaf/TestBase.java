package software.coley.recaf;

import org.junit.jupiter.api.BeforeAll;
import software.coley.recaf.workspace.WorkspaceManager;

/**
 * Common base for test classes using the recaf application.
 */
public class TestBase {
	protected static final Recaf recaf = Bootstrap.get();
	protected static WorkspaceManager workspaceManager;

	@BeforeAll
	public static void setupWorkspaceManager() {
		workspaceManager = recaf.get(WorkspaceManager.class);
	}
}