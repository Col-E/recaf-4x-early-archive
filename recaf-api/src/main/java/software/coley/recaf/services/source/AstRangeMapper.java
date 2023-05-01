package software.coley.recaf.services.source;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.openrewrite.*;
import org.openrewrite.java.JavaPrinter;
import org.openrewrite.java.UpdateSourcePositions;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Space;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Range;
import software.coley.recaf.analytics.logging.DebuggingLogger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.util.StringDiff;

import java.util.*;

import static org.openrewrite.Tree.randomId;

/**
 * Mostly copied from {@link UpdateSourcePositions}, but with a few modifications.
 *
 * @author Matt Coley
 */
public class AstRangeMapper {
	private static final DebuggingLogger logger = Logging.get(AstRangeMapper.class);

	/**
	 * @param tree
	 * 		Tree to map ranges of.
	 * @param backingText
	 * 		Optional backing text that is the origin of the tree.
	 * 		Can be used for detecting dropped tokens, which OpenRewrite does when it detects invalid code in some cases.
	 * 		Technically, the behavior is undefined, but that's how it works currently <i>(which is ideal actually,
	 * 		as opposed to just crashing/refusing to parse)</i>
	 *
	 * @return Sorted map of ranges to tree nodes.
	 * Ranges that appear first in the AST are first as keys.
	 * If two tree nodes have the same start position, the range is used to differentiate, such that the containing node is first.
	 */
	@Nonnull
	public static SortedMap<Range, Tree> computeRangeToTreeMapping(@Nonnull Tree tree, @Nullable String backingText) {
		// Check if there's a cached range mapping.
		Optional<AstRangeMappingMarker> opt = tree.getMarkers().findFirst(AstRangeMappingMarker.class);
		if (opt.isPresent())
			return opt.get().rangeMap;

		// Nope, gotta compute the mapping.
		SortedMap<Range, Tree> rangeMap = new TreeMap<>((o1, o2) -> {
			if (o1 == o2) return 0;
			int off1 = o1.getStart().getOffset();
			int off2 = o2.getStart().getOffset();
			int cmp = Integer.compare(off1, off2);
			if (cmp == 0) {
				off1 = o1.getEnd().getOffset();
				off2 = o2.getEnd().getOffset();
				cmp = -Integer.compare(off1, off2);
			}
			return cmp;
		});
		DiffHelper helper = backingText == null ? null : new DiffHelper(backingText, tree);
		PositionPrintOutputCapture ppoc = new PositionPrintOutputCapture(helper);
		JavaPrinter<ExecutionContext> printer = new JavaPrinter<>() {
			final JavaPrinter<ExecutionContext> spacePrinter = new JavaPrinter<>();

			@Nullable
			@Override
			public J visit(@Nullable Tree tree, @Nonnull PrintOutputCapture<ExecutionContext> outputCapture) {
				if (tree == null) {
					return null;
				}

				J t = (J) tree;

				PositionPrintOutputCapture prefix = new PositionPrintOutputCapture(helper, ppoc.posInCurrent, ppoc.line, ppoc.column);
				spacePrinter.visitSpace(t.getPrefix(), Space.Location.ANY, prefix);

				Range.Position startPosition = new Range.Position(prefix.posInBacking, prefix.line, prefix.column);
				t = super.visit(tree, outputCapture);
				Range.Position endPosition = new Range.Position(ppoc.posInBacking, ppoc.line, ppoc.column);
				Range range = new Range(randomId(), startPosition, endPosition);
				rangeMap.put(range, t);

				return t;
			}

			@Override
			protected void visitModifier(@Nonnull J.Modifier modifier, PrintOutputCapture<ExecutionContext> p) {
				PositionPrintOutputCapture prefix = new PositionPrintOutputCapture(helper, ppoc.posInCurrent, ppoc.line, ppoc.column);
				spacePrinter.visitSpace(modifier.getPrefix(), Space.Location.ANY, prefix);

				Range.Position startPosition = new Range.Position(prefix.posInBacking, prefix.line, prefix.column);
				super.visitModifier(modifier, p);
				Range.Position endPosition = new Range.Position(ppoc.posInBacking, ppoc.line, ppoc.column);
				Range range = new Range(randomId(), startPosition, endPosition);
				rangeMap.put(range, modifier);
			}

		};
		printer.visit(tree, ppoc);

		// Save mapping for potential repeated operations.
		tree.getMarkers().add(new AstRangeMappingMarker(UUID.randomUUID(), rangeMap));
		return rangeMap;
	}

	private record AstRangeMappingMarker(@Nonnull UUID uuid,
										 @Nonnull SortedMap<Range, Tree> rangeMap) implements Marker {
		@Nonnull
		@Override
		public UUID getId() {
			return uuid;
		}

		@Nonnull
		@Override
		@SuppressWarnings("unchecked")
		public <M extends Marker> M withId(@Nonnull UUID id) {
			return (M) new AstRangeMappingMarker(id, rangeMap);
		}
	}

	private static class PositionPrintOutputCapture extends PrintOutputCapture<ExecutionContext> {
		private final DiffHelper diffHelper;
		int posInBacking = 0;
		int posInCurrent = 0;
		int column = 0;
		int line = 1;
		private boolean lineBoundary;

		public PositionPrintOutputCapture(@Nullable DiffHelper diffHelper) {
			super(new InMemoryExecutionContext());
			this.diffHelper = diffHelper;
		}

		public PositionPrintOutputCapture(@Nullable DiffHelper diffHelper, int pos, int line, int column) {
			this(diffHelper);
			this.posInBacking = pos;
			this.posInCurrent = pos;
			this.line = line;
			this.column = column;
		}

