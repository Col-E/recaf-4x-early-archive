package software.coley.recaf.workspace.model.bundle;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.recaf.info.Info;

import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

/**
 * Base bundle type.
 *
 * @param <I>
 * 		Bundle value type.
 *
 * @author Matt Coley
 */
public interface Bundle<I extends Info> extends Map<String, I>, Iterable<I> {
	/**
	 * History contains a stack of prior states of items.
	 * If an item has not been modified there is no entry in this map.
	 *
	 * @return Map of historical states of items within this bundle.
	 */
	@Nonnull
	Map<String, Stack<I>> getHistory();

	/**
	 * History stack for the given item key.
	 *
	 * @param key
	 * 		Item key.
	 *
	 * @return History of item.
	 */
	@Nullable
	default Stack<I> getHistoryStack(String key) {
		return getHistory().get(key);
	}

	/**
	 * @return Keys of items that have been modified <i>(Containing any history values)</i>.
	 */
	@Nonnull
	default Set<String> getDirtyItems() {
		Set<String> dirty = new TreeSet<>();
		getHistory().forEach((key, itemHistory) -> {
			if (itemHistory.size() > 1) {
				dirty.add(key);
			}
		});
		return dirty;
	}

	// TODO: Copy from 'ResourceItemMap'
	//  - Use custom listeners here
	//  - Resource wraps forwards events in here, via its own listeners
}
