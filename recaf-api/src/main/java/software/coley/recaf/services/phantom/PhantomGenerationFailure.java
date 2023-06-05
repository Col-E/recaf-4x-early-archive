package software.coley.recaf.services.phantom;

/**
 * Exception thrown when {@link PhantomGenerator} operations fail.
 *
 * @author Matt Coley
 */
public class PhantomGenerationFailure extends Exception {
	/**
	 * @param cause
	 * 		Root cause of the failure.
	 * @param message
	 * 		Additional detail message.
	 */
	public PhantomGenerationFailure(Throwable cause, String message) {
		super(message, cause);
	}

	/**
	 * @param cause
	 * 		Root cause of the failure.
	 */
	public PhantomGenerationFailure(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * 		Additional detail message.
	 */
	public PhantomGenerationFailure(String message) {
		super(message);
	}
}
