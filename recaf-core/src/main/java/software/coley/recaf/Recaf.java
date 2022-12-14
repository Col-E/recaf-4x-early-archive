package software.coley.recaf;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanContainer;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;

/**
 * Recaf application instance.
 *
 * @author Matt Coley
 */
public class Recaf {
	private static final Annotation[] NO_QUALIFIERS = new Annotation[0];
	private final SeContainer container;

	/**
	 * @param container
	 * 		Container instance for bean management.
	 */
	public Recaf(@Nonnull SeContainer container) {
		this.container = container;
	}

	/**
	 * @param type
	 * 		Type to get an {@link Instance} of.
	 * @param <T>
	 * 		Instance type.
	 *
	 * @return Instance accessor for bean of the given type.
	 */
	public <T> Instance<T> instance(Class<T> type) {
		return instance(type, NO_QUALIFIERS);
	}

	/**
	 * @param type
	 * 		Type to get an {@link Instance} of.
	 * @param qualifiers
	 * 		Qualifiers to narrow down an option if multiple candidate instances exist for the type.
	 * @param <T>
	 * 		Instance type.
	 *
	 * @return Instance accessor for bean of the given type.
	 */
	public <T> Instance<T> instance(Class<T> type, Annotation... qualifiers) {
		return container.select(type, qualifiers);
	}

	/**
	 * @param type
	 * 		Type to get instance of.
	 * @param <T>
	 * 		Instance type.
	 *
	 * @return Instance of type <i>(Wrapped in a proxy)</i>.
	 */
	@Nonnull
	public <T> T proxy(Class<T> type) {
		return proxy(type, NO_QUALIFIERS);
	}

	/**
	 * @param type
	 * 		Type to get instance of.
	 * @param qualifiers
	 * 		Qualifiers to narrow down an option if multiple candidate instances exist for the type.
	 * @param <T>
	 * 		Instance type.
	 *
	 * @return Instance of type <i>(Wrapped in a proxy)</i>.
	 */
	@Nonnull
	public <T> T proxy(Class<T> type, Annotation... qualifiers) {
		return instance(type, qualifiers).get();
	}

	/**
	 * @param type
	 * 		Type to get instance of.
	 * @param <T>
	 * 		Instance type.
	 *
	 * @return Instance of type.
	 */
	@Nonnull
	public <T> T literal(Class<T> type) {
		return literal(type, NO_QUALIFIERS);
	}

	/**
	 * @param type
	 * 		Type to get instance of.
	 * @param qualifiers
	 * 		Qualifiers to narrow down an option if multiple candidate instances exist for the type.
	 * @param <T>
	 * 		Instance type.
	 *
	 * @return Instance of type.
	 */
	@Nonnull
	@SuppressWarnings("unchecked")
	public <T> T literal(Class<T> type, Annotation... qualifiers) {
		// Get candidate beans of type given the qualifiers
		BeanContainer beanContainer = container.getBeanContainer();
		Set<Bean<?>> beans = (qualifiers == null || qualifiers.length == 0) ?
				beanContainer.getBeans(type, NO_QUALIFIERS) :
				beanContainer.getBeans(type, qualifiers);

		// Only allow there to be one candidate
		if (beans.isEmpty()) throw new IllegalArgumentException("No beans of type: " + type +
				" for qualifiers: " + Arrays.toString(qualifiers));
		if (beans.size() > 1) throw new IllegalArgumentException("Multiple candidates for type: " + type +
				" for qualifiers: " + Arrays.toString(qualifiers));

		// Get instance from scope/context
		Bean<T> bean = (Bean<T>) beans.iterator().next();
		Class<? extends Annotation> scope = bean.getScope();
		Context context = beanContainer.getContext(scope);
		T value = context.get(bean);

		// Create it if not already initialized
		if (value == null) {
			CreationalContext<T> creationalContext = beanContainer.createCreationalContext(bean);
			value = context.get(bean, creationalContext);
		}

		return value;
	}
}
