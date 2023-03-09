package software.coley.recaf.ui.pane.editing;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import javafx.scene.control.Label;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.ui.config.ClassEditingConfig;
import software.coley.recaf.ui.pane.editing.jvm.JvmDecompilerPane;

/**
 * Displays {@link JvmClassInfo} in a configurable manner.
 *
 * @author Matt Coley
 */
@Dependent
public class JvmClassPane extends ClassPane {
	private final Instance<JvmDecompilerPane> decompilerProvider;
	private JvmClassEditorType editorType;

	@Inject
	public JvmClassPane(@Nonnull ClassEditingConfig config, @Nonnull Instance<JvmDecompilerPane> decompilerProvider) {
		editorType = config.getDefaultJvmEditor().getValue();
		this.decompilerProvider = decompilerProvider;

		// TODO: Side-panel system
		//  - Optional show title or just icons to maximize space
	}

	/**
	 * @return Current editor display type.
	 */
	@Nonnull
	public JvmClassEditorType getEditorType() {
		return editorType;
	}

	/**
	 * @param editorType
	 * 		New editor display type.
	 */
	public void setEditorType(@Nonnull JvmClassEditorType editorType) {
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
		// We do not need to pass the path along to it here, since the calling context should do that.
		JvmClassEditorType type = getEditorType();
		switch (type) {
			case DECOMPILE -> setDisplay(decompilerProvider.get());
			case HEX -> setDisplay(new Label("TODO: Hex")); // TODO: Implement hex UI component
			default -> throw new IllegalStateException("Unknown editor type: " + type.name());
		}
	}
}
