package software.coley.recaf.services.source;

import jakarta.annotation.Nonnull;
import org.junit.jupiter.api.*;
import org.objectweb.asm.Type;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.tree.J;
import software.coley.recaf.TestBase;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.info.member.ClassMember;
import software.coley.recaf.path.ClassMemberPathNode;
import software.coley.recaf.path.ClassPathNode;
import software.coley.recaf.path.DirectoryPathNode;
import software.coley.recaf.path.PathNode;
import software.coley.recaf.services.mapping.IntermediateMappings;
import software.coley.recaf.test.TestClassUtils;
import software.coley.recaf.test.dummy.*;
import software.coley.recaf.util.Types;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.BasicJvmClassBundle;

import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AstService}
 */
@SuppressWarnings("DataFlowIssue")
public class AstServiceTest extends TestBase {
	static JvmClassInfo thisClass;
	static AstService service;
	static AstContextHelper helper;
	static JavaParser parser;

	@BeforeAll
	static void setup() throws IOException {
		thisClass = TestClassUtils.fromRuntimeClass(AstServiceTest.class);
		BasicJvmClassBundle bundle = TestClassUtils.fromClasses(
				DummyEnum.class,
				StringSupplier.class,
				ClassWithConstructor.class,
				ClassWithExceptions.class,
				ClassWithStaticInit.class,
				HelloWorld.class,
				Types.class,
				Type.class,
				AnnotationImpl.class
		);
		Workspace workspace = TestClassUtils.fromBundle(bundle);
		helper = new AstContextHelper(workspace);
		workspaceManager.setCurrent(workspace);
		service = recaf.get(AstService.class);
		parser = service.newParser(thisClass);
	}

	@AfterEach
	void cleanup() {
		// Flush in-memory caches.
		parser.reset();

		// For some reason, you need to re-allocate the parser to actually gain the full benefits
		// of the prior cache flush.
		parser = service.newParser(thisClass);
	}

	@Nested
	class Resolving {
		@Test
		void testPackage() {
			String source = """
					package software.coley.recaf.test.dummy;
					enum DummyEnum {}
					""";
			handleUnit(source, (unit, ctx) -> {
				validateRange(unit, source, "package software.coley.recaf.test.dummy;", DirectoryPathNode.class, packagePath -> {
					assertEquals("software/coley/recaf/test/dummy", packagePath.getValue());
				});
			});
		}

		@Test
		void testPackage_NoEndingSemicolon() {
			String source = """
					package software.coley.recaf.test.dummy\t
					enum DummyEnum {}
					""";
			handleUnit(source, (unit, ctx) -> {
				validateRange(unit, source, "package software.coley.recaf.test.dummy", DirectoryPathNode.class, packagePath -> {
					assertEquals("software/coley/recaf/test/dummy", packagePath.getValue());
				});
			});
		}

		@Test
		void testImport() {
			String source = """
					package software.coley.recaf.test.dummy;
					import java.io.File;
					import software.coley.recaf.test.dummy.DummyEnum;
					class HelloWorld {}
					""";
			handleUnit(source, (unit, ctx) -> {
				validateRange(unit, source, "import java.io.File;", ClassPathNode.class, classPath -> {
					assertEquals("java/io/File", classPath.getValue().getName());
				});
				validateRange(unit, source, "import software.coley.recaf.test.dummy.DummyEnum;", ClassPathNode.class, classPath -> {
					assertEquals("software/coley/recaf/test/dummy/DummyEnum", classPath.getValue().getName());
				});
			});
		}

