package software.coley.recaf.ui.control.richtext.syntax;

import com.google.gson.annotations.SerializedName;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Regex rule data type.
 *
 * @author Matt Coley
 * @see RegexSyntaxHighlighter
 */
public class RegexRule {
	@SerializedName("name")
	private final String name;
	@SerializedName("regex")
	private final String regex;
	@SerializedName("classes")
	private final List<String> classes;
	@SerializedName("sub-rules")
	private final List<RegexRule> subRules;
	@SerializedName("backtrack-mark")
	private final String backtrackMark;
	@SerializedName("advance-mark")
	private final String advanceMark;

	/**
	 * @param name
	 * 		Name of rule. Must not contain any whitespaces or special characters.
	 * @param regex
	 * 		Regex pattern.
	 * @param classes
	 * 		Style classes to apply to matched ranges within a {@link SyntaxHighlighter}.
	 * @param subRules
	 * 		Sub-rules which can be used to create sub-matches within ranges matched by this rule.
	 * @param backtrackMark
	 * 		Used for rules that are variable length. Indicates the start text of such matches.
	 * 		See {@link RegexSyntaxHighlighter#expandRange(String, int, int)} for usage.
	 * @param advanceMark
	 * 		Used for rules that are variable length. Indicates the end text of such matches.
	 * 		See {@link RegexSyntaxHighlighter#expandRange(String, int, int)} for usage.
	 */
	public RegexRule(@Nonnull String name, @Nonnull String regex,
					 @Nonnull List<String> classes, @Nonnull List<RegexRule> subRules,
					 @Nullable String backtrackMark, @Nullable String advanceMark) {
		this.name = name;
		this.regex = regex;
		this.classes = classes;
		this.subRules = subRules;
		this.backtrackMark = backtrackMark;
		this.advanceMark = advanceMark;
	}

	/**
	 * @return Name of rule. Must not contain any whitespaces or special characters.
	 */
	@Nonnull
	public String getName() {
		return name;
	}

	/**
	 * @return Regex pattern.
	 */
	@Nonnull
	public String getRegex() {
		return regex;
	}

	/**
	 * @return Style classes to apply to matched ranges within a {@link SyntaxHighlighter}.
	 */
	@Nonnull
	public List<String> getClasses() {
		return classes;
	}

	/**
	 * @return Sub-rules which can be used to create sub-matches within ranges matched by this rule.
	 */
	@Nonnull
	public List<RegexRule> getSubRules() {
		return subRules;
	}

	/**
	 * @return Used for rules that are variable length. Indicates the start text of such matches.
	 *
	 * @see RegexSyntaxHighlighter#expandRange(String, int, int) for usage.
	 */
	@Nullable
	public String getBacktrackMark() {
		return backtrackMark;
	}

	/**
	 * @return Used for rules that are variable length. Indicates the end text of such matches.
	 *
	 * @see RegexSyntaxHighlighter#expandRange(String, int, int) for usage.
	 */
	@Nullable
	public String getAdvanceMark() {
		return advanceMark;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		RegexRule rule = (RegexRule) o;

		if (!name.equals(rule.name)) return false;
		if (!regex.equals(rule.regex)) return false;
		if (!classes.equals(rule.classes)) return false;
		if (!subRules.equals(rule.subRules)) return false;
		if (!Objects.equals(backtrackMark, rule.backtrackMark)) return false;
		return Objects.equals(advanceMark, rule.advanceMark);
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + regex.hashCode();
		result = 31 * result + classes.hashCode();
		result = 31 * result + subRules.hashCode();
		result = 31 * result + (backtrackMark != null ? backtrackMark.hashCode() : 0);
		result = 31 * result + (advanceMark != null ? advanceMark.hashCode() : 0);
		return result;
	}
}
