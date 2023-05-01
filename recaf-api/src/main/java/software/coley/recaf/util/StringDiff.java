package software.coley.recaf.util;

import org.openrewrite.shaded.jgit.diff.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapping around JGit diff logic.
 *
 * @author Matt Coley
 */
public class StringDiff {
	/**
	 * @param a
	 * 		Input.
	 * @param b
	 * 		Modified input.
	 *
	 * @return Diffs from {@code a} --> {@code b}.
	 */
	public static List<Diff> diff(String a, String b) {
		// Track line info.
		String[] aLines = a.split("\n");
		String[] bLines = b.split("\n");
		int[] aPositions = new int[aLines.length];
		int[] bPositions = new int[bLines.length];
		for (int i = 0; i < aPositions.length - 1; i++)
			aPositions[i + 1] = aPositions[i] + aLines[i].length() + 1;
		for (int i = 0; i < bPositions.length - 1; i++)
			bPositions[i + 1] = bPositions[i] + bLines[i].length() + 1;

		// Use JGit to diff.
		EditList diffs = DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.MYERS)
				.diff(RawTextComparator.DEFAULT,
						new RawText(a.getBytes(StandardCharsets.UTF_8)),
						new RawText(b.getBytes(StandardCharsets.UTF_8)));

		// Map to diff types that are easier to use.
		// We want raw positions, not line numbers, so we can do replace operations easily.
		List<Diff> ret = new ArrayList<>();
		for (Edit diff : diffs) {
			DiffType type = DiffType.from(diff.getType());
			int beginA = diff.getBeginA();
			int endA = diff.getEndA();
			int posStartA = aPositions[beginA];
			int posEndA = aPositions[endA];
			String contentA = pull(aLines, beginA, endA);
			int beginB = diff.getBeginB();
			int endB = diff.getEndB();
			int posStartB = bPositions[beginB];
			int posEndB = bPositions[endB];
			String contentB = pull(bLines, beginB, endB);

			// Shrink difference
			int lengthA = contentA.length();
			int lengthB = contentB.length();
			int commonLength = Math.min(lengthA, lengthB);
			int cutStart = 0;
			while (cutStart < commonLength && contentA.charAt(cutStart) == contentB.charAt(cutStart))
				cutStart++;
			int cutEnd = 0;
			while (cutEnd < commonLength && contentA.charAt(lengthA - cutEnd - 1) == contentB.charAt(lengthB - cutEnd - 1))
				cutEnd++;
			if (cutStart > 0) {
				posStartA += cutStart;
				posStartB += cutStart;
				contentA = contentA.substring(cutStart);
				contentB = contentB.substring(cutStart);
			}
			if (cutEnd > 0) {
				posEndA -= cutEnd;
				posEndB -= cutEnd;
				contentA = contentA.substring(0, contentA.length() - cutEnd);
				contentB = contentB.substring(0, contentB.length() - cutEnd);
			}

			// Check if shrinking changed type
			if (contentA.isEmpty() && !contentB.isEmpty())
				type = DiffType.INSERT;
			else if (!contentA.isEmpty() && contentB.isEmpty())
				type = DiffType.REMOVE;

			ret.add(new Diff(type, posStartA, posStartB, posEndA, posEndB, contentA, contentB));
		}
		return ret;
	}

	/**
	 * @param array
	 * 		Sources.
	 * @param start
	 * 		Source line start.
	 * @param end
	 * 		Source line end.
	 *
	 * @return Text from range.
	 */
	private static String pull(String[] array, int start, int end) {
		StringBuilder sb = new StringBuilder();
		for (int i = start; i < end; i++)
			sb.append(array[i]).append("\n");
		return sb.toString();
	}

	/**
	 * @param type
	 * 		Diff type.
	 * @param startA
	 * 		Start position of original different text.
	 * @param startB
	 * 		Start position of modified different text.
	 * @param endA
	 * 		End position of original different text.
	 * @param endB
	 * 		End position of modified different text.
	 * @param textA
	 * 		Original text in range.
	 * @param textB
	 * 		Modified text in range.
	 *
	 * @author Matt Coley
	 */
	public record Diff(DiffType type, int startA, int startB, int endA, int endB, String textA, String textB) {
		/**
		 * @return {@code true} when {@link #type()} is {@link DiffType#CHANGE}.
		 */
		public boolean isReplace() {
			return type == DiffType.CHANGE;
		}

		/**
		 * @return {@code true} when {@link #type()} is {@link DiffType#REMOVE}.
		 */
		public boolean isRemoval() {
			return type == DiffType.REMOVE;
		}

		/**
		 * @return {@code true} when {@link #type()} is {@link DiffType#INSERT}.
		 */
		public boolean isInsert() {
			return type == DiffType.INSERT;
		}

		/**
		 * @return {@code true} when {@link #type()} is {@link DiffType#EMPTY}.
		 */
		public boolean isEmpty() {
			return type == DiffType.EMPTY;
		}

		public boolean inRangeA(int pos) {
			return pos >= startA && pos <= endA;
		}

		public boolean inRangeB(int pos) {
			return pos >= startB && pos <= endB;
		}
	}

	/**
	 * Type of diff.
	 *
	 * @author Matt Coley
	 */
	public enum DiffType {
		CHANGE,
		INSERT,
		REMOVE,
		EMPTY;

		/**
		 * @param type
		 * 		JGit diff type.
		 *
		 * @return Our diff type.
		 */
		public static DiffType from(Edit.Type type) {
			return switch (type) {
				case INSERT -> INSERT;
				case DELETE -> REMOVE;
				case REPLACE -> CHANGE;
				case EMPTY -> EMPTY;
			};
		}
	}
}
