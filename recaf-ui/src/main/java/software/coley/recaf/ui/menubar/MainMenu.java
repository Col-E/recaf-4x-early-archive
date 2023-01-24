package software.coley.recaf.ui.menubar;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.scene.control.MenuBar;

/**
 * Main menu component, bundling sub-menu components.
 *
 * @author Matt Coley
 * @see FileMenu
 */
@Dependent
public class MainMenu extends MenuBar {
	@Inject
	public MainMenu(FileMenu fileMenu, ConfigMenu configMenu, HelpMenu helpMenu) {
		getMenus().addAll(fileMenu, configMenu, helpMenu);
	}
}
