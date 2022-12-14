package software.coley.recaf;

import jakarta.enterprise.inject.Instance;
import org.junit.jupiter.api.Test;
import software.coley.recaf.test.TestClassUtils;
import software.coley.recaf.test.dummy.HelloWorld;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Bootstrap}
 */
class BootstrapTest extends TestBase {
	@Test
	void testGetApplicationScopedInstance() {
		assertNotNull(workspaceManager, "Failed to get instance of workspace manager, which should be application-scoped");
	}

	@Test
	void testGetDependentScopeInstance() throws IOException {
		assertNull(workspaceManager.getCurrent(), "No workspace should be assigned!");

		// Create workspace with single class
		JvmClassBundle classes = TestClassUtils.fromClasses(HelloWorld.class);
		Workspace workspace = TestClassUtils.fromBundle(classes);

		// Should be null since nothing is active in the workspace manager.
		// Thus, the supplier method should yield 'null'.
		Instance<Workspace> currentWorkspaceInstance = recaf.instance(Workspace.class);
		assertNull(currentWorkspaceInstance.get(), "No workspace should be available while there is no");

		// Assign a workspace.
		workspaceManager.setCurrentIgnoringConditions(workspace);

		// Should no longer be null.
		assertEquals(workspace, currentWorkspaceInstance.get(),
				"Workspace manager should expose current workspace as dependent scoped bean");
	}
}
