package software.coley.recaf;

import jakarta.enterprise.inject.se.SeContainer;
import org.jboss.weld.environment.se.Weld;

import java.util.function.Consumer;

/**
 * Handles creation of Recaf instance.
 *
 * @author Matt Coley
 */
public class Bootstrap {
	private static Recaf instance;
	private static Consumer<Weld> weldConsumer;

	/**
	 * @return Recaf instance.
	 */
	public static Recaf get() {
		if (instance == null) {
			SeContainer container = createContainer();
			instance = new Recaf(container);
		}
		return instance;
	}

	/**
	 * Must be called before invoking {@link #get()}.
	 *
	 * @param consumer Consumer to operate on the CDI container producing {@link Weld} instance.
	 */
	public static void setWeldConsumer(Consumer<Weld> consumer) {
		weldConsumer = consumer;
	}

	private static SeContainer createContainer() {
		Weld weld = new Weld("recaf");

		// Setup bean discovery
		//  - one instance for base package in API
		//  - one instance for base package in Core
		weld.addPackage(true, RecafConstants.class);
		weld.addPackage(true, Recaf.class);

		// Handle user-defined action
		if (weldConsumer != null) {
			weldConsumer.accept(weld);
			weldConsumer = null;
		}

		return weld.initialize();
	}
}
