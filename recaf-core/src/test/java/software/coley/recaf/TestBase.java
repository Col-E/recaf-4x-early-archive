package software.coley.recaf;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import software.coley.recaf.workspace.WorkspaceManager;

/**
 * Common base for test classes using the recaf application.
 */
public class TestBase {
	protected static final Recaf recaf = Bootstrap.get();
	protected static WorkspaceManager workspaceManager;

	@BeforeAll
	static void setup() {
		workspaceManager = recaf.proxy(WorkspaceManager.class);
	}

	@AfterAll
	static void cleanup() {
		// Close any open workspace from tests
		workspaceManager.setCurrentIgnoringConditions(null);
	}
}