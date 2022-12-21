package software.coley.recaf.services.search;

import jakarta.annotation.Nonnull;
import software.coley.recaf.info.AndroidClassInfo;
import software.coley.recaf.services.search.result.AndroidClassLocation;
import software.coley.recaf.services.search.result.Location;
import software.coley.recaf.services.search.result.Results;

import java.util.function.BiConsumer;

/**
 * Visitor for {@link AndroidClassInfo}
 *
 * @author Matt Coley
 */
public interface AndroidClassSearchVisitor extends SearchVisitor {
	/**
	 * Visits an Android class.
	 *
	 * @param resultSink
	 * 		Consumer to feed result values into, typically populating a {@link Results} instance.
	 * @param currentLocation
	 * 		Additional information about the current location.
	 * @param classInfo
	 * 		Class to visit.
	 */
	void visit(@Nonnull BiConsumer<Location, Object> resultSink,
			   @Nonnull AndroidClassLocation currentLocation,
			   @Nonnull AndroidClassInfo classInfo);
}
