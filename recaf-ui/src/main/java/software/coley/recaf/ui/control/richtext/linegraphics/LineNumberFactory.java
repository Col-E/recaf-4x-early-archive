package software.coley.recaf.ui.control.richtext.linegraphics;

import jakarta.annotation.Nonnull;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.fxmisc.richtext.CodeArea;
import software.coley.recaf.ui.control.richtext.Editor;
import software.coley.recaf.util.StringUtil;

/**
 * Graphic factory to draw line numbers.
 *
 * @author Matt Coley
 */
public class LineNumberFactory extends AbstractLineGraphicFactory {
	private CodeArea codeArea;

	/**
	 * New line number factory.
	 */
	public LineNumberFactory() {
		super(P_LINE_NUMBERS);
	}

	@Override
	public void install(@Nonnull Editor editor) {
		codeArea = editor.getCodeArea();
	}

	@Override
	public void uninstall(@Nonnull Editor editor) {
		codeArea = null;
	}

	@Override
	public Node apply(int line) {
		if (codeArea == null) return null;
		return new Label(format(line, codeArea.getParagraphs().size()));
	}

	private static String format(int line, int max) {
		int digits = (int) Math.floor(Math.log10(max)) + 1;
		return String.format(StringUtil.fillLeft(digits, " ", String.valueOf(line)));
	}
}
