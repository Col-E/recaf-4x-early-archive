package software.coley.recaf.ui.control.richtext.syntax;

import jakarta.annotation.Nonnull;
import org.fxmisc.richtext.model.StyleSpans;
import software.coley.recaf.ui.control.richtext.Editor;

import java.util.Collection;

/**
 * Outline of basic syntax highlighting.
 *
 * @author Matt Coley
 * @see RegexSyntaxHighlighter Regex implementation.
 */
public interface SyntaxHighlighter {
	/**
	 * @param text
	 * 		Full text.
	 * @param start
	 * 		Start range in text to style.
	 * @param end
	 * 		End range in text to style.
	 *
	 * @return Spans for RichTextFX.
	 */
	@Nonnull
	StyleSpans<Collection<String>> createStyleSpans(@Nonnull String text, int start, int end);

	/**
	 * Called when the syntax highlighter is {@link Editor#setSyntaxHighlighter(SyntaxHighlighter) installed} into
	 * the given editor.
	 * <br>
	 * Something a syntax highlighter may want to do is install a custom stylesheet via {@link Editor#getStylesheets()}.
	 *
	 * @param editor
	 * 		Editor installed to.
	 */
	default void install(@Nonnull Editor editor) {
		// no-op by default
	}

	/**
	 * Called when the syntax highlighter is removed from the given editor.
	 * <br>
	 * Should clean up any actions done in {@link #install(Editor)}.
	 *
	 * @param editor
	 * 		Editor removed from.
	 */
	default void uninstall(@Nonnull Editor editor) {
		// no-op by default
	}
}
