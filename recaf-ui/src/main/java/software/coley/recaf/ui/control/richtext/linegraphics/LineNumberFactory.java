package software.coley.recaf.ui.control.richtext.linegraphics;

import javafx.scene.Node;
import javafx.scene.control.Label;
import org.fxmisc.richtext.CodeArea;
import software.coley.recaf.util.StringUtil;

/**
 * Graphic factory to draw line numbers.
 *
 * @author Matt Coley
 */
public class LineNumberFactory extends AbstractLineGraphicFactory {
	private final CodeArea codeArea;

	/**
	 * @param codeArea
	 * 		Parent code area to pull line count from.
	 */
	public LineNumberFactory(CodeArea codeArea) {
		super(P_LINE_NUMBERS);
		this.codeArea = codeArea;
	}

	@Override
	public Node apply(int line) {
		return new Label(format(line, codeArea.getParagraphs().size()));
	}

	private static String format(int line, int max) {
		int digits = (int) Math.floor(Math.log10(max)) + 1;
		return String.format(StringUtil.fillLeft(digits, " ", String.valueOf(line)));
	}
}
