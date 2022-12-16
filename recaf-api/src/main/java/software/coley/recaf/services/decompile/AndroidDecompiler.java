package software.coley.recaf.services.decompile;

import jakarta.annotation.Nonnull;
import software.coley.recaf.info.AndroidClassInfo;
import software.coley.recaf.workspace.model.Workspace;

/**
 * Outline for Android/Dalvik decompile capabilities.
 *
 * @author Matt Coley
 */
public abstract class AndroidDecompiler extends AbstractDecompiler implements Decompiler {
	/**
	 * @param name
	 * 		Decompiler name.
	 * @param version
	 * 		Decompiler version.
	 */
	public AndroidDecompiler(@Nonnull String name, @Nonnull String version) {
		super(name, version);
	}

	// Placeholder until more fleshed out API is implemented
	public abstract DecompileResult decompile(Workspace workspace, AndroidClassInfo classInfo);
}
