package software.coley.recaf.ui.dock;

import atlantafx.base.theme.Styles;
import com.panemu.tiwulfx.control.dock.DetachableTabPane;
import com.panemu.tiwulfx.control.dock.TabDropHint;
import javafx.scene.shape.*;

/**
 * Base docking tab-pane.
 *
 * @author Matt Coley
 */
public class RecafDockingPane extends DetachableTabPane {
	public RecafDockingPane() {
		setDropHint(new RecafDropHint());
		getStyleClass().add(Styles.DENSE);
	}

	/**
	 * This is a slightly modified version of the default {@link TabDropHint} which fixes some size constants.
	 */
	private static class RecafDropHint extends TabDropHint {
		@Override
		protected void generateAdjacentPath(Path path, double startX, double startY, double width, double height) {
			// no-op
		}

		@Override
		protected void generateInsertionPath(Path path, double tabPos, double width, double height) {
			int padding = 5;
			int tabHeight = 45;
			tabPos = Math.max(0, tabPos);
			path.getElements().clear();
			MoveTo moveTo = new MoveTo();
			moveTo.setX(padding);
			moveTo.setY(tabHeight);
			path.getElements().add(moveTo);//start

			path.getElements().add(new HLineTo(width - padding));//path width
			path.getElements().add(new VLineTo(height - padding));//path height
			path.getElements().add(new HLineTo(padding));//path bottom left
			path.getElements().add(new VLineTo(tabHeight));//back to start

			if (tabPos > 20) {
				path.getElements().add(new MoveTo(tabPos, tabHeight + 5));
				path.getElements().add(new LineTo(Math.max(padding, tabPos - 10), tabHeight + 15));
				path.getElements().add(new HLineTo(tabPos + 10));
				path.getElements().add(new LineTo(tabPos, tabHeight + 5));
			} else {
				double tip = Math.max(tabPos, padding + 5);
				path.getElements().add(new MoveTo(tip, tabHeight + 5));
				path.getElements().add(new LineTo(tip + 10, tabHeight + 5));
				path.getElements().add(new LineTo(tip, tabHeight + 15));
				path.getElements().add(new VLineTo(tabHeight + 5));
			}
		}
	}
}
