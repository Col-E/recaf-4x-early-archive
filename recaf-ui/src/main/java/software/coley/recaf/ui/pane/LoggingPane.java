package software.coley.recaf.ui.pane;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import org.fxmisc.richtext.CodeArea;
import org.slf4j.event.Level;
import software.coley.recaf.analytics.logging.LogConsumer;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.services.file.RecafDirectoriesConfig;
import software.coley.recaf.ui.control.richtext.Editor;
import software.coley.recaf.ui.control.richtext.linegraphics.LineContainer;
import software.coley.recaf.ui.control.richtext.linegraphics.LineGraphicFactory;
import software.coley.recaf.util.FxThreadUtil;
import software.coley.recaf.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Pane for displaying logger calls.
 *
 * @author Matt Coley
 */
@Dependent
public class LoggingPane extends BorderPane implements LogConsumer<String> {
	private final List<LogCallInfo> infos = new ArrayList<>();
	private final Editor editor = new Editor();
	private final CodeArea codeArea = editor.getCodeArea();

	@Inject
	public LoggingPane(@Nonnull RecafDirectoriesConfig config) {
		Logging.addLogConsumer(this);
		codeArea.setEditable(false);
		editor.getRootLineGraphicFactory().addLineGraphicFactory(new LoggingLineFactory());
		setCenter(editor);

		// Initial line
		infos.add(new LogCallInfo("Initial", Level.TRACE, null));
		codeArea.appendText("Current log will write to: " + StringUtil.pathToAbsoluteString(config.getCurrentLogPath()));
	}

	@Override
	public void accept(String loggerName, Level level, String messageContent) {
		infos.add(new LogCallInfo(loggerName, level, null));
		FxThreadUtil.run(() -> {
			codeArea.appendText("\n" + messageContent);
			codeArea.showParagraphAtBottom(codeArea.getParagraphs().size() - 1);
		});
	}

	@Override
	public void accept(String loggerName, Level level, String messageContent, Throwable throwable) {
		infos.add(new LogCallInfo(loggerName, level, throwable));
		FxThreadUtil.run(() -> {
			codeArea.appendText("\n" + messageContent);
			codeArea.showParagraphAtBottom(codeArea.getParagraphs().size() - 1);
		});
	}

	private record LogCallInfo(String loggerName, Level level, Throwable throwable) {
	}

	private class LoggingLineFactory implements LineGraphicFactory {
		private static final Insets PADDING = new Insets(0, 5, 0, 0);
		private static final double SIZE = 4;
		private static final double[] TRIANGLE = {
				SIZE / 2, 0,
				SIZE, SIZE,
				0, SIZE
		};

		@Override
		public int priority() {
			return -1;
		}

		@Override
		public void apply(@Nonnull LineContainer container, int paragraph) {
			LogCallInfo info = infos.get(paragraph);
			Shape shape;
			switch (info.level) {
				case ERROR -> shape = info.throwable == null ?
						new Circle(SIZE, Color.RED) : new Polygon(TRIANGLE);
				case WARN -> shape = new Circle(SIZE, Color.YELLOW);
				case INFO -> shape = new Circle(SIZE, Color.LIGHTBLUE);
				case DEBUG -> shape = new Circle(SIZE, Color.CORNFLOWERBLUE);
				case TRACE -> shape = new Circle(SIZE, Color.DODGERBLUE);
				default -> throw new IllegalArgumentException("Unsupported logging level");
			}
			shape.setOpacity(0.65);

			// Wrap and provide right-side padding to give the indicator space between it and the line no.
			HBox wrapper = new HBox(shape);
			wrapper.setAlignment(Pos.CENTER);
			wrapper.setPadding(PADDING);
			container.addHorizontal(wrapper);
		}

		@Override
		public void install(@Nonnull Editor editor) {
			// no-op
		}

		@Override
		public void uninstall(@Nonnull Editor editor) {
			// no-op
		}
	}
}
