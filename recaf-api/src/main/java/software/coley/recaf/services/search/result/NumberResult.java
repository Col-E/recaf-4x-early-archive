package software.coley.recaf.services.search.result;

import jakarta.annotation.Nonnull;

/**
 * Result of a string match.
 *
 * @author Matt Coley
 */
public class NumberResult extends Result<Number> {
	private final Number value;

	/**
	 * @param location
	 * 		Result location.
	 * @param value
	 * 		Matched value.
	 */
	public NumberResult(@Nonnull Location location, @Nonnull Number value) {
		super(location);
		this.value = value;
	}

	@Nonnull
	@Override
	protected Number getValue() {
		return value;
	}
}
