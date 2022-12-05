package software.coley.recaf.info;

import jakarta.annotation.Nonnull;
import software.coley.recaf.workspace.model.Bundle;

/**
 * Outline of all info types.
 *
 * @author Matt Coley
 */
public interface Info {
	/**
	 * @return Name. Used as a path/key in a {@link Bundle}.
	 */
	@Nonnull
	String getName();
}
