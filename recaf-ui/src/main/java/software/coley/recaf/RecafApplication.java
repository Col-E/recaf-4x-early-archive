package software.coley.recaf;

import atlantafx.base.theme.NordDark;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * JavaFX application entry point.
 *
 * @author Matt Coley
 */
public class RecafApplication extends Application {
	private final Recaf recaf = Bootstrap.get();

	@Override
	public void start(Stage stage) {
		// Setup global style
		setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());

		// Get components
		MenuBar menu = new MenuBar(); // TODO: Main menu

		// Layout
		BorderPane root = new BorderPane();
		root.setTop(menu);

		// Display
		stage.setScene(new Scene(root));
		stage.setTitle("Recaf");
		stage.show();
	}
}
