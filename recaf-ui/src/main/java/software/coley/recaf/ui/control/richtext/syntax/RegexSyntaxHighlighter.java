package software.coley.recaf.ui.control.richtext.syntax;

import com.google.gson.annotations.SerializedName;
import jakarta.annotation.Nonnull;
import jregex.Matcher;
import jregex.Pattern;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import software.coley.collections.Lists;
import software.coley.recaf.util.RegexUtil;

import java.util.*;

/**
 * Regex backed syntax highlighter.
 *
 * @author Matt Coley
 */
public class RegexSyntaxHighlighter implements SyntaxHighlighter {
	private static final Map<List<Rule>, Pattern> patternCache = new HashMap<>();
	private final Rule rootRule;

	/**
	 * @param rootRule
	 * 		Root rule, constituting a language by its sub-rules.
	 */
	public RegexSyntaxHighlighter(Rule rootRule) {
		this.rootRule = rootRule;
	}

	@Nonnull
	@Override
	public StyleSpans<Collection<String>> createStyleSpans(@Nonnull String text, int start, int end) {
		StyleSpansBuilder<Collection<String>> builder = new StyleSpansBuilder<>();
		Region region = new Region(text, null, rootRule, start, end);
		region.split(rootRule.getSubRules());
		region.visitBuilder(builder);
		return builder.create();
	}

	/**
	 * @param rules
	 * 		Rules to match against.
	 *
	 * @return Compiled regex pattern from the given rules.
	 */
	private static Pattern getCombinedPattern(List<Rule> rules) {
		// Cache results. Very likely to encounter rule-collection again which makes it wise to save
		// the result instead of re-computing every time.
		return patternCache.computeIfAbsent(rules, RegexSyntaxHighlighter::createCombinedPattern);
	}

	/**
	 * @param rules
	 * 		Rules to match against.
	 *
	 * @return Compiled regex pattern from the given rules.
	 */
	private static Pattern createCombinedPattern(List<Rule> rules) {
		// Dummy pattern
		if (rules.isEmpty()) return RegexUtil.pattern("({EMPTY}EMPTY)");
		StringBuilder sb = new StringBuilder();
		for (Rule rule : rules) {
			String pattern = rule.getRegex();
			sb.append("({").append(rule.getName()).append("}").append(pattern).append(")|");
		}
		return RegexUtil.pattern(sb.substring(0, sb.length() - 1));
	}

	/**
	 * @param rules
	 * 		Rules to search for.
	 * @param matcher
	 * 		Matcher to pull rule name from using matched group's name.
	 *
	 * @return Rule with matching name as matched group.
	 */
	private static Rule getRuleFromMatcher(Collection<Rule> rules, Matcher matcher) {
		return rules.stream()
				.filter(rule -> matcher.group(rule.getName()) != null)
				.findFirst()
				.orElse(null);
	}

	/**
	 * Splittable region breaking down rule matches into ranges and sub-ranges.
	 */
	private static class Region {
		private final List<Region> children = new ArrayList<>();
		private final Region parent;
		private final String text;
		private final Rule rule;
		private final int start;
		private final int end;

		/**
		 * Constructor for matching a rule.
		 *
		 * @param text
		 * 		Complete text.
		 * @param parent
		 * 		Parent region.
		 * @param rule
		 * 		Rule associated with region.
		 * @param start
		 * 		Start offset in complete text.
		 * @param end
		 * 		End offset in complete text.
		 */
		private Region(String text, Region parent, Rule rule, int start, int end) {
			this.text = text;
			this.parent = parent;
			this.rule = rule;
			this.start = start;
			this.end = end;
		}

		/**
		 * Splits this node into subregions based on the given rules.
		 *
		 * @param rules
		 * 		Rules to match and split by.
		 */
		public void split(List<Rule> rules) {
			// Skip when no rules are given.
			if (rules.isEmpty())
				return;

			// Match within give region
			String localText = text.substring(start, end);
			Matcher matcher = getCombinedPattern(rules).matcher(localText);
			while (matcher.find()) {
				// Create region from found match
				int localStart = matcher.start();
				int localEnd = matcher.end();
				Rule matchedRule = getRuleFromMatcher(rules, matcher);
				Region localChild = new Region(text, this, matchedRule, start + localStart, start + localEnd);

				// Break the new region into smaller ones if the rule associated with the match has sub-rules.
				List<Rule> subrules = matchedRule.getSubRules();
				if (!subrules.isEmpty()) localChild.split(subrules);

				// Add child (splitting technically handled in output logic)
				children.add(localChild);
			}
		}

		/**
		 * Appends style spans to the given builder.
		 * The classes for each span take into account this classes of the {@link #rule} and the {@link #parent} rules.
		 *
		 * @param builder
		 * 		Builder to append to.
		 */
		public void visitBuilder(StyleSpansBuilder<Collection<String>> builder) {
			int lastEnd = start;

			// Add children
			for (Region child : children) {
				int childStart = child.start;

				// Append text not matched at start
				if (childStart > lastEnd) {
					int len = childStart - lastEnd;
					builder.add(child.unmatchedClasses(), len);
				}

				// Append child content
				child.visitBuilder(builder);
				lastEnd = child.end;
			}

			// Append remaining unmatched text
			int len = end - lastEnd;
			builder.add(currentClasses(), len);
		}

		/**
		 * @return List of classes for matching the {@link #rule} in this region.
		 */
		private List<String> currentClasses() {
			if (parent == null) return Collections.emptyList();
			return Lists.combine(parent.currentClasses(), rule.classes);
		}

		/**
		 * @return List of classes for unmatched sections in this region.
		 */
		private List<String> unmatchedClasses() {
			if (parent == null) return Collections.emptyList();
			return Lists.combine(parent.unmatchedClasses(), parent.rule.classes);
		}
	}

	/**
	 * Regex rule wrapper type.
	 */
	public static class Rule {
		@SerializedName("name")
		private final String name;
		@SerializedName("regex")
		private final String regex;
		@SerializedName("classes")
		private final List<String> classes;
		@SerializedName("sub-rules")
		private final List<Rule> subRules;

		public Rule(@Nonnull String name, @Nonnull String regex,
					@Nonnull List<String> classes, @Nonnull List<Rule> subRules) {
			this.name = name;
			this.regex = regex;
			this.classes = classes;
			this.subRules = subRules;
		}

		@Nonnull
		public String getName() {
			return name;
		}

		@Nonnull
		public String getRegex() {
			return regex;
		}

		@Nonnull
		public List<String> getClasses() {
			return classes;
		}

		@Nonnull
		public List<Rule> getSubRules() {
			return subRules;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Rule rule = (Rule) o;

			if (!name.equals(rule.name)) return false;
			if (!regex.equals(rule.regex)) return false;
			if (!classes.equals(rule.classes)) return false;
			return subRules.equals(rule.subRules);
		}

		@Override
		public int hashCode() {
			int result = name.hashCode();
			result = 31 * result + regex.hashCode();
			result = 31 * result + classes.hashCode();
			result = 31 * result + subRules.hashCode();
			return result;
		}
	}
}
