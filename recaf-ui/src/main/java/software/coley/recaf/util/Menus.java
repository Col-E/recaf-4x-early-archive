package software.coley.recaf.util;

import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.Ikon;
import software.coley.recaf.ui.control.ActionMenu;
import software.coley.recaf.ui.control.ActionMenuItem;
import software.coley.recaf.ui.control.FontIconView;
import software.coley.recaf.ui.control.IconView;

/**
 * Menu utilities.
 *
 * @author Matt Coley
 */
public class Menus {
	/**
	 * @param name
	 * 		Header text.
	 * @param graphic
	 * 		Header graphic.
	 * @param limit
	 * 		Text length limit.
	 *
	 * @return Header menu item.
	 */
	public static MenuItem createHeader(String name, Node graphic, int limit) {
		MenuItem header = new MenuItem(TextDisplayUtil.shortenEscapeLimit(name, limit));
		header.getStyleClass().add("context-menu-header");
		header.setGraphic(graphic);
		header.setDisable(true);
		return header;
	}

	/**
	 * @param textKey
	 * 		Translation key.
	 *
	 * @return Menu instance.
	 */
	public static Menu menu(String textKey) {
		return menu(textKey, (String) null);
	}

	/**
	 * @param textKey
	 * 		Translation key.
	 * @param imagePath
	 * 		Path to image for menu graphic.
	 *
	 * @return Menu instance, with optional graphic.
	 */
	public static Menu menu(String textKey, String imagePath) {
		return menu(textKey, imagePath, false);
	}

	/**
	 * @param textKey
	 * 		Translation key.
	 * @param imagePath
	 * 		Path to image for menu graphic.
	 * @param antialias
	 * 		Flag to enable anti-aliasing of the image graphic.
	 *
	 * @return Menu instance, with optional graphic.
	 */
	public static Menu menu(String textKey, String imagePath, boolean antialias) {
		Node graphic = imagePath == null ? null :
				antialias ? Icons.getScaledIconView(imagePath) : Icons.getIconView(imagePath);
		Menu menu = new Menu();
		menu.textProperty().bind(Lang.getBinding(textKey));
		menu.setGraphic(graphic);
		return menu;
	}

	/**
	 * @param textKey
	 * 		Translation key.
	 * @param icon
	 * 		Ikonli icon for the menu graphic.
	 *
	 * @return Menu instance, with graphic.
	 */
	public static Menu menu(String textKey, Ikon icon) {
		return menu(textKey, icon, null);
	}

	/**
	 * @param textKey
	 * 		Translation key.
	 * @param icon
	 * 		Ikonli icon for the menu graphic.
	 * @param color
	 * 		Color for icon.
	 *
	 * @return Menu instance, with graphic.
	 */
	public static Menu menu(String textKey, Ikon icon, Color color) {
		FontIconView graphic = color == null ?
				new FontIconView(icon) :
				new FontIconView(icon, IconView.DEFAULT_ICON_SIZE, color);
		return menu(textKey, graphic);
	}

	/**
	 * @param textKey
	 * 		Translation key.
	 * @param graphic
	 * 		Optional menu graphic.
	 *
	 * @return Menu instance, with optional graphic.
	 */
	public static Menu menu(String textKey, Node graphic) {
		Menu menu = new Menu();
		menu.textProperty().bind(Lang.getBinding(textKey));
		menu.setGraphic(graphic);
		return menu;
	}

	/**
	 * @param textKey
	 * 		Translation key.
	 * @param imagePath
	 * 		Path to image for menu graphic.
	 * @param runnable
	 * 		Action to run on click.
	 *
	 * @return Menu instance, with behavior on-click.
	 */
	public static Menu actionMenu(String textKey, String imagePath, Runnable runnable) {
		return actionMenu(textKey, imagePath, runnable, false);
	}

