package software.coley.recaf.services.search.result;

import jakarta.annotation.Nonnull;

import java.util.Objects;

/**
 * The base result contains location information of the matched value.
 *
 * @author Matt Coley
 */
public abstract class Result<T> implements Comparable<Result<?>> {
	private final Location location;

	/**
	 * @param location
	 * 		Result location.
	 */
	public Result(@Nonnull Location location) {
		this.location = location;
	}

	/**
	 * @return Wrapped value, used internally for {@link #toString()}.
	 */
	@Nonnull
	protected abstract T getValue();

	/**
	 * @return Location of result.
	 */
	@Nonnull
	public Location getLocation() {
		return location;
	}

	@Override
	public int compareTo(@Nonnull Result<?> o) {
		if (o == this)
			return 0;
		return location.compareTo(o.location);
	}

	@Override
	public String toString() {
		return "Result{value=" + getValue() + ", Location=" + location + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Result<?> result = (Result<?>) o;
		return Objects.equals(location, result.location) &&
				Objects.equals(getValue(), result.getValue());
	}

	@Override
	public int hashCode() {
		int result = location.hashCode();
		result = 31 * result + Objects.hashCode(getValue());
		return result;
	}
}