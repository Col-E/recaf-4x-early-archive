package software.coley.recaf.services.decompile;

import software.coley.recaf.workspace.model.Workspace;

/**
 * No-op decompiler for {@link JvmDecompiler}
 *
 * @author Matt Coley
 */
public class NoopJvmDecompiler extends JvmDecompiler {
	private static final NoopJvmDecompiler INSTANCE = new NoopJvmDecompiler();

	private NoopJvmDecompiler() {
		super("no-op-jvm", "1.0.0");
	}

	/**
	 * @return Singleton instance.
	 */
	public static NoopJvmDecompiler getInstance() {
		return INSTANCE;
	}

	@Override
	protected DecompileResult decompile(Workspace workspace, String name, byte[] bytecode) {
		return new DecompileResult(null, null, DecompileResult.ResultType.SKIPPED);
	}
}
