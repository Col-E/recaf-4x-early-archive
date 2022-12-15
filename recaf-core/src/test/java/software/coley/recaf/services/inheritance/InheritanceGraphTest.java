package software.coley.recaf.services.inheritance;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.coley.recaf.TestBase;
import software.coley.recaf.test.TestClassUtils;
import software.coley.recaf.test.dummy.Inheritance;
import software.coley.recaf.util.Types;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link InheritanceGraph}
 */
class InheritanceGraphTest extends TestBase {
	static Workspace workspace;
	static InheritanceGraph graph;

	@BeforeAll
	static void setup() throws IOException {
		// Create workspace with the inheritance classes
		JvmClassBundle classes = TestClassUtils.fromClasses(Inheritance.class.getClasses());
		assertEquals(5, classes.size(), "Expecting 5 classes");
		workspace = TestClassUtils.fromBundle(classes);
		workspaceManager.setCurrentIgnoringConditions(workspace);

		// Get graph
		assertTrue(recaf.isAvailable(InheritanceGraph.class), "Graph should be available after setting workspace");
		graph = recaf.get(InheritanceGraph.class);
	}

	@Test
	void getVertex() {
		String appleName = Inheritance.Apple.class.getName().replace('.', '/');

		// Check vertex
		InheritanceVertex vertex = graph.getVertex(appleName);
		assertNotNull(vertex, "Could not get Apple vertex from workspace");
		assertEquals(appleName, vertex.getName(), "Vertex should have same name as lookup");

		// Check children
		Set<InheritanceVertex> children = vertex.getChildren();
		assertEquals(1, children.size(), "Expecting 1 child for Apple (apple with worm)");
		assertTrue(children.stream()
				.map(InheritanceVertex::getName)
				.anyMatch(name -> name.equals(appleName + "WithWorm")));

		// Check parents
		Set<InheritanceVertex> parents = vertex.getParents();
		assertEquals(2, parents.size(), "Expecting 3 parents for Apple (interfaces, super is object and is ignored)");
		assertTrue(parents.stream()
						.map(InheritanceVertex::getName)
						.anyMatch(name -> name.equals(appleName.replace("Apple", "Red"))),
				"Apple missing parent: Red");
		assertTrue(parents.stream()
						.map(InheritanceVertex::getName)
						.anyMatch(name -> name.equals(appleName.replace("Apple", "Edible"))),
				"Apple missing parent: Edible");
	}

	@Test
	void getVertexFamily() {
		String appleName = Inheritance.Apple.class.getName().replace('.', '/');
		Set<String> names = Set.of(appleName,
				appleName + "WithWorm", // child of apple
				appleName.replace("Apple", "Red"), // parent of apple
				appleName.replace("Apple", "Edible"), // parent of apple
				appleName.replace("Apple", "Grape") // shared parent edible
		);
		Set<InheritanceVertex> family = graph.getVertexFamily(appleName);
		assertEquals(5, family.size());
		assertEquals(names, family.stream().map(InheritanceVertex::getName).collect(Collectors.toSet()));
	}

	@Test
	void getCommon() {
		String edibleName = Inheritance.Edible.class.getName().replace('.', '/');
		String appleName = Inheritance.Apple.class.getName().replace('.', '/');
		String grapeName = Inheritance.Grape.class.getName().replace('.', '/');

		// Compare obvious case --> edible
		String commonType = graph.getCommon(appleName, grapeName);
		assertEquals(edibleName, commonType, "Common type of Apple/Grape should be Edible");

		// Compare with bogus --> object
		commonType = graph.getCommon(appleName, UUID.randomUUID().toString());
		assertEquals(Types.OBJECT_TYPE.getInternalName(), commonType,
				"Common type of two unrelated classes should be Object");
	}
}