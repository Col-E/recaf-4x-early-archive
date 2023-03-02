package software.coley.recaf.ui.control.richtext;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.StackPane;
import org.fxmisc.flowless.VirtualFlow;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.model.PlainTextChange;
import org.fxmisc.richtext.model.StyleSpans;
import org.reactfx.Change;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;
import software.coley.recaf.ui.control.richtext.bracket.SelectedBracketTracking;
import software.coley.recaf.ui.control.richtext.linegraphics.RootLineGraphicFactory;
import software.coley.recaf.ui.control.richtext.syntax.StyleResult;
import software.coley.recaf.ui.control.richtext.syntax.SyntaxHighlighter;
import software.coley.recaf.ui.control.richtext.syntax.SyntaxUtil;
import software.coley.recaf.util.FxThreadUtil;
import software.coley.recaf.util.IntRange;
import software.coley.recaf.util.ReflectUtil;
import software.coley.recaf.util.Unchecked;
import software.coley.recaf.util.threading.ThreadPoolFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Modular text editor control.
 * <ul>
 *     <li>Configure syntax with {@link #setSyntaxHighlighter(SyntaxHighlighter)}</li>
 *     <li>Configure selected bracket tracking with {@link #setSelectedBracketTracking(SelectedBracketTracking)}</li>
 *     <li>Configure line graphics via {@link #getRootLineGraphicFactory()}</li>
 * </ul>
 *
 * @author Matt Coley
 */
public class Editor extends StackPane {
	public static final int SHORT_DELAY_MS = 150;
	private final CodeArea codeArea = new CodeArea();
	private final VirtualFlow<?, ?> virtualFlow;
	private final ExecutorService syntaxPool = ThreadPoolFactory.newSingleThreadExecutor("syntax-highlight");
	private final RootLineGraphicFactory rootLineGraphicFactory = new RootLineGraphicFactory(this);
	private final EventStream<Change<Integer>> caretPosEventStream;
	private SyntaxHighlighter syntaxHighlighter;
	private SelectedBracketTracking selectedBracketTracking;

	/**
	 * New editor instance.
	 */
	public Editor() {
		getStylesheets().add("/style/code-editor.css");
		getChildren().add(new VirtualizedScrollPane<>(codeArea));
		virtualFlow = Unchecked.get(() -> ReflectUtil.quietGet(codeArea, GenericStyledArea.class.getDeclaredField("virtualFlow")));

		// Do not want text wrapping in a code editor.
		codeArea.setWrapText(false);

		// Set paragraph graphic factory to the user-configurable root graphics factory.
		codeArea.setParagraphGraphicFactory(rootLineGraphicFactory);

		// This property copies the style of adjacent characters when typing (instead of having no style).
		// It may not seem like much, but it makes our restyle range computation logic much simpler.
		// Consider a multi-line comment. If you had this set to use the initial style (none) it would break up
		// multi-line comment style spans. We would have to re-stitch them together based on the inserted text position
		// which would be a huge pain in the ass.
		codeArea.setUseInitialStyleForInsertion(false);

		// Register a text change listener and use the inserted/removed text content to determine what portions
		// of the document need to be restyled.
		codeArea.plainTextChanges()
				.successionEnds(Duration.ofMillis(SHORT_DELAY_MS))
				.addObserver(changes -> {
					if (syntaxHighlighter != null) {
						schedule(syntaxPool, () -> {
							IntRange range = SyntaxUtil.getRangeForRestyle(getText(), getStyleSpans(),
									syntaxHighlighter, changes);
							int start = range.start();
							int end = range.end();
							return new StyleResult(syntaxHighlighter.createStyleSpans(getText(), start, end), start);
						}, result -> codeArea.setStyleSpans(result.position(), result.spans()));
					}
				});

		// Create event-streams for various events.
		caretPosEventStream = EventStreams.changesOf(codeArea.caretPositionProperty());
	}

	/**
	 * Redraw visible paragraph graphics.
	 * <br>
	 * <b>Must be called on FX thread.</b>
	 */
	public void redrawParagraphGraphics() {
		int startParagraphIndex = Math.max(0, codeArea.firstVisibleParToAllParIndex() - 1);
		int endParagraphIndex = Math.min(codeArea.getParagraphs().size() - 1, codeArea.lastVisibleParToAllParIndex());
		for (int i = startParagraphIndex; i <= endParagraphIndex; i++)
			codeArea.recreateParagraphGraphic(i);
	}

	/**
	 * @return Current style spans for the entire document.
	 */
	public StyleSpans<Collection<String>> getStyleSpans() {
		return codeArea.getStyleSpans(0, getTextLength());
	}

	/**
	 * @return Current length of document text.
	 */
	public int getTextLength() {
		return codeArea.getLength();
	}

	/**
	 * @return Current document text.
	 */
	@Nonnull
	public String getText() {
		return Objects.requireNonNullElse(codeArea.getText(), "");
	}

	/**
	 * @param text
	 * 		Text to set.
	 */
	public void setText(@Nullable String text) {
		// Filter input
		if (text == null)
			text = "";

		// Prepare reset of caret/scroll position
		codeArea.textProperty().addListener(new CaretReset(codeArea.getCaretPosition()));
		codeArea.textProperty().addListener(new ScrollReset(virtualFlow.getFirstVisibleIndex()));

		// Replace the full text document
		if (getTextLength() == 0) {
			codeArea.appendText(text);
		} else {
			codeArea.replaceText(text);
		}
	}

	/**
	 * Delegates to {@link CodeArea#textProperty()}.
	 * <br>
	 * Do not use this to set text. Instead, use {@link #setText(String)}.
	 *
	 * @return Property representation of {@link #getText()}.
	 */
	@Nonnull
	public ObservableValue<String> textProperty() {
		return codeArea.textProperty();
	}

	/**
	 * Delegates to {@link CodeArea#plainTextChanges()}.
	 *
	 * @return Event stream for changes to {@link #textProperty()}.
	 */
	@Nonnull
	public EventStream<PlainTextChange> getTextChangeEventStream() {
		return codeArea.plainTextChanges();
	}

	/**
	 * @return Event stream wrapper for {@link CodeArea#caretPositionProperty()}.
	 */
	@Nonnull
	public EventStream<Change<Integer>> getCaretPosEventStream() {
		return caretPosEventStream;
	}

	/**
	 * @return The root line graphics factory.
	 */
	@Nonnull
	public RootLineGraphicFactory getRootLineGraphicFactory() {
		return rootLineGraphicFactory;
	}

	/**
	 * @return Current highlighter.
	 */
	@Nullable
	public SyntaxHighlighter getSyntaxHighlighter() {
		return syntaxHighlighter;
	}

	/**
	 * @param syntaxHighlighter
	 * 		Highlighter to use.
	 */
	public void setSyntaxHighlighter(@Nullable SyntaxHighlighter syntaxHighlighter) {
		// Uninstall prior.
		SyntaxHighlighter previousSyntaxHighlighter = this.syntaxHighlighter;
		if (previousSyntaxHighlighter != null)
			previousSyntaxHighlighter.uninstall(this);

		// Set and install new instance.
		this.syntaxHighlighter = syntaxHighlighter;
		if (syntaxHighlighter != null) {
			syntaxHighlighter.install(this);
			codeArea.setStyleSpans(0, syntaxHighlighter.createStyleSpans(getText(), 0, getTextLength()));
		}
	}

	/**
	 * @param selectedBracketTracking
	 * 		New selected bracket tracking implementation, or {@code null} to disable selected bracket tracking.
	 */
	public void setSelectedBracketTracking(@Nullable SelectedBracketTracking selectedBracketTracking) {
		// Uninstall prior.
		SelectedBracketTracking previousSelectedBracketTracking = this.selectedBracketTracking;
		if (previousSelectedBracketTracking != null)
			previousSelectedBracketTracking.uninstall(this);

		// Set and install new instance.
		this.selectedBracketTracking = selectedBracketTracking;
		if (selectedBracketTracking != null)
			selectedBracketTracking.install(this);
	}

	/**
	 * @return Selected bracket tracking implementation.
	 */
	@Nullable
	public SelectedBracketTracking getSelectedBracketTracking() {
		return selectedBracketTracking;
	}

	/**
	 * @return Backing text editor component.
	 */
	@Nonnull
	public CodeArea getCodeArea() {
		return codeArea;
	}

	/**
	 * @param supplierService
	 * 		Executor service to run the supplier on.
	 * @param supplier
	 * 		Value supplier.
	 * @param consumer
	 * 		Value consumer, run on the JavaFX UI thread.
	 * @param <T>
	 * 		Value type.
	 */
	public <T> void schedule(@Nonnull ExecutorService supplierService,
							 @Nonnull Supplier<T> supplier, @Nonnull Consumer<T> consumer) {
		CompletableFuture.supplyAsync(supplier, supplierService)
				.thenAcceptAsync(consumer, FxThreadUtil.executor());
	}

	/**
	 * Handles re-scrolling to the same location after the text document has been updated.
	 *
	 * @see #setText(String)
	 */
	private class ScrollReset implements ChangeListener<String> {
		private final int firstIndex;

		ScrollReset(int firstIndex) {
			this.firstIndex = firstIndex;
		}

		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
			// Of the multiple ways to reset scroll position, this seems to be the most reliable.
			// It's not pixel perfect, but it shouldn't be too jarring since resetting content should
			// not happen often.
			virtualFlow.showAsFirst(firstIndex);
			observable.removeListener(this);
		}
	}

	/**
	 * Handles re-positioning the caret to the same location after the text document has been updated.
	 *
	 * @see #setText(String)
	 */
	private class CaretReset implements ChangeListener<String> {
		private final int pos;

		CaretReset(int pos) {
			this.pos = pos;
		}

		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
			codeArea.moveTo(pos);
			observable.removeListener(this);
		}
	}
}
