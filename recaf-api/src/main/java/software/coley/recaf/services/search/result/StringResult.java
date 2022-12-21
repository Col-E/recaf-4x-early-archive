package software.coley.recaf.services.search.result;

import jakarta.annotation.Nonnull;

/**
 * Result of a string match.
 *
 * @author Matt Coley
 */
public class StringResult extends Result<String> {
	private final String value;

	/**
	 * @param location
	 * 		Result location.
	 * @param value
	 * 		Matched value.
	 */
	public StringResult(@Nonnull Location location, @Nonnull String value) {
		super(location);
		this.value = value;
	}

	@Nonnull
	@Override
	protected String getValue() {
		return value;
	}
}
