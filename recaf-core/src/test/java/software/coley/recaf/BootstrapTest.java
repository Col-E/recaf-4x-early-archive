package software.coley.recaf;

import jakarta.enterprise.inject.Instance;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.coley.recaf.services.inheritance.InheritanceGraph;
import software.coley.recaf.test.EmptyWorkspace;
import software.coley.recaf.test.TestClassUtils;
import software.coley.recaf.test.dummy.HelloWorld;
import software.coley.recaf.workspace.model.BasicWorkspace;
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
	void testGetWorkspaceInstance() throws IOException {
		assertNull(workspaceManager.getCurrent(), "No workspace should be assigned!");

		// Create workspace with single class
		JvmClassBundle classes = TestClassUtils.fromClasses(HelloWorld.class);
		Workspace workspace = TestClassUtils.fromBundle(classes);

		// Should be null since nothing is active in the workspace manager.
		// Thus, the supplier method should yield 'null'.
		Instance<Workspace> currentWorkspaceInstance = recaf.instance(Workspace.class);
		assertNull(currentWorkspaceInstance.get(), "No workspace should be available for injection " +
				"while there is no current workspace");

		// Assign a workspace.
		workspaceManager.setCurrentIgnoringConditions(workspace);

		// Should no longer be null.
		assertEquals(workspace, currentWorkspaceInstance.get(),
				"Workspace manager should expose current workspace as dependent scoped bean");
	}

	@Test
	void testGetWorkspaceScopedInstance() {
		// Get the graph when one workspace is open.
		workspaceManager.setCurrentIgnoringConditions(new EmptyWorkspace());
		InheritanceGraph graph1 = recaf.getAndCreate(InheritanceGraph.class);
		InheritanceGraph graph2 = recaf.getAndCreate(InheritanceGraph.class);
		assertSame(graph1, graph2, "Graph should be workspace-scoped, but values differ!");

		// Assign a new workspace.
		// The graph should be different since the prior workspace is closed.
		workspaceManager.setCurrentIgnoringConditions(new EmptyWorkspace());
		InheritanceGraph graph3 = recaf.getAndCreate(InheritanceGraph.class);
		InheritanceGraph graph4 = recaf.getAndCreate(InheritanceGraph.class);
		assertSame(graph3, graph4, "Graph should be workspace-scoped, but values differ!");
		assertNotSame(graph1, graph3, "Graph scope from before/after a new workspace yielded the same graph bean!");
	}
}
