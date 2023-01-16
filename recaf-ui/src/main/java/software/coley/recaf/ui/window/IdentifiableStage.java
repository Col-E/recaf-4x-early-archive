package software.coley.recaf.ui.window;

import javafx.stage.Stage;

/**
 * Identifiable stage.
 *
 * @author Matt Coley
 */
public interface IdentifiableStage {
	/**
	 * @return Unique stage ID.
	 */
	String getId();

	/**
	 * @return Self as stage.
	 */
	Stage asStage();
}