		@Test
		void testImportStaticMember() {
			String source = """
					package software.coley.recaf.test.dummy;
					import static software.coley.recaf.test.dummy.DummyEnum.ONE;
					class HelloWorld {}
					""";
			handleUnit(source, (unit, ctx) -> {
				// The 'DummyEnum' takes precedence
				validateRange(unit, source, "DummyEnum", ClassPathNode.class, classPath -> {
					assertEquals("software/coley/recaf/test/dummy/DummyEnum", classPath.getValue().getName());
				});

				// The static import takes precedence
				validateRange(unit, source, "import static software.coley.recaf.test.dummy.", ClassMemberPathNode.class, memberPath -> {
					assertEquals("software/coley/recaf/test/dummy/DummyEnum",
							memberPath.getValueOfType(ClassInfo.class).getName());
					assertEquals("ONE", memberPath.getValue().getName());
				});
				validateRange(unit, source, "ONE;", ClassMemberPathNode.class, memberPath -> {
					assertEquals("software/coley/recaf/test/dummy/DummyEnum",
							memberPath.getValueOfType(ClassInfo.class).getName());
					assertEquals("ONE", memberPath.getValue().getName());
				});
			});
		}

		@Test
		void testClassDeclaration() {
			String source = """
					package software.coley.recaf.test.dummy;
					class HelloWorld {}
					""";
			handleUnit(source, (unit, ctx) -> {
				validateRange(unit, source, "class HelloWorld", ClassPathNode.class, classPath -> {
					assertEquals("software/coley/recaf/test/dummy/HelloWorld", classPath.getValue().getName());
				});
			});
		}

		@Test
		void testInterfaceDeclaration() {
			String source = """
					package software.coley.recaf.test.dummy;
					interface HelloWorld {}
					""";
			handleUnit(source, (unit, ctx) -> {
				validateRange(unit, source, "interface HelloWorld", ClassPathNode.class, classPath -> {
					assertEquals("software/coley/recaf/test/dummy/HelloWorld", classPath.getValue().getName());
				});
			});
		}

		@Test
		void testEnumDeclaration() {
			String source = """
					package software.coley.recaf.test.dummy;
					enum HelloWorld {}
					""";
			handleUnit(source, (unit, ctx) -> {
				validateRange(unit, source, "enum HelloWorld", ClassPathNode.class, classPath -> {
					assertEquals("software/coley/recaf/test/dummy/HelloWorld", classPath.getValue().getName());
				});
			});
		}

		@Test
		void testRecordDeclaration() {
			String source = """
					package software.coley.recaf.test.dummy;
					record HelloWorld() {}
					""";
			handleUnit(source, (unit, ctx) -> {
				validateRange(unit, source, "record HelloWorld", ClassPathNode.class, classPath -> {
					assertEquals("software/coley/recaf/test/dummy/HelloWorld", classPath.getValue().getName());
				});
			});
		}

		@Test
		void testFieldDeclaration_Normal() {
			String source = """
					package software.coley.recaf.util;
					import org.objectweb.asm.Type;
					public class Types {
					  	public static final Type OBJECT_TYPE = Type.getObjectType("java/lang/Object");
					  	public static final Type STRING_TYPE = new Type();
					  	private static final Type[] PRIMITIVES = new Type[0];
					}
					""";
			handleUnit(source, (unit, ctx) -> {
				validateRange(unit, source, "OBJECT_TYPE", ClassMemberPathNode.class, memberPath -> {
					ClassMember member = memberPath.getValue();
					assertEquals("OBJECT_TYPE", member.getName());
				});
				validateRange(unit, source, "STRING_TYPE", ClassMemberPathNode.class, memberPath -> {
					ClassMember member = memberPath.getValue();
					assertEquals("STRING_TYPE", member.getName());
				});
				validateRange(unit, source, "PRIMITIVES", ClassMemberPathNode.class, memberPath -> {
					ClassMember member = memberPath.getValue();
					assertEquals("PRIMITIVES", member.getName());
				});
				validateRange(unit, source, "getObjectType", ClassMemberPathNode.class, memberPath -> {
					ClassMember member = memberPath.getValue();
					assertEquals("getObjectType", member.getName());
				});
				validateRange(unit, source, "new Type[0]", ClassPathNode.class, classPath -> {
					assertEquals("org/objectweb/asm/Type", classPath.getValue().getName());
				});
			});
		}

