package software.coley.recaf.util;

import jakarta.annotation.Nonnull;

import java.util.Collections;
import java.util.List;

/**
 * Various collection utils.
 *
 * @author Matt Coley
 * @author <a href="https://stackoverflow.com/a/29356678/8071915">Paul Boddington</a> - Binary search.
 */
public class CollectionUtil {
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

	public static <T extends Comparable<T>> int binarySearch(T[] items, T target, int first, int last) {
		if (first > last)
			// Typically yield '-1' but with this, we will have it such that if 'target' is not in the list
			// then the return value will be the negative value of the index where it would be inserted into
			// while maintaining sorted order.
			return (first == 0 && last == -1) ? 0 : -last;
		else {
			int middle = (first + last) / 2;
			int compResult = target.compareTo(items[middle]);
			if (compResult == 0)
				return middle;
			else if (compResult < 0)
				return binarySearch(items, target, first, middle - 1);
			else
				return binarySearch(items, target, middle + 1, last);
		}
	}

	public static <T extends Comparable<T>> int binarySearch(List<T> items, T target, int first, int last) {
		if (first > last)
			// Typically yield '-1' but with this, we will have it such that if 'target' is not in the list
			// then the return value will be the negative value of the index where it would be inserted into
			// while maintaining sorted order.
			return (first == 0 && last == -1) ? 0 : -last;
		else {
			int middle = (first + last) / 2;
			int compResult = target.compareTo(items.get(middle));
			if (compResult == 0)
				return middle;
			else if (compResult < 0)
				return binarySearch(items, target, first, middle - 1);
			else
				return binarySearch(items, target, middle + 1, last);
		}
	}

	public static <T extends Comparable<T>> int binarySearch(T[] items, T target) {
		return binarySearch(items, target, 0, items.length - 1);
	}

	public static <T extends Comparable<T>> int binarySearch(List<T> items, T target) {
		return binarySearch(items, target, 0, items.size() - 1);
	}
}
