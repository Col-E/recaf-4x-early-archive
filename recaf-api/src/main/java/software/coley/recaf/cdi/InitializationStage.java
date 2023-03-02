package software.coley.recaf.cdi;

/**
 * Initialization stage for {@link EagerInitialization#value()}.
 *
 * @author Matt Coley
 * @see EagerInitialization
 */
public enum InitializationStage {
	/**
	 * Occurs after the CDI container is created.
	 */
	CONTAINER_DEPLOY,
	/**
	 * Occurs after the UI is populated.
	 */
	UI_INITIALIZE
}