		@Test
		void testFieldDeclaration_EnumConstant() {
			String source = """
					package software.coley.recaf.test.dummy;
					public enum DummyEnum {
					 	ONE,
					 	TWO,
					 	THREE
					 }
					""";
			handleUnit(source, (unit, ctx) -> {
				validateRange(unit, source, "ONE", ClassMemberPathNode.class, memberPath -> {
					ClassMember member = memberPath.getValue();
					assertEquals("ONE", member.getName());
				});
				validateRange(unit, source, "TWO", ClassMemberPathNode.class, memberPath -> {
					ClassMember member = memberPath.getValue();
					assertEquals("TWO", member.getName());
				});
				validateRange(unit, source, "THREE", ClassMemberPathNode.class, memberPath -> {
					ClassMember member = memberPath.getValue();
					assertEquals("THREE", member.getName());
				});
			});
		}

		@Test
		@Disabled("Fails since generic type is used as return type, getEnumConstants() = DummyEnum[] not base type Enum[]")
		void testMethodReference_SyntheticEnumMethod() {
			String source = """
					package software.coley.recaf.test.dummy;
					     
					public class DummyEnumPrinter {
						public static void main(String[] args) {
							for (DummyEnum enumConstant : DummyEnum.class.getEnumConstants()) {
								String name = enumConstant.name();
								System.out.println(name);
								
								Supplier<String> supplier = enumConstant::name;
								System.out.println(supplier.get());
							}
						}
					}
					""";
			// TODO: More tests validating generic methods being resolved to NOT the <T> type once this behavior is fixed
			handleUnit(source, (unit, ctx) -> {
				validateRange(unit, source, "name()", ClassMemberPathNode.class, memberPath -> {
					ClassMember member = memberPath.getValue();
					assertEquals("name", member.getName());
					assertEquals("()Ljava/lang/String;", member.getDescriptor());
				});
				validateRange(unit, source, "enumConstant::name", ClassMemberPathNode.class, memberPath -> {
					ClassMember member = memberPath.getValue();
					assertEquals("name", member.getName());
					assertEquals("()Ljava/lang/String;", member.getDescriptor());
				});
				validateRange(unit, source, "getEnumConstants", ClassPathNode.class, classPath -> {
					assertEquals("software/coley/recaf/test/dummy/DummyEnum", classPath.getValue().getName());
				});
			});
		}

		@Test
		void testStaticMethodCall() {
			String source = """
					package software.coley.recaf.test.dummy;
					  										
					class HelloWorld {
						static void main(String[] args) {
							ClassWithExceptions.readInt(args[0]);
						}
					}
					""";
			handleUnit(source, (unit, ctx) -> {
				validateRange(unit, source, "ClassWithExceptions", ClassPathNode.class, classPath -> {
					assertEquals("software/coley/recaf/test/dummy/ClassWithExceptions", classPath.getValue().getName());
				});
				validateRange(unit, source, "readInt", ClassMemberPathNode.class, memberPath -> {
					ClassMember member = memberPath.getValue();
					assertEquals("readInt", member.getName());
					assertEquals("(Ljava/lang/Object;)I", member.getDescriptor());
				});
			});
		}

		@Test
		void testStaticFieldRef() {
			String source = """
					package software.coley.recaf.test.dummy;
					  										
					class HelloWorld {
						static void main(String[] args) {
							ClassWithStaticInit.i = 0;
						}
					}
					""";
			handleUnit(source, (unit, ctx) -> {
				validateRange(unit, source, "ClassWithStaticInit", ClassPathNode.class, classPath -> {
					assertEquals("software/coley/recaf/test/dummy/ClassWithStaticInit", classPath.getValue().getName());
				});
				validateRange(unit, source, "i ", ClassMemberPathNode.class, memberPath -> {
					ClassMember member = memberPath.getValue();
					assertEquals("software/coley/recaf/test/dummy/ClassWithStaticInit", member.getDeclaringClass().getName());
					assertEquals("i", member.getName());
					assertEquals("I", member.getDescriptor());
				});
			});
		}

