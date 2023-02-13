package software.coley.recaf.services.decompile;

import jakarta.annotation.Nonnull;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.workspace.model.Workspace;

/**
 * Outline for decompilers targeting {@link JvmClassInfo}.
 *
 * @param <C>
 * 		Config type.
 *
 * @author Matt Coley
 */
public interface JvmDecompiler<C extends DecompilerConfig> extends Decompiler<C> {
	/**
	 * @param filter
	 * 		Filter to add.
	 */
	void addJvmInputFilter(@Nonnull JvmInputFilter filter);

	/**
	 * @param workspace
	 * 		Workspace to pull data from.
	 * @param classInfo
	 * 		Class to decompile.
	 *
	 * @return Decompilation result.
	 */
	DecompileResult decompile(@Nonnull Workspace workspace, @Nonnull JvmClassInfo classInfo);

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
	DecompileResult decompile(@Nonnull Workspace workspace, @Nonnull String name, @Nonnull byte[] bytecode);
}
