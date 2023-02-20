package software.coley.recaf.ui.pane;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

/**
 * Pane displayed when first opening Recaf.
 *
 * @author Matt Coley
 */
@Dependent
public class WelcomePane extends BorderPane {
	@Inject
	public WelcomePane() {
		// TODO: Content
		//   - Recent files
		//   - Drop files here to open them
		//   - Tip of the day sorta thing?
		setCenter(new Label("welcome"));
	}
}
