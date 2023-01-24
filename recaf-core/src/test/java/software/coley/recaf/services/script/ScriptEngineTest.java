package software.coley.recaf.services.script;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.coley.recaf.TestBase;
import software.coley.recaf.services.compile.CompilerDiagnostic;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ScriptEngine}
 */
public class ScriptEngineTest extends TestBase {
	static ScriptEngine engine;

	@BeforeAll
	static void setup() {
		engine = recaf.get(ScriptEngine.class);
	}

	@Nested
	class Snippet {
		@Test
		void testHelloWorld() {
			assertSuccess("System.out.println(\"hello\");");
		}
	}

	@Nested
	class Full {
		@Test
		void testConstructorInjection() {
			assertSuccess("""
					@Dependent
					public class Test implements Runnable {
						private final JavacCompiler compiler;
										
						@Inject
						public Test(JavacCompiler compiler) {
							this.compiler = compiler;
						}
						
						@Override
						public void run() {
							System.out.println("hello: " + compiler);
							if (compiler == null) throw new IllegalStateException();
						}
					}
					""");
		}

		@Test
		void testFieldInjection() {
			assertSuccess("""
					@Dependent
					public class Test implements Runnable {
						@Inject
						JavacCompiler compiler;
						
						@Override
						public void run() {
							System.out.println("hello: " + compiler);
							if (compiler == null) throw new IllegalStateException();
						}
					}
					""");
		}

		@Test
		void testInjectionWithoutStatedScope() {
			assertSuccess("""
					public class Test implements Runnable {
						@Inject
						JavacCompiler compiler;
						
						@Override
						public void run() {
							System.out.println("hello: " + compiler);
							if (compiler == null) throw new IllegalStateException();
						}
					}
					""");
		}
	}

	static void assertSuccess(String code) {
		try {
			engine.run(code).thenAccept(result -> {
				for (CompilerDiagnostic diagnostic : result.getCompileDiagnostics())
					fail("Unexpected diagnostic: " + diagnostic);

				Throwable thrown = result.getRuntimeThrowable();
				if (thrown != null)
					fail("Unexpected exception at runtime", thrown);

				assertTrue(result.wasSuccess());
				assertFalse(result.wasCompileFailure());
				assertFalse(result.wasRuntimeError());
			}).get(5, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException ex) {
			fail(ex);
		}
	}
}
