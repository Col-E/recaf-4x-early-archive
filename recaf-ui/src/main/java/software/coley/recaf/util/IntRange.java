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
public record IntRange(int start, int end) {
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
}
