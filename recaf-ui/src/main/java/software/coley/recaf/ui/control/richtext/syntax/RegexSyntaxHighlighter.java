package software.coley.recaf.ui.control.richtext.syntax;

import jakarta.annotation.Nonnull;
import jregex.Matcher;
import jregex.Pattern;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import software.coley.collections.Lists;
import software.coley.recaf.util.IntRange;
import software.coley.recaf.util.RegexUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Regex backed syntax highlighter.
 *
 * @author Matt Coley
 * @see RegexLanguages Predefined languages to pass to {@link RegexSyntaxHighlighter#RegexSyntaxHighlighter(RegexRule)}.
 */
public class RegexSyntaxHighlighter implements SyntaxHighlighter {
	private static final Map<List<RegexRule>, Pattern> patternCache = new ConcurrentHashMap<>();
	private final RegexRule rootRule;

	/**
	 * @param rootRule
	 * 		Root rule, constituting a language by its sub-rules.
	 */
	public RegexSyntaxHighlighter(RegexRule rootRule) {
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

	@Nonnull
	@Override
	public IntRange expandRange(@Nonnull String text, int initialStart, int initialEnd) {
		String rangeText = text.substring(initialStart, initialEnd);
		int start = initialStart;
		int end = initialEnd;

		// Any rule that has backtracking/advancing needs to be expanded so that if the change breaks the start/end
		// of one of these matches, the range covers the new start/end. Primary example of this is multi-line comments.
		//
		// Example: Deleting the last '/' will expand the range forwards.
		for (RegexRule rule : rootRule.getSubRules()) {
			String backtrackMark = rule.getBacktrackMark();
			String advanceMark = rule.getAdvanceMark();

			if (advanceMark != null && backtrackMark != null) {
				// If the range is a FULL match (from start to finish, no leading or trailing text)
				// then we do not need to change anything.
				if (rangeText.matches(rule.getRegex()))
					break;

				// The change in the text caused our pattern to break its original match.
				// We need to restyle a wider range.
				//
				// Advance forward if start of pattern exists, but no end of pattern exists
				if (rangeText.startsWith(backtrackMark)) {
					end = Math.max(end, text.indexOf(advanceMark, end) + advanceMark.length());

					// Rules will only expand in one direction, so if we made a match by forward expansion
					// then we do not need to do backtracking for this rule.
					//
					// We are done and do not need to check any other rules.
					break;
				}

				// Advance backwards if the end of a pattern exists, but no start of pattern exists
				if (rangeText.endsWith(advanceMark)) {
					int index = text.substring(0, start).lastIndexOf(backtrackMark);
					if (index >= 0) {
						start = index;

						// Rules will only expand in one direction, so if we made a match by backwards expansion
						// then we are done with this rule and do not need to check any other rules.
						break;
					}
				}
			}
		}
		return new IntRange(start, end);
	}

	/**
	 * @param rules
	 * 		Rules to match against.
	 *
	 * @return Compiled regex pattern from the given rules.
	 */
	private static Pattern getCombinedPattern(List<RegexRule> rules) {
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
	private static Pattern createCombinedPattern(List<RegexRule> rules) {
		// Dummy pattern
		if (rules.isEmpty()) return RegexUtil.pattern("({EMPTY}EMPTY)");
		StringBuilder sb = new StringBuilder();
		for (RegexRule rule : rules) {
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
	private static RegexRule getRuleFromMatcher(Collection<RegexRule> rules, Matcher matcher) {
		return rules.stream()
				.filter(rule -> matcher.group(rule.getName()) != null)
				.findFirst()
				.orElse(null);
	}

	/**
	 * Splittable region breaking down rule matches into ranges and sub-ranges.
	 */
	public static class Region {
		private final List<Region> children = new ArrayList<>();
		private final Region parent;
		private final String text;
		private final RegexRule rule;
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
		public Region(String text, Region parent, RegexRule rule, int start, int end) {
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
		public void split(List<RegexRule> rules) {
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
				RegexRule matchedRule = getRuleFromMatcher(rules, matcher);
				Region localChild = new Region(text, this, matchedRule, start + localStart, start + localEnd);

				// Break the new region into smaller ones if the rule associated with the match has sub-rules.
				List<RegexRule> subrules = matchedRule.getSubRules();
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
		public List<String> currentClasses() {
			if (parent == null) return Collections.emptyList();
			return Lists.combine(parent.currentClasses(), rule.getClasses());
		}

		/**
		 * @return List of classes for unmatched sections in this region.
		 */
		public List<String> unmatchedClasses() {
			if (parent == null) return Collections.emptyList();
			return Lists.combine(parent.unmatchedClasses(), parent.rule.getClasses());
		}
	}
}
