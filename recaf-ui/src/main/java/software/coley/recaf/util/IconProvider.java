package software.coley.recaf.util;

import javafx.scene.Node;

/**
 * Provides an icon. Primarily used when wanting to provide an expected icon
 * <i>(typically requiring some computation to assemble)</i> lazily.
 *
 * @author xDark
 */
@FunctionalInterface
public interface IconProvider {
	/**
	 * @return Provided icon.
	 */
	Node makeIcon();
}
