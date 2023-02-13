package software.coley.recaf.services.cell;

import javafx.scene.control.ContextMenu;

/**
 * Provides a {@link ContextMenu}. Primarily used when wanting to provide an expected set of actions on a type lazily.
 *
 * @author Matt Coley
 */
public interface ContextMenuProvider {
	/**
	 * @return Provided menu.
	 */
	ContextMenu makeMenu();
}
