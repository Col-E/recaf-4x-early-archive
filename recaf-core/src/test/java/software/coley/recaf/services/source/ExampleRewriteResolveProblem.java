package software.coley.recaf.services.source;

import com.android.tools.r8.D8;
import jakarta.annotation.Nonnull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import software.coley.recaf.TestBase;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.services.decompile.DecompilerManager;
import software.coley.recaf.workspace.io.ResourceImporter;
import software.coley.recaf.workspace.model.BasicWorkspace;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.nio.file.Paths;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Demonstration of the bug mentioned
 * <a href="https://discord.com/channels/996548279191998535/996548280118935674/1104253312754253945">here</a>.
 */
public class ExampleRewriteResolveProblem extends TestBase {
	static AstService ast;
	static DecompilerManager decompile;
	static Workspace workspace;
	static JavaParser parser;
	static JvmClassInfo target;

	static {
		// D8 has some bogus asserts we need to disable
		D8.class.getClassLoader().setDefaultAssertionStatus(false);
	}

	@BeforeAll
	static void setup() throws Exception {
		// Import the DEX file to a workspace
		WorkspaceResource resource = recaf.get(ResourceImporter.class)
				.importResource(Paths.get("src/test/resources/classes.dex"));
		workspace = new BasicWorkspace(resource);
		workspaceManager.setCurrent(workspace);
		target = workspace.findClass("androidx/activity/ComponentActivity").getValue().asJvmClass();

		// Get decompiler instance to fetch 'source' of the class.
		decompile = recaf.get(DecompilerManager.class);

		// Create the parser instance.
		// - byte[][] classpath is populated by directly referenced classes in the target class
		ast = recaf.get(AstService.class);
		parser = ast.newParser(target);
	}

	@Test
	void test() throws Exception {
		String source = decompile.decompile(workspace, target).get().getText();

		// The first parse fails.
		handleUnit(source, (unit, ctx) -> {
			validateRange(unit, "ViewModelStoreOwner", discoveredType -> {
				// You can comment out the assertion here to go on to the next block further below,
				// which will always succeed.
				assertEquals("androidx.lifecycle.ViewModelStoreOwner", discoveredType);
			});
		});

		// Must call reset between parses.
		parser.reset();

		// Works as intended. The type is resolved successfully now. Really weird.
		handleUnit(source, (unit, ctx) -> {
			validateRange(unit, "ViewModelStoreOwner", discoveredType -> {
				assertEquals("androidx.lifecycle.ViewModelStoreOwner", discoveredType);
			});
		});
	}


	private static void validateRange(@Nonnull J.CompilationUnit unit,
									  @Nonnull String match,
									  @Nonnull Consumer<String> consumer) {
		String result = resolve(unit, match);
		if (result != null) {
			consumer.accept(result);
		} else {
			fail("Failed to identify target");
		}
	}

	private static String resolve(@Nonnull J.CompilationUnit unit,
								  @Nonnull String match) {
		// Holds the type result
		String[] typeHolder = new String[1];

		// Use a visitor to extract the type from identifiers.
		unit.acceptJava(new JavaIsoVisitor<>() {
			@Nonnull
			@Override
			public J.Identifier visitIdentifier(@Nonnull J.Identifier identifier,
												@Nonnull InMemoryExecutionContext ctx) {
				if (identifier.getSimpleName().equals(match)) {
					// Extract qualified type name from identifier
					JavaType type = identifier.getType();
					JavaType.Variable fieldType = identifier.getFieldType();
					if (type == null || type == JavaType.Unknown.getInstance() && fieldType != null) {
						type = fieldType.getType();
					}

					// Useful to show that the two resolves behave differently between the first/second calls to 'handleUnit'
					System.out.println(match + " --> " + type);

					// Yes, this will override the first result. That is intentional.
					// In our test setup the first result is ALWAYS an import statement, which can be resolved.
					// The following references are what fails (stuff like implements X)
					typeHolder[0] = ((JavaType.FullyQualified) type).getFullyQualifiedName();
				}
				return super.visitIdentifier(identifier, ctx);
			}
		}, new InMemoryExecutionContext());

		// Unbox the result.
		return typeHolder[0];
	}

	private static void handleUnit(String source, BiConsumer<J.CompilationUnit, ExecutionContext> consumer) {
		InMemoryExecutionContext context = new InMemoryExecutionContext(Throwable::printStackTrace);
		List<J.CompilationUnit> units = parser.parse(context, source);
		assertEquals(1, units.size());
		if (consumer != null)
			consumer.accept(units.get(0), context);
	}
}
