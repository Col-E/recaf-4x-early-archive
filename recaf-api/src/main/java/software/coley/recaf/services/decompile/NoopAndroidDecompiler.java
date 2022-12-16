package software.coley.recaf.services.decompile;

/**
 * No-op decompiler for {@link JvmDecompiler}
 *
 * @author Matt Coley
 */
public class NoopAndroidDecompiler extends AndroidDecompiler {
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
}
