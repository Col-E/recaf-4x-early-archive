package software.coley.recaf.services.cell;

/**
 * Type used by components that may provide context menus.
 * Allows the {@link ContextMenuProviderFactory} types to know which class is requesting a menu.
 *
 * @author Matt Coley
 */
public interface ContextSource {
	// TODO: Should provide a way to validate a <PathNode> is a declaration or reference
	//       (or maybe make a custom type/enum to allow filtering the menu in additional cases?)
}
