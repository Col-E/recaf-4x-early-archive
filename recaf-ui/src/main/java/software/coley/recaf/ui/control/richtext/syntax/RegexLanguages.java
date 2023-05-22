package software.coley.recaf.ui.control.richtext.syntax;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.annotation.Nonnull;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Pre-defined rule-sets to pattern-match languages for syntax highlighting.
 *
 * @author Matt Coley
 * @see RegexSyntaxHighlighter Highlighter implementation that accepts these rule-sets.
 */
public class RegexLanguages {
	private static final JsonMapper MAPPER = new JsonMapper();
	private static final RegexRule LANG_JAVA;
	private static final RegexRule LANG_XML;

	// Prevent construction
	private RegexLanguages() {
	}

	static {
		try {
			LANG_JAVA = read("/syntax/java.json");
			LANG_XML = read("/syntax/xml.json");
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to read syntax rules from resources", ex);
		}
	}

	@SuppressWarnings("all")
	@Nonnull
	private static RegexRule read(@Nonnull String path) throws IOException {
		JsonParser parser = MAPPER.createParser(new InputStreamReader(RegexLanguages.class.getResourceAsStream(path)));
		return parser.readValueAs(RegexRule.class);
	}

	/**
	 * @return Root rule for Java regex matching.
	 */
	@Nonnull
	public static RegexRule getJavaLanguage() {
		return LANG_JAVA;
	}

	/**
	 * @return Root rule for XML regex matching.
	 */
	@Nonnull
	public static RegexRule getXmlLanguage() {
		return LANG_XML;
	}
}
