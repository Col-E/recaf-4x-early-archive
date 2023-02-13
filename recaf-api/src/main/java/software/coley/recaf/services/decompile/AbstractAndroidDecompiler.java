package software.coley.recaf.services.decompile;

import jakarta.annotation.Nonnull;

/**
 * Basic setup for {@link AndroidDecompiler}.
 *
 * @param <C>
 * 		Config type.
 *
 * @author Matt Coley
 */
public abstract class AbstractAndroidDecompiler<C extends DecompilerConfig> extends AbstractDecompiler<C> implements AndroidDecompiler<C> {
	/**
	 * @param name
	 * 		Decompiler name.
	 * @param version
	 * 		Decompiler version.
	 * @param config
	 * 		Decompiler configuration.
	 */
	public AbstractAndroidDecompiler(@Nonnull String name, @Nonnull String version, @Nonnull C config) {
		super(name, version, config);
	}
}