		@Nonnull
		@Override
		public PrintOutputCapture<ExecutionContext> append(char c) {
			next(c);
			return this;
		}

		@Nonnull
		@Override
		public PrintOutputCapture<ExecutionContext> append(@Nullable String text) {
			if (text != null) {
				int length = text.length();
				for (int i = 0; i < length; i++)
					next(text.charAt(i));
			}
			return this;
		}

		private void next(char c) {
			// Error correction. OpenRewrite will drop tokens it deems invalid/not-fitting the Java model at the given
			// point. As such we should assume a move forward in the position should skip over the content in the
			// original backing text that got skipped by OpenRewrite.
			if (diffHelper != null) {
				int start = posInBacking;

				// If text is present in the original but not in the AST, something got removed.
				// We want to move the offset for the backing text over the text removed, but keep
				// the current offset for the current text.
				//
				// This will keep the backing offset synchronized with what is next in the current text.
				StringDiff.Diff removed = diffHelper.getRemoved(posInBacking);
				if (removed != null)
					posInBacking += removed.textA().length();
				else {
					// If text is present in the AST but not the original, something got inserted.
					// We want to move the offset for the backing text to before the insertion, but keep
					// the current offset for the current text.
					//
					// This will keep the backing offset synchronized with what is next in the current text.
					StringDiff.Diff inserted = diffHelper.getInserted(posInCurrent);
					if (inserted != null)
						posInBacking -= inserted.textB().length();
					else {
						// If text is present in both but with some changes, we want to move the offset for
						// the backing text to after the change, but only up to the difference in size of the length.
						//
						// If we have change:
						//   void foo() {}
						// To:
						//   void xyz(int p) {}
						// Then the change is 'foo()' --> 'xyz(int p)'
						//
						// The current offsets should be sync'd at the start of the change, which would be:
						// 'foo' and 'xyz' in this case. The change swaps 'foo' to 'xyz' which does not require
						// any offset changes, but it does insert 'int p' which is a 5 character insertion.
						// Thus, in order to keep things synced, we will move the backing offset backwards by 5.
						//
						// Alternatively if we had the reverse, where 'xyz' is changed to 'foo' then we would
						// want to move the offset forwards by 5.
						StringDiff.Diff replaced = diffHelper.getReplaced(posInCurrent);
						if (replaced != null) {
							int aLength = replaced.textA().length();
							int bLength = replaced.textB().length();
							int lengthDiff = (bLength - aLength);
							posInBacking -= lengthDiff;
						}
					}
				}

				if (posInBacking != start)
					logger.debugging(l -> l.info("Text adjustments: {} --> {}", start, posInBacking));
			}

			// Standard positioning updates
			posInBacking++;
			moveNext(c);
		}

		private void moveNext(char c) {
			posInCurrent++;
			if (lineBoundary) {
				line++;
				column = 0;
				lineBoundary = false;
			} else {
				column++;
			}
			if (c == '\n') {
				lineBoundary = true;
			}
		}
	}

	/**
	 * Helper that holds diff information between the original text, and the test that the tree believes it is modeling.
	 * In some instances these models may not be aligned and will have to be de-conflicted.
	 */
	static class DiffHelper {
		private final List<StringDiff.Diff> diffs;

		/**
		 * @param backingText
		 * 		Original text that yields the tree.
		 * @param tree
		 * 		Tree model to compare against. Will be turned into a string representation.
		 */
		private DiffHelper(@Nonnull String backingText, @Nonnull Tree tree) {
			String treePrintText = tree.print(new Cursor(null, tree));
			this.diffs = StringDiff.diff(backingText, treePrintText);
		}

		/**
		 * Replacements can be detected in both the original and AST text representations.
		 * Thus, we can search in the 'b' ranges, which are those belonging to the AST and still maintain
		 * the ability to grab a result.
		 *
		 * @param pos
		 * 		Position in the AST's version of the text.
		 *
		 * @return Replace diff encompassing the given position,
		 * or {@code null} if no replace diff wraps the requested range.
		 */
		@Nullable
		public StringDiff.Diff getReplaced(int pos) {
			for (StringDiff.Diff diff : diffs) {
				if (diff.isReplace() && diff.inRangeB(pos)) {
					return diff;
				}
			}
			return null;
		}

		/**
		 * Removals can only be detected in the original text representation.
		 * Thus, we will search in the 'a' ranges, which are those belonging to the original backing text.
		 *
		 * @param pos
		 * 		Position in the original backing version of the text.
		 *
		 * @return Removal diff encompassing the given position,
		 * or {@code null} if no removal diff wraps the requested range.
		 */
		@Nullable
		public StringDiff.Diff getRemoved(int pos) {
			for (StringDiff.Diff diff : diffs) {
				if (diff.isRemoval() && diff.inRangeA(pos)) {
					return diff;
				}
			}
			return null;
		}

		/**
		 * Insertions can only be detected in the AST's text representation.
		 * Thus, we will search in the 'b' ranges, which are those belonging to the AST.
		 *
		 * @param pos
		 * 		Position in the AST's version of the text.
		 *
		 * @return Insertion diff encompassing the given position,
		 * or {@code null} if no insertion diff wraps the requested range.
		 */
		@Nullable
		public StringDiff.Diff getInserted(int pos) {
			for (StringDiff.Diff diff : diffs) {
				if (diff.isInsert() && diff.inRangeB(pos)) {
					return diff;
				}
			}
			return null;
		}
	}
}
