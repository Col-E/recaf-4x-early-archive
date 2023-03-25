package software.coley.recaf.services.source;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.Tree;
import org.openrewrite.java.JavaPrinter;
import org.openrewrite.java.UpdateSourcePositions;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Space;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Range;

import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import static org.openrewrite.Tree.randomId;

/**
 * Mostly copied from {@link UpdateSourcePositions}, but with a few modifications.
 *
 * @author Matt Coley
 */
public class AstRangeMapper {
	/**
	 * @param tree
	 * 		Tree to map ranges of.
	 *
	 * @return Sorted map of ranges to tree nodes.
	 * Ranges that appear first in the AST are first as keys.
	 * If two tree nodes have the same start position, the range is used to differentiate, such that the containing node is first.
	 */
	@Nonnull
	public static SortedMap<Range, Tree> mapRanges(@Nonnull Tree tree) {
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
		PositionPrintOutputCapture ppoc = new PositionPrintOutputCapture();
		JavaPrinter<ExecutionContext> printer = new JavaPrinter<>() {
			final JavaPrinter<ExecutionContext> spacePrinter = new JavaPrinter<>();

			@Nullable
			@Override
			public J visit(@Nullable Tree tree, @Nonnull PrintOutputCapture<ExecutionContext> outputCapture) {
				if (tree == null) {
					return null;
				}

				J t = (J) tree;

				PositionPrintOutputCapture prefix = new PositionPrintOutputCapture(ppoc.pos, ppoc.line, ppoc.column);
				spacePrinter.visitSpace(t.getPrefix(), Space.Location.ANY, prefix);

				Range.Position startPosition = new Range.Position(prefix.pos, prefix.line, prefix.column);
				t = super.visit(tree, outputCapture);
				Range.Position endPosition = new Range.Position(ppoc.pos, ppoc.line, ppoc.column);
				Range range = new Range(randomId(), startPosition, endPosition);
				rangeMap.put(range, t);

				return t;
			}

			@Override
			protected void visitModifier(@Nonnull J.Modifier modifier, PrintOutputCapture<ExecutionContext> p) {
				PositionPrintOutputCapture prefix = new PositionPrintOutputCapture(ppoc.pos, ppoc.line, ppoc.column);
				spacePrinter.visitSpace(modifier.getPrefix(), Space.Location.ANY, prefix);

				Range.Position startPosition = new Range.Position(prefix.pos, prefix.line, prefix.column);
				super.visitModifier(modifier, p);
				Range.Position endPosition = new Range.Position(ppoc.pos, ppoc.line, ppoc.column);
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
		int pos = 0;
		int line = 1;
		int column = 0;
		private boolean lineBoundary;

		public PositionPrintOutputCapture() {
			super(new InMemoryExecutionContext());
		}

		public PositionPrintOutputCapture(int pos, int line, int column) {
			this();
			this.pos = pos;
			this.line = line;
			this.column = column;
		}

		@Nonnull
		@Override
		public PrintOutputCapture<ExecutionContext> append(char c) {
			pos++;
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
			return this;
		}

		@Nonnull
		@Override
		public PrintOutputCapture<ExecutionContext> append(@Nullable String text) {
			if (text != null) {
				if (lineBoundary) {
					line++;
					column = 0;
					lineBoundary = false;
				}
				int length = text.length();
				pos += length;
				int numberOfLines = 0;
				int indexOfLastNewLine = -1;
				for (int i = 0; i < length; i++) {
					if (text.charAt(i) == '\n') {
						indexOfLastNewLine = i;
						numberOfLines++;
					}
				}
				if (numberOfLines > 0) {
					line += numberOfLines;
					column = length - indexOfLastNewLine;
				} else {
					column += length;
				}
			}
			return this;
		}
	}
}
