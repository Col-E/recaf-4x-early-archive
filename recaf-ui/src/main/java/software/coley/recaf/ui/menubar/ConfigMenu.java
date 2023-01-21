package software.coley.recaf.ui.menubar;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.scene.control.Menu;
import javafx.stage.Stage;
import org.kordamp.ikonli.carbonicons.CarbonIcons;
import org.kordamp.ikonli.javafx.FontIcon;
import software.coley.recaf.services.config.ConfigManager;
import software.coley.recaf.services.window.WindowManager;
import software.coley.recaf.ui.control.ActionMenuItem;
import software.coley.recaf.ui.control.IconView;

import static software.coley.recaf.util.Lang.getBinding;

/**
 * Config menu component for {@link MainMenu}.
 *
 * @author Matt Coley
 */
@Dependent
public class ConfigMenu extends Menu {
	private final WindowManager windowManager;
	private final ConfigManager configManager;

	@Inject
	public ConfigMenu(WindowManager windowManager,
					  ConfigManager configManager) {
		this.windowManager = windowManager;
		this.configManager = configManager;

		FontIcon graphic = new FontIcon(CarbonIcons.SETTINGS);
		graphic.setIconSize(IconView.DEFAULT_ICON_SIZE);
		textProperty().bind(getBinding("menu.config"));
		setGraphic(graphic);

		getItems().add(new ActionMenuItem(getBinding("menu.config.edit"), new FontIcon(CarbonIcons.EDIT), this::openEditor));
		getItems().add(new ActionMenuItem(getBinding("menu.config.export"), new FontIcon(CarbonIcons.DOCUMENT_EXPORT), this::exportProfile));
		getItems().add(new ActionMenuItem(getBinding("menu.config.import"), new FontIcon(CarbonIcons.DOCUMENT_IMPORT), this::importProfile));
	}

	/**
	 * Display the config window.
	 */
	private void openEditor() {
		Stage configWindow = windowManager.getConfigWindow();
		configWindow.show();
		configWindow.requestFocus();
	}

	/**
	 * Exports the current config to a file.
	 */
	private void exportProfile() {
		// TODO: implement
	}

	/**
	 * Applies values in the profile file provided by the user to the current config.
	 */
	private void importProfile() {
		// TODO: implement
	}
}
