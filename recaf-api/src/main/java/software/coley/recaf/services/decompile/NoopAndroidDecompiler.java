package software.coley.recaf.services.decompile;

import software.coley.recaf.info.AndroidClassInfo;
import software.coley.recaf.workspace.model.Workspace;

/**
 * No-op decompiler for {@link JvmDecompiler}
 *
 * @author Matt Coley
 */
public class NoopAndroidDecompiler extends AbstractAndroidDecompiler {
	private static final NoopAndroidDecompiler INSTANCE = new NoopAndroidDecompiler();

	private NoopAndroidDecompiler() {
		super("no-op-android", "1.0.0");
	}

	/**
	 * @return Singleton instance.
	 */
	public static NoopAndroidDecompiler getInstance() {
		return INSTANCE;
	}

	@Override
	public DecompileResult decompile(Workspace workspace, AndroidClassInfo classInfo) {
		return new DecompileResult(null, null, DecompileResult.ResultType.SKIPPED);
	}
}
