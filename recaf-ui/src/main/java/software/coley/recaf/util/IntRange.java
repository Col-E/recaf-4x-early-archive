package software.coley.recaf.util;

/**
 * Representation of a range.
 *
 * @param start
 * 		Start value.
 * @param end
 * 		End value.
 *
 * @author Matt Coley
 */
public record IntRange(int start, int end) implements Comparable<IntRange> {
	/**
	 * @return Length of the range.
	 */
	public int length() {
		return end() - start();
	}

	/**
	 * @return {@code true} when {@link #length()} is {@code 0}.
	 */
	public boolean empty() {
		return length() == 0;
	}

	/**
	 * @param startInclusive
	 * 		Flag to indicate inclusive range check against {@link #start()}.
	 * @param endInclusive
	 * 		Flag to indicate inclusive range check against {@link #end()}.
	 * @param value
	 * 		Position to check.
	 *
	 * @return {@code true} when the position is within this range.
	 */
	public boolean isBetween(boolean startInclusive, boolean endInclusive, int value) {
		if (startInclusive ? value >= start : value > start)
			return endInclusive ? value <= end : value < end;
		return false;
	}

	/**
	 * @param length
	 * 		Extension size.
	 *
	 * @return Copy of range, with end offset forwards by length.
	 */
	public IntRange extendForwards(int length) {
		return new IntRange(start(), end() + length);
	}

	/**
	 * @param length
	 * 		Extension size.
	 *
	 * @return Copy of range, with start offset backwards by length.
	 */
	public IntRange extendBackwards(int length) {
		return new IntRange(Math.max(0, start() - length), end());
	}

	@Override
	public int compareTo(IntRange o) {
		return Integer.compare(start, o.start);
	}
}
