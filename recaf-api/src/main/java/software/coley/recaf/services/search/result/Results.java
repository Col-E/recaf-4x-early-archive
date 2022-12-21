package software.coley.recaf.services.search.result;

import jakarta.annotation.Nonnull;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Results wrapper for a search operation.
 *
 * @author Matt Coley
 */
public class Results {
	private final SortedSet<Result<?>> results = new TreeSet<>();

	public void add(@Nonnull Result<?> result) {
		results.add(result);
	}

	@Nonnull
	public SortedSet<Result<?>> getResults() {
		return results;
	}
}
