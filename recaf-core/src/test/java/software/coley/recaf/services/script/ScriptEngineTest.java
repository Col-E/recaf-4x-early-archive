package software.coley.recaf.services.script;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.coley.recaf.TestBase;
import software.coley.recaf.services.compile.JavacCompiler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ScriptEngine}
 */
public class ScriptEngineTest extends TestBase {
	static ScriptEngine engine;

	@BeforeAll
	static void setup() {
		assertTrue(JavacCompiler.isAvailable(), "javac not available!");
		engine = recaf.get(ScriptEngine.class);
	}

	@Test
	void testInlineCode() {
		engine.run("System.out.println(\"hello\");").thenAccept(result -> {
			assertTrue(result.wasSuccess());
			assertFalse(result.wasCompileFailure());
			assertFalse(result.wasRuntimeError());
			assertTrue(result.getCompileDiagnostics().isEmpty());
		});
	}

	@Test
	void testClassCode() {
		engine.run("""
				public class Test {
					public static void main() {
						System.out.println("hello");
					}
				}
				""").thenAccept(result -> {
			assertTrue(result.wasSuccess());
			assertFalse(result.wasCompileFailure());
			assertFalse(result.wasRuntimeError());
			assertTrue(result.getCompileDiagnostics().isEmpty());
		});
	}
}
