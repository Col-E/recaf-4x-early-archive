package software.coley.recaf.services.parse;

import jakarta.annotation.Nonnull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.tree.J;
import software.coley.recaf.TestBase;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.path.DirectoryPathNode;
import software.coley.recaf.path.PathNode;
import software.coley.recaf.services.mapping.IntermediateMappings;
import software.coley.recaf.services.source.AstContextHelper;
import software.coley.recaf.services.source.AstMappingVisitor;
import software.coley.recaf.services.source.AstService;
import software.coley.recaf.test.TestClassUtils;
import software.coley.recaf.test.dummy.AnnotationImpl;
import software.coley.recaf.test.dummy.ClassWithExceptions;
import software.coley.recaf.test.dummy.DummyEnum;
import software.coley.recaf.test.dummy.StringSupplier;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.BasicJvmClassBundle;

import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AstService}
 */
@SuppressWarnings("DataFlowIssue")
public class AstServiceTest extends TestBase {
	static JvmClassInfo thisClass;
	static AstService service;
	static AstContextHelper helper;

	@BeforeAll
	static void setup() throws IOException {
		thisClass = TestClassUtils.fromRuntimeClass(AstServiceTest.class);
		BasicJvmClassBundle bundle = TestClassUtils.fromClasses(
				DummyEnum.class,
				StringSupplier.class,
				ClassWithExceptions.class,
				AnnotationImpl.class
		);
		Workspace workspace = TestClassUtils.fromBundle(bundle);
		helper = new AstContextHelper(workspace);
		workspaceManager.setCurrent(workspace);
		service = recaf.get(AstService.class);
	}

	@Nested
	class Resolving {
		@Test
		void testPackage() {
			String source = """
					package software.coley.recaf.test.dummy;
					enum DummyEnum {}
					""";

			handle(source, (unit, ctx) -> {
				PathNode<?> path;

				// Entire range of package should be recognized
				for (int i = 0; i < source.indexOf(';'); i++) {
					path = helper.resolve(unit, i);
					if (path instanceof DirectoryPathNode packagePath) {
						assertEquals("software/coley/recaf/test/dummy", packagePath.getValue());
					} else {
						fail("Failed to identify package");
					}
				}
			});
		}

		@Test
		void testPackage_NoEndingSemicolon() {
			String source = """
					package software.coley.recaf.test.dummy\t
					enum DummyEnum {}
					""";

			handle(source, (unit, ctx) -> {
				PathNode<?> path;

				// Entire range of package should be recognized
				for (int i = 0; i < source.indexOf('\t'); i++) {
					path = helper.resolve(unit, i);
					if (path instanceof DirectoryPathNode packagePath) {
						assertEquals("software/coley/recaf/test/dummy", packagePath.getValue());
					} else {
						fail("Failed to identify package");
					}
				}
			});
		}
	}

	@Nested
	class Mapping {
		@Test
		void renameClassReplacesPackage() {
			String source = """
					package software.coley.recaf.test.dummy;
					
					enum DummyEnum {
						ONE, TWO, THREE
					}
					""";
			handle(source, (unit, ctx) -> {
				IntermediateMappings mappings = new IntermediateMappings();
				mappings.addClass(DummyEnum.class.getName().replace('.', '/'), "com/example/MyEnum");
				AstMappingVisitor visitor = new AstMappingVisitor(mappings);

				String modified = unit.acceptJava(visitor, ctx).print(new Cursor(null, unit));
				assertTrue(modified.startsWith("package com.example;"));
				assertTrue(modified.contains("enum MyEnum {"));
			});
		}
	}

	private static void handle(String source, BiConsumer<J.CompilationUnit, ExecutionContext> consumer) {
		InMemoryExecutionContext context = new InMemoryExecutionContext(Throwable::printStackTrace);
		List<J.CompilationUnit> units = parser().parse(context, source);
		assertEquals(1, units.size());
		if (consumer != null)
			consumer.accept(units.get(0), context);
	}

	@Nonnull
	private static JavaParser parser() {
		// Since this extracts references from the passed info, passing this test class should yield a path
		// that includes everything in the bundle.
		return service.newParser(thisClass);
	}
}
