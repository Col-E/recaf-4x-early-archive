package software.coley.recaf.config;

import software.coley.observables.AbstractObservable;

/**
 * Basic implementation of {@link ConfigValue}.
 *
 * @param <T>
 * 		Value type.
 *
 * @author Matt Coley
 */
public class BasicConfigValue<T> implements ConfigValue<T> {
	private final String key;
	private final Class<T> type;
	private final AbstractObservable<T> observable;

	/**
	 * @param key
	 * 		Value key.
	 * @param type
	 * 		Value type class.
	 * @param observable
	 * 		Observable of value.
	 */
	public BasicConfigValue(String key, Class<T> type, AbstractObservable<T> observable) {
		this.key = key;
		this.type = type;
		this.observable = observable;
	}

	@Override
	public String getId() {
		return key;
	}

	@Override
	public Class<T> getType() {
		return type;
	}

	@Override
	public AbstractObservable<T> getObservable() {
		return observable;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BasicConfigValue<?> other = (BasicConfigValue<?>) o;

		if (!key.equals(other.key)) return false;
		return type.equals(other.type);
	}

	@Override
	public int hashCode() {
		int result = key.hashCode();
		result = 31 * result + type.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "BasicConfigValue{" +
				"key='" + key + '\'' +
				", type=" + type +
				'}';
	}
}
