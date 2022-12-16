package software.coley.recaf.services.decompile;

import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.workspace.model.Workspace;

/**
 * Outline for decompilers targeting {@link JvmClassInfo}.
 *
 * @author Matt Coley
 */
public interface JvmDecompiler extends Decompiler {
	/**
	 * @param filter
	 * 		Filter to add.
	 */
	void addJvmInputFilter(JvmInputFilter filter);

	/**
	 * @param workspace
	 * 		Workspace to pull data from.
	 * @param classInfo
	 * 		Class to decompile.
	 *
	 * @return Decompilation result.
	 */
	DecompileResult decompile(Workspace workspace, JvmClassInfo classInfo);

	/**
	 * @param workspace
	 * 		Workspace to pull data from.
	 * @param name
	 * 		Class name.
	 * @param bytecode
	 * 		Class bytecode.
	 *
	 * @return Decompilation result.
	 */
	DecompileResult decompile(Workspace workspace, String name, byte[] bytecode);
}
