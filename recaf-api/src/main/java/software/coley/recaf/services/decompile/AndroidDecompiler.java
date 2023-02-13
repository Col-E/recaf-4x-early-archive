package software.coley.recaf.services.decompile;

import jakarta.annotation.Nonnull;
import software.coley.recaf.info.AndroidClassInfo;
import software.coley.recaf.workspace.model.Workspace;

/**
 * Outline for Android/Dalvik decompile capabilities.
 *
 * @param <C>
 * 		Config type.
 *
 * @author Matt Coley
 */
public interface AndroidDecompiler<C extends DecompilerConfig> extends Decompiler<C> {
	// Placeholder until more fleshed out API is implemented
	DecompileResult decompile(@Nonnull Workspace workspace, @Nonnull AndroidClassInfo classInfo);
}
