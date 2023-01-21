package software.coley.recaf.util;

import jakarta.annotation.Nonnull;

import java.util.Collections;
import java.util.List;

/**
 * Various collection utils.
 *
 * @author Matt Coley
 */
public class CollectionUtils {
	/**
	 * @param list
	 * 		List to insert into.
	 * @param item
	 * 		Item to insert.
	 * @param <T>
	 * 		Item type.
	 *
	 * @return Index to insert the item at to ensure sorted order.
	 * Results are only correct if the list itself is already in sorted order.
	 */
	public static <T extends Comparable<T>> int sortedInsertIndex(@Nonnull List<T> list, @Nonnull T item) {
		if (list.isEmpty()) return 0;
		int i = Collections.binarySearch(list, item);
		if (i < 0) i = -i - 1; // When not found, invert to get correct index.
		return i;
	}
}
