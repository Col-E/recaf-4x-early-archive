package software.coley.recaf.ui.pane.editing;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.scene.control.Label;
import software.coley.recaf.info.AndroidClassInfo;
import software.coley.recaf.ui.config.ClassEditingConfig;

/**
 * Displays {@link AndroidClassInfo} in a configurable manner.
 *
 * @author Matt Coley
 */
@Dependent
public class AndroidClassPane extends ClassPane {
	private AndroidClassEditorType editorType;

	@Inject
	public AndroidClassPane(@Nonnull ClassEditingConfig config) {
		editorType = config.getDefaultAndroidEditor().getValue();
	}

	/**
	 * @return Current editor display type.
	 */
	@Nonnull
	public AndroidClassEditorType getEditorType() {
		return editorType;
	}

	/**
	 * @param editorType
	 * 		New editor display type.
	 */
	public void setEditorType(@Nonnull AndroidClassEditorType editorType) {
		if (this.editorType != editorType) {
			this.editorType = editorType;

			// Refresh display
			clearDisplay();
			generateDisplay();
		}
	}

	@Override
	protected void generateDisplay() {
		// If you want to swap out the display, first clear the existing one.
		// Clearing is done automatically when changing the editor type.
		if (getCenter() != null)
			return;

		// Update content in pane.
		AndroidClassEditorType type = getEditorType();
		switch (type) {
			case SMALI:
				// TODO: Create 'Editor' set-up for smali
				Label decompile = new Label("TODO: Smali");
				setCenter(decompile);
				break;
			default:
				throw new IllegalStateException("Unknown editor type: " + type.name());
		}
	}
}
