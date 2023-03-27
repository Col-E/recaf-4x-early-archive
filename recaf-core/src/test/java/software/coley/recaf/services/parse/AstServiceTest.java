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
import software.coley.recaf.test.dummy.*;
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
				HelloWorld.class,
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

		// TODO: Casts, imports, import types of static imports, member of static imports
		//  arrays, new-array,
		//  field declarations
		//  method declarations, constructors, static block
		//  static-field qualifier, field qualifier, field reference
		//  static-method qualifier, method qualifier, method reference
	}

	@Nested
	class Mapping {
		@Test
		void renameClass_ReplacesPackage() {
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

		@Test
		void renameClass_ReplacesCast() {
			String source = """
					package software.coley.recaf.test.dummy;
										
					class HelloWorld {
						static void main(Object arg) {
							HelloWorld casted = (HelloWorld) arg;
						}
					}
					""";
			handle(source, (unit, ctx) -> {
				IntermediateMappings mappings = new IntermediateMappings();
				mappings.addClass(HelloWorld.class.getName().replace('.', '/'), "com/example/Howdy");
				AstMappingVisitor visitor = new AstMappingVisitor(mappings);

				String modified = unit.acceptJava(visitor, ctx).print(new Cursor(null, unit));
				assertTrue(modified.startsWith("package com.example;"));
				assertTrue(modified.contains("class Howdy {"));
				assertTrue(modified.contains("Howdy casted = (Howdy) arg;"));
			});
		}

		@Test
		void renameClass_ReplaceStaticCallContextInSamePackage() {
			String source = """
					package software.coley.recaf.test.dummy;
										
					class HelloWorld {
						static void main(String[] args) {
							ClassWithExceptions.readInt(args[0]);
						}
					}
					""";
			handle(source, (unit, ctx) -> {
				IntermediateMappings mappings = new IntermediateMappings();
				mappings.addClass(ClassWithExceptions.class.getName().replace('.', '/'), "com/example/Call");
				AstMappingVisitor visitor = new AstMappingVisitor(mappings);

				String modified = unit.acceptJava(visitor, ctx).print(new Cursor(null, unit));
				assertTrue(modified.contains("Call.readInt("));
			});
		}

		@Test
		void renameClass_ReplaceImportOfStaticCall() {
			String source = """
					package software.coley.recaf.test.dummy;
										
					import static software.coley.recaf.test.dummy.ClassWithExceptions.readInt;
										
					class HelloWorld {
						static void main(String[] args) {
							readInt(args[0]);
						}
					}
					""";
			handle(source, (unit, ctx) -> {
				IntermediateMappings mappings = new IntermediateMappings();
				mappings.addClass(ClassWithExceptions.class.getName().replace('.', '/'), "com/example/Call");
				AstMappingVisitor visitor = new AstMappingVisitor(mappings);

				String modified = unit.acceptJava(visitor, ctx).print(new Cursor(null, unit));
				assertTrue(modified.contains("import static com.example.Call.readInt"));
			});
		}

		@Test
		void renameClass_ArrayDecAndNew() {
			String source = """
					package software.coley.recaf.test.dummy;
										
					class HelloWorld {
						static void main(String[] args) {
							ClassWithExceptions[] bar = new ClassWithExceptions[0];
						}
					}
					""";
			handle(source, (unit, ctx) -> {
				IntermediateMappings mappings = new IntermediateMappings();
				mappings.addClass(ClassWithExceptions.class.getName().replace('.', '/'), "com/example/Foo");
				AstMappingVisitor visitor = new AstMappingVisitor(mappings);

				String modified = unit.acceptJava(visitor, ctx).print(new Cursor(null, unit));
				assertTrue(modified.contains("Foo[] bar = new Foo[0];"));
			});
		}

		@Test
		void renameClass_FieldStaticQualifier() {
			String source = """
					package software.coley.recaf.test.dummy;
										
					class HelloWorld {
						static void main(String[] args) {
							DummyEnum one = DummyEnum.ONE;
							String two = DummyEnum.valueOf("TWO").name();
						}
					}
					""";
			handle(source, (unit, ctx) -> {
				IntermediateMappings mappings = new IntermediateMappings();
				mappings.addClass(DummyEnum.class.getName().replace('.', '/'), "com/example/Singleton");
				AstMappingVisitor visitor = new AstMappingVisitor(mappings);

				String modified = unit.acceptJava(visitor, ctx).print(new Cursor(null, unit));
				assertTrue(modified.contains("Singleton one = Singleton.ONE;"));
				assertTrue(modified.contains("String two = Singleton.valueOf(\"TWO\").name();"));
			});
		}

		@Test
		void renameClass_FieldAndVariableDeclarations() {
			String source = """
					package software.coley.recaf.test.dummy;
										
					class HelloWorld {
						ClassWithExceptions[] array;
						ClassWithExceptions single;
										
						static void main(String[] args) {
							ClassWithExceptions[] local_array = null;
							ClassWithExceptions local_single = null;
						}
					}
					""";
			handle(source, (unit, ctx) -> {
				IntermediateMappings mappings = new IntermediateMappings();
				mappings.addClass(ClassWithExceptions.class.getName().replace('.', '/'), "com/example/Foo");
				AstMappingVisitor visitor = new AstMappingVisitor(mappings);

				String modified = unit.acceptJava(visitor, ctx).print(new Cursor(null, unit));
				assertTrue(modified.contains("Foo[] array;"));
				assertTrue(modified.contains("Foo single;"));
				assertTrue(modified.contains("Foo[] local_array = null;"));
				assertTrue(modified.contains("Foo local_single = null;"));
			});
		}

		@Test
		void renameClass_MethodReturnAndArgs() {
			String source = """
					package software.coley.recaf.test.dummy;
										
					class HelloWorld {
						static HelloWorld get() { return null; }
						void accept(HelloWorld arg) {}
					}
					""";
			handle(source, (unit, ctx) -> {
				IntermediateMappings mappings = new IntermediateMappings();
				mappings.addClass(HelloWorld.class.getName().replace('.', '/'), "com/example/Howdy");
				AstMappingVisitor visitor = new AstMappingVisitor(mappings);

				String modified = unit.acceptJava(visitor, ctx).print(new Cursor(null, unit));
				assertTrue(modified.contains("static Howdy get()"));
				assertTrue(modified.contains("void accept(Howdy arg)"));
			});
		}

		@Test
		void renameClass_Constructor() {
			String source = """
					package software.coley.recaf.test.dummy;
										
					class HelloWorld {
						private HelloWorld(String s) {}
						HelloWorld() {}
					}
					""";
			handle(source, (unit, ctx) -> {
				IntermediateMappings mappings = new IntermediateMappings();
				mappings.addClass(HelloWorld.class.getName().replace('.', '/'), "com/example/Howdy");
				AstMappingVisitor visitor = new AstMappingVisitor(mappings);

				String modified = unit.acceptJava(visitor, ctx).print(new Cursor(null, unit));
				assertTrue(modified.contains("private Howdy(String s) {}"));
				assertTrue(modified.contains("Howdy() {}"));
			});
		}

		@Test
		void renameClass_MethodReference() {
			String source = """
					package software.coley.recaf.test.dummy;
										
					import java.util.function.Supplier;
										
					class HelloWorld {
						static {
							Supplier<HelloWorld> worldSupplier = HelloWorld::new;
						}
					}
					""";
			handle(source, (unit, ctx) -> {
				IntermediateMappings mappings = new IntermediateMappings();
				mappings.addClass(HelloWorld.class.getName().replace('.', '/'), "com/example/Howdy");
				AstMappingVisitor visitor = new AstMappingVisitor(mappings);

				String modified = unit.acceptJava(visitor, ctx).print(new Cursor(null, unit));
				assertTrue(modified.contains("Supplier<Howdy> worldSupplier = Howdy::new"));
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
