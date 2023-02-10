package software.coley.recaf.ui.control.richtext.linegraphics;

import jakarta.annotation.Nonnull;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import software.coley.recaf.ui.control.richtext.Editor;
import software.coley.recaf.ui.control.richtext.bracket.SelectedBracketTracking;

/**
 * Graphic factory that adds a line indicator to matched lines containing the
 * {@link SelectedBracketTracking#getRange() current bracket pair}.
 *
 * @author Matt Coley
 * @see SelectedBracketTracking
 */
public class BracketMatchGraphicFactory extends AbstractLineGraphicFactory {
	private static final Insets PADDING = new Insets(0, 0, 0, 5);
	private SelectedBracketTracking selectedBracketTracking;

	/**
	 * New graphic factory.
	 */
	public BracketMatchGraphicFactory() {
		super(P_BRACKET_MATCH);
	}

	@Override
	public void install(@Nonnull Editor editor) {
		selectedBracketTracking = editor.getSelectedBracketTracking();
	}

	@Override
	public void uninstall(@Nonnull Editor editor) {
		selectedBracketTracking = null;
	}

	@Override
	public Node apply(int line) {
		// Always null if no bracket tracking is registered for the editor.
		if (selectedBracketTracking == null) return null;

		// Add brace line for selected.
		if (selectedBracketTracking.isSelectedLine(line)) {
			Separator separator = new Separator(Orientation.VERTICAL);
			separator.setPadding(PADDING);
			separator.getStyleClass().add("matched-brace-line");
			return separator;
		}

		// Add fallback brace line for unmatched lines.
		Separator separator = new Separator(Orientation.VERTICAL);
		separator.setPadding(PADDING);
		separator.getStyleClass().add("unmatched-brace-line");
		return separator;
	}
}
