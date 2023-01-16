package software.coley.recaf.ui;

import atlantafx.base.theme.Theme;

/**
 * AtlantaFX Recaf theme.
 *
 * @author Matt Coley
 */
public class RecafTheme implements Theme {
	@Override
	public String getName() {
		return "recaf";
	}

	@Override
	public String getUserAgentStylesheet() {
		return "/style/recaf.css";
	}

	@Override
	public boolean isDarkMode() {
		return true;
	}
}