	/**
	 * @param textKey
	 * 		Translation key.
	 * @param imagePath
	 * 		Path to image for menu graphic.
	 * @param runnable
	 * 		Action to run on click.
	 * @param antialias
	 * 		Flag to enable anti-aliasing of the image graphic.
	 *
	 * @return Menu instance, with behavior on-click.
	 */
	public static Menu actionMenu(String textKey, String imagePath, Runnable runnable, boolean antialias) {
		Node graphic = imagePath == null ? null :
				antialias ? Icons.getScaledIconView(imagePath) : Icons.getIconView(imagePath);
		return new ActionMenu(Lang.getBinding(textKey), graphic, runnable);
	}

	/**
	 * @param textKey
	 * 		Translation key.
	 * @param runnable
	 * 		Action to run on click.
	 *
	 * @return Action menu item with behavior on-click.
	 */
	public static ActionMenuItem action(String textKey, Runnable runnable) {
		return action(textKey, (String) null, runnable);
	}

	/**
	 * @param textKey
	 * 		Translation key.
	 * @param imagePath
	 * 		Path to image for menu graphic.
	 * @param runnable
	 * 		Action to run on click.
	 *
	 * @return Action menu item with behavior on-click.
	 */
	public static ActionMenuItem action(String textKey, String imagePath, Runnable runnable) {
		return action(textKey, imagePath, runnable, false);
	}

	/**
	 * @param textKey
	 * 		Translation key.
	 * @param imagePath
	 * 		Path to image for menu graphic.
	 * @param runnable
	 * 		Action to run on click.
	 * @param antialias
	 * 		Flag to enable anti-aliasing of the image graphic.
	 *
	 * @return Action menu item with behavior on-click.
	 */
	public static ActionMenuItem action(String textKey, String imagePath, Runnable runnable, boolean antialias) {
		Node graphic = imagePath == null ? null :
				antialias ? Icons.getScaledIconView(imagePath) : Icons.getIconView(imagePath);
		return action(textKey, graphic, runnable);
	}

	/**
	 * @param textKey
	 * 		Translation key.
	 * @param icon
	 * 		Ikonli icon for the menu graphic.
	 * @param runnable
	 * 		Action to run on click.
	 *
	 * @return Action menu item with behavior on-click.
	 */
	public static ActionMenuItem action(String textKey, Ikon icon, Runnable runnable) {
		return action(textKey, icon, null, runnable);
	}

	/**
	 * @param textKey
	 * 		Translation key.
	 * @param icon
	 * 		Ikonli icon for the menu graphic.
	 * @param runnable
	 * 		Action to run on click.
	 *
	 * @return Action menu item with behavior on-click.
	 */
	public static ActionMenuItem action(String textKey, Ikon icon, Color color, Runnable runnable) {
		FontIconView graphic = color == null ?
				new FontIconView(icon) :
				new FontIconView(icon, IconView.DEFAULT_ICON_SIZE, color);
		return action(textKey, graphic, runnable);
	}

	/**
	 * @param textKey
	 * 		Translation key.
	 * @param graphic
	 * 		Menu graphic.
	 * @param runnable
	 * 		Action to run on click.
	 *
	 * @return Action menu item with behavior on-click.
	 */
	public static ActionMenuItem action(String textKey, Node graphic, Runnable runnable) {
		return new ActionMenuItem(Lang.getBinding(textKey), graphic, runnable);
	}

	/**
	 * @param text
	 * 		Menu item text.
	 * @param imagePath
	 * 		Path to image for menu graphic.
	 * @param runnable
	 * 		Action to run on click.
	 *
	 * @return Action menu item with behavior on-click.
	 */
	public static ActionMenuItem actionLiteral(String text, String imagePath, Runnable runnable) {
		Node graphic = imagePath == null ? null : Icons.getIconView(imagePath);
		return new ActionMenuItem(text, graphic, runnable);
	}

	/**
	 * @return New menu separator.
	 */
	public static SeparatorMenuItem separator() {
		return new SeparatorMenuItem();
	}
}