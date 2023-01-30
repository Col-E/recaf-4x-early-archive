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
import software.coley.recaf.ui.control.richtext.syntax.SyntaxHighlighter;
import software.coley.recaf.util.FxThreadUtil;
import software.coley.recaf.util.ReflectUtil;
import software.coley.recaf.util.Unchecked;
import software.coley.recaf.util.threading.ThreadPoolFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Modular text editor control.
 *
 * @author Matt Coley
 */
public class Editor extends StackPane {
	private static final String styleSheetPath;
	private final CodeArea codeArea = new CodeArea();
	private final VirtualFlow<?, ?> virtualFlow;
	private final ExecutorService syntaxPool = ThreadPoolFactory.newSingleThreadExecutor("syntax-highlight");
	private SyntaxHighlighter syntaxHighlighter;

	static {
		styleSheetPath = Editor.class.getResource("/style/code-editor.css").toExternalForm();
	}

	/**
	 * New editor instance.
	 */
	public Editor() {
		getStylesheets().add(styleSheetPath);
		getChildren().add(new VirtualizedScrollPane<>(codeArea));
		virtualFlow = Unchecked.get(() -> ReflectUtil.quietGet(codeArea, GenericStyledArea.class.getDeclaredField("virtualFlow")));
	}

	/**
	 * @param syntaxHighlighter
	 * 		Highlighter to use.
	 */
	public void setSyntaxHighlighter(@Nullable SyntaxHighlighter syntaxHighlighter) {
		// Uninstall prior
		SyntaxHighlighter previousSyntaxHighlighter = this.syntaxHighlighter;
		if (previousSyntaxHighlighter != null)
			previousSyntaxHighlighter.uninstall(this);

		// Set and install new instance
		this.syntaxHighlighter = syntaxHighlighter;
		if (syntaxHighlighter != null) {
			syntaxHighlighter.install(this);
			codeArea.setStyleSpans(0, syntaxHighlighter.createStyleSpans(getText(), 0, getTextLength()));
		}
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

		// Schedule syntax highlight for complete document
		if (syntaxHighlighter != null) {
			String textCopy = text;
			schedule(syntaxPool, () -> syntaxHighlighter.createStyleSpans(textCopy, 0, textCopy.length()),
					style -> codeArea.setStyleSpans(0, style));
		}
	}

	/**
	 * Delegates to {@link CodeArea#textProperty()}
	 *
	 * @return Property representation of {@link #getText()}.
	 */
	@Nonnull
	public ObservableValue<String> textProperty() {
		return codeArea.textProperty();
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
