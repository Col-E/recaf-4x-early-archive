package software.coley.recaf.info;

import jakarta.annotation.Nonnull;

/**
 * Outline of all info types.
 *
 * @author Matt Coley
 */
public interface Info {
	/**
	 * @return Name.
	 */
	@Nonnull
	String getName();
}
