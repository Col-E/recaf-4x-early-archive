package software.coley.recaf.workspace.model.bundle;

import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.info.Info;

import java.util.*;
import java.util.stream.Stream;

/**
 * Basic bundle implementation.
 *
 * @param <I>
 * 		Item type.
 *
 * @author Matt Coley
 */
public class BasicBundle<I extends Info> implements Bundle<I> {
	private static final Logger logger = Logging.get(BasicBundle.class);
	private final Map<String, Stack<I>> history = new HashMap<>();
	private final List<BundleListener<I>> listeners = new ArrayList<>();
	private final Map<String, I> backing = new HashMap<>();

	/**
	 * Create initial history item.
	 *
	 * @param info
	 * 		Origin item.
	 */
	private void initHistory(I info) {
		Stack<I> itemHistory = new Stack<>();
		itemHistory.push(info);
		history.put(info.getName(), itemHistory);
	}

	/**
	 * Utility call for {@link #put(String, Info)}, without invoking the listener.
	 *
	 * @param info
	 * 		Item to put.
	 */
	public void initialPut(I info) {
		backing.put(info.getName(), info);
		initHistory(info);
	}

	/**
	 * Utility call for {@link #put(String, Info)}
	 *
	 * @param info
	 * 		Item to put.
	 *
	 * @return Prior associated value, if any.
	 */
	public I put(I info) {
		return put(info.getName(), info);
	}

	/**
	 * @return Stream of items.
	 */
	public Stream<I> stream() {
		return values().stream();
	}

	/**
	 * History contains a stack of prior states of items.
	 * If an item has not been modified there is no entry in this map.
	 *
	 * @return Map of historical states of items within this bundle.
	 */
	@Nonnull
	protected Map<String, Stack<I>> getHistory() {
		return history;
	}

	@Override
	public Stack<I> getHistory(String key) {
		return history.get(key);
	}

	@Override
	public Set<String> getDirtyKeys() {
		Set<String> dirty = new TreeSet<>();
		history.forEach((key, itemHistory) -> {
			if (itemHistory.size() > 1) {
				dirty.add(key);
			}
		});
		return dirty;
	}

	@Override
	public boolean hasHistory(String key) {
		return history.get(key) != null;
	}

	@Override
	public void incrementHistory(I info) {
		String key = info.getName();
		Stack<I> itemHistory = getHistory(key);
		if (itemHistory == null) {
			throw new IllegalStateException("Failed history increment, no prior history to build on for: " + key);
		}
		// logger.debug("Increment history: {} - {} states", EscapeUtil.escapeCommon(key), itemHistory.size());
		itemHistory.push(info);
	}

	@Override
	public void decrementHistory(String key) {
		Stack<I> itemHistory = getHistory(key);
		if (itemHistory == null) {
			throw new IllegalStateException("Failed history decrement, no prior history to read from for: " + key);
		}
		int size = itemHistory.size();
		// Update map with prior entry
		I currentItem = get(key);
		I priorItem;
		if (size > 1) {
			priorItem = itemHistory.pop();
		} else {
			priorItem = itemHistory.peek();
		}
		backing.put(key, priorItem);
		// Notify listener
		for (BundleListener<I> listener : listeners) {
			try {
				listener.onUpdateItem(key, currentItem, priorItem);
			} catch (Throwable t) {
				logger.error("Uncaught error in bundle listener (revert)", t);
			}
		}
	}

	@Override
	public void addBundleListener(BundleListener<I> listener) {
		listeners.add(listener);
	}

	@Override
	public void removeBundleListener(BundleListener<I> listener) {
		listeners.remove(listener);
	}

	@Override
	public Iterator<I> iterator() {
		return backing.values().iterator();
	}

	@Override
	public int size() {
		return backing.size();
	}

	@Override
	public boolean isEmpty() {
		return backing.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return backing.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return backing.containsValue(value);
	}

	@Override
	public I get(Object key) {
		return backing.get(key);
	}

	@Override
	public I put(String key, I newValue) {
		I oldValue = backing.put(key, newValue);
		// Notify listener
		for (BundleListener<I> listener : listeners) {
			try {
				if (oldValue == null) {
					listener.onNewItem(key, newValue);
				} else {
					listener.onUpdateItem(key, oldValue, newValue);
				}
			} catch (Throwable t) {
				logger.error("Uncaught error in resource listener (put)", t);
			}
		}
		// Update history
		if (oldValue == null) {
			initHistory(newValue);
		} else {
			incrementHistory(newValue);
		}
		return oldValue;
	}

	@Override
	public I remove(Object key) {
		I info = backing.remove(key);
		if (info != null) {
			// Notify listener
			for (BundleListener<I> listener : listeners) {
				try {
					listener.onRemoveItem((String) key, info);
				} catch (Throwable t) {
					logger.error("Uncaught error in resource listener (remove)", t);
				}
			}
			// Update history
			history.remove(key);
		}
		return info;
	}

	@Override
	public void putAll(Map<? extends String, ? extends I> map) {
		throw new UnsupportedOperationException("Bundles cannot use 'putAll'");
	}

	@Override
	public void clear() {
		backing.clear();
		history.clear();
	}

	@Override
	public Set<String> keySet() {
		return backing.keySet();
	}

	@Override
	public Collection<I> values() {
		return backing.values();
	}

	@Override
	public Set<Entry<String, I>> entrySet() {
		return backing.entrySet();
	}
}