		@Test
		void testConstructorDeclaration() {
			String source = """
					package software.coley.recaf.test.dummy;
					public class ClassWithConstructor {
					  	public ClassWithConstructor() {}
					  	public ClassWithConstructor(int i) {}
					  	public ClassWithConstructor(int i, int j) {}
					}
					""";
			handleUnit(source, (unit, ctx) -> {
				validateRange(unit, source, "ClassWithConstructor()", ClassMemberPathNode.class, memberPath -> {
					ClassMember member = memberPath.getValue();
					assertEquals("<init>", member.getName());
					assertEquals("()V", member.getDescriptor());
				});
				validateRange(unit, source, "ClassWithConstructor(int i)", ClassMemberPathNode.class, memberPath -> {
					ClassMember member = memberPath.getValue();
					assertEquals("<init>", member.getName());
					assertEquals("(I)V", member.getDescriptor());
				});
				validateRange(unit, source, "ClassWithConstructor(int i, int j)", ClassMemberPathNode.class, memberPath -> {
					ClassMember member = memberPath.getValue();
					assertEquals("<init>", member.getName());
					assertEquals("(II)V", member.getDescriptor());
				});
			});
		}

		@Test
		void testStaticInitializer() {
			String source = """
					package software.coley.recaf.test.dummy;
					public class ClassWithStaticInit {
					 	public static int i;
					 	static { i = 42; }
					 }
					""";
			handleUnit(source, (unit, ctx) -> {
				validateRange(unit, source, "static {", ClassMemberPathNode.class, memberPath -> {
					ClassMember member = memberPath.getValue();
					assertEquals("<clinit>", member.getName());
					assertEquals("()V", member.getDescriptor());
				});
			});
		}
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
			handleUnit(source, (unit, ctx) -> {
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
			handleUnit(source, (unit, ctx) -> {
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
			handleUnit(source, (unit, ctx) -> {
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
			handleUnit(source, (unit, ctx) -> {
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
			handleUnit(source, (unit, ctx) -> {
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
			handleUnit(source, (unit, ctx) -> {
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
			handleUnit(source, (unit, ctx) -> {
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
			handleUnit(source, (unit, ctx) -> {
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
			handleUnit(source, (unit, ctx) -> {
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
			handleUnit(source, (unit, ctx) -> {
				IntermediateMappings mappings = new IntermediateMappings();
				mappings.addClass(HelloWorld.class.getName().replace('.', '/'), "com/example/Howdy");
				AstMappingVisitor visitor = new AstMappingVisitor(mappings);

				String modified = unit.acceptJava(visitor, ctx).print(new Cursor(null, unit));
				assertTrue(modified.contains("Supplier<Howdy> worldSupplier = Howdy::new"));
			});
		}

		@Test
		@Disabled("Pending support in mapping visitor")
		void renameClass_QualifiedNameReference() {
			String source = """
					package software.coley.recaf.test.dummy;
										
					class HelloWorld {
						static {
							software.coley.recaf.util.Types value = new software.coley.recaf.util.Types();
						}
					}
					""";
			handleUnit(source, (unit, ctx) -> {
				IntermediateMappings mappings = new IntermediateMappings();
				mappings.addClass(Types.class.getName().replace('.', '/'), "com/example/TypeUtil");
				AstMappingVisitor visitor = new AstMappingVisitor(mappings);

				String modified = unit.acceptJava(visitor, ctx).print(new Cursor(null, unit));
				System.err.println(modified);
				assertTrue(modified.contains("com.example.TypeUtil value = new com.example.TypeUtil();"));
			});
		}

		@Test
		void renameMember_FieldName() {
			String source = """
					package software.coley.recaf.test.dummy;
										
					class HelloWorld {
						static String foo;
						String fizz;
						
						HelloWorld() {
							this.fizz = "fizz";
							fizz = "buzz";
							fizz = foo;
							fizz.toString();
						}
						
						static {
							HelloWorld.foo = "foo";
							foo = "bar";
						}
					}
					""";
			handleUnit(source, (unit, ctx) -> {
				IntermediateMappings mappings = new IntermediateMappings();
				mappings.addField(HelloWorld.class.getName().replace('.', '/'), "Ljava/lang/String;", "foo", "bar");
				mappings.addField(HelloWorld.class.getName().replace('.', '/'), "Ljava/lang/String;", "fizz", "buzz");
				AstMappingVisitor visitor = new AstMappingVisitor(mappings);

				String modified = unit.acceptJava(visitor, ctx).print(new Cursor(null, unit));
				assertTrue(modified.contains("static String bar;"));
				assertTrue(modified.contains("String buzz;"));

				assertTrue(modified.contains("this.buzz = \"fizz\";"));
				assertTrue(modified.contains("buzz = \"buzz\";"));
				assertTrue(modified.contains("buzz = bar;"));
				assertTrue(modified.contains("buzz.toString();"));

				assertTrue(modified.contains("HelloWorld.bar = \"foo\";"));
				assertTrue(modified.contains("bar = \"bar\";"));
			});
		}

		@Test
		void renameMember_MethodName() {
			String source = """
					package software.coley.recaf.test.dummy;
										
					import java.util.function.Supplier;
										
					class HelloWorld {
						static {
							Supplier<String> fooSupplier = HelloWorld::foo;
							
							foo();
						}
						
						static String foo() { return "foo"; }
					}
					""";
			handleUnit(source, (unit, ctx) -> {
				IntermediateMappings mappings = new IntermediateMappings();
				mappings.addMethod(HelloWorld.class.getName().replace('.', '/'), "()Ljava/lang/String;", "foo", "bar");
				AstMappingVisitor visitor = new AstMappingVisitor(mappings);

				String modified = unit.acceptJava(visitor, ctx).print(new Cursor(null, unit));
				assertTrue(modified.contains("Supplier<String> fooSupplier = HelloWorld::bar;"));
				assertTrue(modified.contains("bar();"));
				assertTrue(modified.contains("static String bar() { return \"foo\"; }"));
			});
		}

		@Test
		@Disabled("Resolving the exact member reference of static import not directly supported by OpenRewrite")
		void renameMember_MethodNameStaticallyImported() {
			String source = """
					package software.coley.recaf.test.dummy;
										
					import static software.coley.recaf.test.dummy.ClassWithExceptions.readInt;
										
					class HelloWorld {
						static void main(String[] args) {
							readInt(args[0]);
						}
					}
					""";
			handleUnit(source, (unit, ctx) -> {
				IntermediateMappings mappings = new IntermediateMappings();
				mappings.addMethod(ClassWithExceptions.class.getName().replace('.', '/'), "(Ljava/lang/Object;)I", "readInt", "foo");
				AstMappingVisitor visitor = new AstMappingVisitor(mappings);

				String modified = unit.acceptJava(visitor, ctx).print(new Cursor(null, unit));
				assertTrue(modified.contains("import static software.coley.recaf.test.dummy.ClassWithExceptions.foo;"));
				assertTrue(modified.contains("foo(args[0]);"));
			});
		}
	}

	private static <T> void validateRange(@Nonnull J.CompilationUnit unit,
										  @Nonnull String source, @Nonnull String match,
										  @Nonnull Class<T> targetType,
										  @Nonnull Consumer<T> consumer) {
		int start = source.indexOf(match);
		int end = start + match.length();
		validateRange(unit, start, end, targetType, consumer);
	}

	@SuppressWarnings("unchecked")
	private static <T> void validateRange(@Nonnull J.CompilationUnit unit,
										  int start, int end,
										  @Nonnull Class<T> targetType,
										  @Nonnull Consumer<T> consumer) {
		PathNode<?> path;
		for (int i = start; i < end; i++) {
			path = helper.resolve(unit, i);
			if (path != null && targetType.isAssignableFrom(path.getClass())) {
				consumer.accept((T) path);
			} else {
				fail("Failed to identify target at index: " + i + " in range [" + start + "-" + end + "]");
			}
		}
	}

	private static void handleUnit(String source, BiConsumer<J.CompilationUnit, ExecutionContext> consumer) {
		InMemoryExecutionContext context = new InMemoryExecutionContext(Throwable::printStackTrace);
		List<J.CompilationUnit> units = parser.parse(context, source);
		assertEquals(1, units.size());
		if (consumer != null)
			consumer.accept(units.get(0), context);
	}
}
