package software.coley.recaf.services.search;

import jakarta.annotation.Nonnull;
import software.coley.recaf.info.FileInfo;
import software.coley.recaf.services.search.result.FileLocation;
import software.coley.recaf.services.search.result.Location;
import software.coley.recaf.services.search.result.Results;

import java.util.function.BiConsumer;

/**
 * Visitor for {@link FileInfo}
 *
 * @author Matt Coley
 */
public interface FileSearchVisitor extends SearchVisitor {
	/**
	 * Visits a generic file.
	 *
	 * @param resultSink
	 * 		Consumer to feed result values into, typically populating a {@link Results} instance.
	 * @param currentLocation
	 * 		Additional information about the current location.
	 * @param fileInfo
	 * 		File to visit.
	 */
	void visit(@Nonnull BiConsumer<Location, Object> resultSink,
			   @Nonnull FileLocation currentLocation,
			   @Nonnull FileInfo fileInfo);
}
