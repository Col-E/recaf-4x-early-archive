package software.coley.recaf.services.search;

import jakarta.annotation.Nonnull;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.services.search.result.JvmClassLocation;
import software.coley.recaf.services.search.result.Location;
import software.coley.recaf.services.search.result.Results;

import java.util.function.BiConsumer;

/**
 * Visitor for {@link JvmClassInfo}
 *
 * @author Matt Coley
 */
public interface JvmClassSearchVisitor extends SearchVisitor {
	/**
	 * Visits an JVM class.
	 *
	 * @param resultSink
	 * 		Consumer to feed result values into, typically populating a {@link Results} instance.
	 * @param currentLocation
	 * 		Additional information about the current location.
	 * @param classInfo
	 * 		Class to visit.
	 */
	void visit(@Nonnull BiConsumer<Location, Object> resultSink,
			   @Nonnull JvmClassLocation currentLocation,
			   @Nonnull JvmClassInfo classInfo);
}
