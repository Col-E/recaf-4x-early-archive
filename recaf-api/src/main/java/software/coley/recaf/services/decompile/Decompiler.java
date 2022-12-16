package software.coley.recaf.services.decompile;

/**
 * Common decompiler operations.
 *
 * @author Matt Coley
 * @see JvmDecompiler For decompiling JVM bytecode.
 * @see AndroidDecompiler For decompiling Android/Dalvik bytecode.
 */
public interface Decompiler {
	/**
	 * @return Decompiler name.
	 */
	String getName();

	/**
	 * @return Decompiler version.
	 */
	String getVersion();
}
