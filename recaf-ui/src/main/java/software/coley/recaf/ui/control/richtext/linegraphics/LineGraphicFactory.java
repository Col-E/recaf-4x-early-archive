package software.coley.recaf.ui.control.richtext.linegraphics;

import javafx.scene.Node;

import java.util.function.IntFunction;

/**
 * RichTextFX only uses {@link IntFunction} for graphics factories. We want to create our own system where
 * any number of graphic factories can be added, and the result will always be consistently displayed.
 * <br>
 * To facilitate this, we have this type which also is comparable to other instances of the same type.
 * Each graphic factory has a {@link #priority()} which defines its placement in the {@link RootLineGraphicFactory}.
 * Lower values appear first.
 *
 * @author Matt Coley
 * @see AbstractLineGraphicFactory Base implementation of this type.
 * @see RootLineGraphicFactory The root implementation which managed displaying other
 * {@link LineGraphicFactory} instances in order.
 */
public interface LineGraphicFactory extends IntFunction<Node>, Comparable<LineGraphicFactory> {
	/**
	 * Priority for {@link LineNumberFactory}.
	 */
	int P_LINE_NUMBERS = 0;

	/**
	 * @return Order priority for sorting in {@link RootLineGraphicFactory}. Lower values appear first.
	 */
	int priority();

	@Override
	default int compareTo(LineGraphicFactory o) {
		return Integer.compare(priority(), o.priority());
	}
}
