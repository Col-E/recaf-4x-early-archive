package software.coley.recaf.services.callgraph;

import org.junit.jupiter.api.Test;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.test.TestClassUtils;
import software.coley.recaf.test.dummy.StringConsumer;
import software.coley.recaf.test.dummy.StringConsumerUser;
import software.coley.recaf.workspace.model.Workspace;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CallGraph}
 */
class CallGraphTest {
	@Test
	void testCalleeCallerRelation() throws IOException {
		Workspace workspace = TestClassUtils.fromBundle(TestClassUtils.fromClasses(
				StringConsumer.class,
				StringConsumerUser.class
		));

		JvmClassInfo mainClass = workspace.findJvmClass(StringConsumerUser.class.getName().replace('.', '/')).getItem();
		JvmClassInfo functionClass = workspace.findJvmClass(StringConsumer.class.getName().replace('.', '/')).getItem();
		assertNotNull(mainClass, "Missing main class");
		assertNotNull(functionClass, "Missing function class");

		CallGraph graph = new CallGraph(new CallGraphConfig(), workspace);
		ClassMethodsContainer containerMain = graph.getClassMethodsContainer(mainClass);
		ClassMethodsContainer containerFunction = graph.getClassMethodsContainer(functionClass);

		// Get outbound calls for main. Should just be to 'new StringConsumer()' and 'StringConsumer.accept(String)'
		MethodVertex mainVertex = containerMain.getVertex("main", "([Ljava/lang/String;)V");
		assertNotNull(mainVertex, "Missing method vertex for 'main'");
		assertEquals(2, mainVertex.getCalls().size());

		// Assert main calls 'accept'
		MethodVertex acceptVertex = containerFunction.getVertex("accept", "(Ljava/lang/String;)V");
		assertNotNull(acceptVertex, "Missing method vertex for 'accept'");
		assertTrue(acceptVertex.getCallers().contains(mainVertex));

		// Assert main calls 'new StringConsumer()'
		MethodVertex newVertex = containerFunction.getVertex("<init>", "()V");
		assertNotNull(newVertex, "Missing method vertex for '<init>'");
		assertTrue(newVertex.getCallers().contains(mainVertex));
	}

	// TODO: Test removing existing class from workspace makes calls to its methods unresolved

	// TODO: Test adding missing class to workspace makes unresolved calls valid
}