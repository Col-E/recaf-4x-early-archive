package software.coley.recaf.ui.control.richtext.bracket;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.TwoDimensional;
import software.coley.recaf.ui.control.richtext.Editor;
import software.coley.recaf.ui.control.richtext.EditorComponent;
import software.coley.recaf.ui.control.richtext.linegraphics.BracketMatchGraphicFactory;
import software.coley.recaf.util.FxThreadUtil;
import software.coley.recaf.util.IntRange;
import software.coley.recaf.util.threading.ThreadPoolFactory;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * Bracket tracking component for {@link Editor}.
 * Records matching bracket pairs the user is interacting with.
 *
 * @author Matt Coley
 * @see BracketMatchGraphicFactory Adds a line indicator to an {@link Editor} for lines covering the {@link #getRange() range}.
 * @see Editor#setBracketTracking(BracketTracking) Call to install into an {@link Editor}.
 */
public class BracketTracking implements EditorComponent, ChangeListener<Integer> {
	private final ExecutorService service = ThreadPoolFactory.newSingleThreadExecutor("brackets");
	private CodeArea codeArea;
	private Editor editor;
	private IntRange range;

	@Override
	public void changed(ObservableValue<? extends Integer> observable, Integer oldPos, Integer curPos) {
		// Unbox
		int pos = curPos;
		int len = codeArea.getLength();

		// Submit task to check for open/close pair
		service.submit(() -> {
			// Check if character adjacent to the caret position is an open or closing bracket.
			boolean found = false;
			if (pos > 0)
				found = scan(pos - 1);
			if (!found && pos < len)
				found = scan(pos);

			// No bracket pair found, clear range.
			if (!found)
				setRange(null);
		});
	}

	/**
	 * @return Current range of bracket pair. {@code null} when no pair is selected currently.
	 */
	@Nullable
	public IntRange getRange() {
		return range;
	}

	/**
	 * @param line
	 * 		Line number.
	 *
	 * @return {@code true} when the line belongs to the {@link #getRange() selected bracket pair range}.
	 */
	public boolean isSelectedLine(int line) {
		return isSelectedParagraph(line - 1);
	}

	/**
	 * @param paragraph
	 * 		Paragraph index, which is {@code line - 1}.
	 *
	 * @return {@code true} when the line belongs to the {@link #getRange() selected bracket pair range}.
	 */
	public boolean isSelectedParagraph(int paragraph) {
		if (range == null) return false;

		// Check paragraph beyond start range.
		TwoDimensional.Position startPos = codeArea.offsetToPosition(range.start(), TwoDimensional.Bias.Backward);
		int startParagraph = startPos.getMajor() - 1; // Position uses line numbers. Offset to paragraph index.
		if (paragraph < startParagraph) return false;

		// Check paragraph before end range.
		TwoDimensional.Position endPos = codeArea.offsetToPosition(range.end(), TwoDimensional.Bias.Forward);
		int endParagraph = endPos.getMajor() - 1; // Position uses line numbers. Offset to paragraph index.
		return paragraph <= endParagraph;
	}

	private boolean scan(int pos) {
		String text = codeArea.getText();
		char c = text.charAt(pos);
		char openChar;
		char closeChar;
		boolean forward;
		if (c == '{' || c == '}') {
			openChar = '{';
			closeChar = '}';
			forward = c == '{';
		} else if (c == '[' || c == ']') {
			openChar = '[';
			closeChar = ']';
			forward = c == '[';
		} else if (c == '(' || c == ')') {
			openChar = '(';
			closeChar = ')';
			forward = c == '(';
		} else {
			// Not a supported bracket pair
			return false;
		}

		return forward ? scanForwards(text, pos, openChar, closeChar) :
				scanBackwards(text, pos, openChar, closeChar);
	}

	private boolean scanForwards(String text, int pos, char openChar, char closeChar) {
		int start = pos;
		int end = -1;
		int balance = 1;
		int open;
		int close;
		boolean unmatched = true;
		do {
			open = text.indexOf(openChar, pos + 1);
			close = text.indexOf(closeChar, pos + 1);

			if (open != -1 && open < close)
				balance++;
			else if (close > pos)
				balance--;
			else
				return false;

			if (balance == 0) {
				end = close;
				break;
			}
			int next = Math.min(open, close);
			if (next <= pos)
				unmatched = false;
			pos = next;
		} while (unmatched);
		if (end > 0) {
			setRange(new IntRange(start, end));
			return true;
		}
		return false;
	}

	private boolean scanBackwards(String text, int pos, char openChar, char closeChar) {
		int start = -1;
		int end = pos;
		int balance = 1;
		int open;
		int close;
		boolean unmatched = true;
		do {
			open = text.lastIndexOf(openChar, pos - 1);
			close = text.lastIndexOf(closeChar, pos - 1);

			if (open > close)
				balance--;
			else if (close != -1 && close < pos)
				balance++;
			else
				return false;

			if (balance == 0) {
				start = open;
				break;
			}
			int next = Math.max(open, close);
			if (next >= pos)
				unmatched = false;
			pos = next;
		} while (unmatched);
		if (start >= 0) {
			setRange(new IntRange(start, end));
			return true;
		}
		return false;
	}

	/**
	 * Assigns the current selected bracket pair range.
	 * When the new value differs from the last value, the linked editor will redraw its paragraph graphics.
	 *
	 * @param newRange
	 * 		New range to assign. {@code null} to remove selected bracket pair range.
	 */
	private void setRange(@Nullable IntRange newRange) {
		IntRange lastRange = range;
		range = newRange;

		// Redraw paragraphs visible when the range is modified.
		if (!Objects.equals(lastRange, newRange))
			FxThreadUtil.run(() -> editor.redrawParagraphGraphics());
	}

	@Override
	public void install(@Nonnull Editor editor) {
		this.editor = editor;
		codeArea = editor.getCodeArea();
		codeArea.caretPositionProperty().addListener(this);
	}

	@Override
	public void uninstall(@Nonnull Editor editor) {
		if (codeArea != null) {
			codeArea.caretPositionProperty().removeListener(this);
			codeArea = null;
		}
	}
}
