package software.coley.recaf.services.decompile;

import software.coley.recaf.info.properties.builtin.CachedDecompileProperty;

/**
 * Common decompiler operations.
 *
 * @param <C>
 * 		Config type.
 *
 * @author Matt Coley
 * @see JvmDecompiler For decompiling JVM bytecode.
 * @see AndroidDecompiler For decompiling Android/Dalvik bytecode.
 * @see DecompilerConfig For config management of decompiler values,
 * and ensuring {@link CachedDecompileProperty} values are compatible with current settings.
 */
public interface Decompiler<C extends DecompilerConfig> {
	/**
	 * @return Decompiler name.
	 */
	String getName();

	/**
	 * @return Decompiler version.
	 */
	String getVersion();

	/**
	 * @return Decompiler config.
	 */
	C getConfig();
}
