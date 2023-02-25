package software.coley.recaf.ui.pane.editing;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.scene.control.Label;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.ui.config.ClassEditingConfig;

/**
 * Displays {@link JvmClassInfo} in a configurable manner.
 *
 * @author Matt Coley
 */
@Dependent
public class JvmClassPane extends ClassPane {
	private JvmClassEditorType editorType;

	@Inject
	public JvmClassPane(@Nonnull ClassEditingConfig config) {
		editorType = config.getDefaultJvmEditor().getValue();

		// TODO: Side-panel system
		//  - Optional show title or just icons to maximize space

		// TODO: When the path updates, update the containing tab graphic if necessary
		//  - We can add a listener type to UpdatableNavigable for this (along to support file pane as well)
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
		JvmClassEditorType type = getEditorType();
		switch (type) {
			case DECOMPILE -> {
				// TODO: Create 'Editor' set-up for class content
				//  - Decompile the class from the path
				//  - Having to pass 'DecompileManager' into JvmClassPane constructor would suck
				//    can we make use of Instance<T> somehow to get injection support?
				//      - If we exclude 'ClassPathNode' from the constructor, we can make our own ClassPane @Inject-able
				Label decompile = new Label("TODO: Decompile");
				setCenter(decompile);
			}
			case HEX -> {
				// TODO: Hex UI
				Label hex = new Label("TODO: Hex");
				setCenter(hex);
			}
			default -> throw new IllegalStateException("Unknown editor type: " + type.name());
		}
	}
}
