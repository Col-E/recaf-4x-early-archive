package software.coley.recaf;

import atlantafx.base.theme.NordDark;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import software.coley.recaf.services.window.WindowManager;
import software.coley.recaf.ui.RecafTheme;
import software.coley.recaf.ui.menubar.MainMenu;

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
		setUserAgentStylesheet(new RecafTheme().getUserAgentStylesheet());

		// Get components
		MainMenu menu = recaf.get(MainMenu.class);

		// Layout
		BorderPane root = new BorderPane();
		root.setTop(menu);

		// Display
		stage.setMinWidth(250);
		stage.setMinHeight(100);
		stage.setScene(new Scene(root));
		stage.setTitle("Recaf");
		stage.show();

		// Register main window
		WindowManager windowManager = recaf.get(WindowManager.class);
		windowManager.register(stage);
	}
}
