package software.coley.recaf.services.decompile;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.coley.recaf.TestBase;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.services.decompile.cfr.CfrDecompiler;
import software.coley.recaf.test.TestClassUtils;
import software.coley.recaf.test.dummy.HelloWorld;
import software.coley.recaf.workspace.model.Workspace;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DecompilerManager}.
 */
public class DecompileManagerTest extends TestBase {
	static DecompilerManager decompilerManager;
	static Workspace workspace;
	static JvmClassInfo classToDecompile;

	@BeforeAll
	static void setup() throws IOException {
		classToDecompile = TestClassUtils.fromRuntimeClass(HelloWorld.class);
		decompilerManager = recaf.get(DecompilerManager.class);
		workspace = TestClassUtils.fromBundle(TestClassUtils.fromClasses(classToDecompile));
		workspaceManager.setCurrent(workspace);
	}

	@Test
	void testCfr() {
		JvmDecompiler decompiler = decompilerManager.getJvmDecompiler(CfrDecompiler.NAME);
		assertNotNull(decompiler, "CFR decompiler was never registered with manager");
		runDecompilation(decompiler);
	}

	private static void runDecompilation(JvmDecompiler decompiler) {
		try {
			DecompileResult result = decompilerManager.decompile(decompiler, workspace, classToDecompile)
					.get(1, TimeUnit.SECONDS);
			assertEquals(DecompileResult.ResultType.SUCCESS, result.getType(), "Decompile result was not successful");
			assertNotNull(result.getText(), "Decompile result missing text");
			assertTrue(result.getText().contains("\"Hello world\""), "Decompilation seems to be wrong");
		} catch (InterruptedException e) {
			fail("Decompile was interrupted", e);
		} catch (ExecutionException e) {
			fail("Decompile was encountered exception", e.getCause());
		} catch (TimeoutException e) {
			fail("Decompile timed out", e);
		}
	}
}
