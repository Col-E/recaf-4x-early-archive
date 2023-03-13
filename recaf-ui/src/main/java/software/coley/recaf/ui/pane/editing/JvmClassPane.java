package software.coley.recaf.ui.pane.editing;

import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.kordamp.ikonli.carbonicons.CarbonIcons;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.ui.config.ClassEditingConfig;
import software.coley.recaf.ui.control.BoundTab;
import software.coley.recaf.ui.control.IconView;
import software.coley.recaf.ui.pane.editing.jvm.JvmDecompilerPane;
import software.coley.recaf.ui.pane.editing.tabs.FieldsAndMethodsPane;
import software.coley.recaf.ui.pane.editing.tabs.InheritancePane;
import software.coley.recaf.util.Icons;
import software.coley.recaf.util.Lang;

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
	public JvmClassPane(@Nonnull ClassEditingConfig config,
						@Nonnull FieldsAndMethodsPane fieldsAndMethodsPane,
						@Nonnull InheritancePane inheritancePane,
						@Nonnull Instance<JvmDecompilerPane> decompilerProvider) {
		editorType = config.getDefaultJvmEditor().getValue();
		this.decompilerProvider = decompilerProvider;

		// Setup side-tabs
		addSideTab(new BoundTab(Lang.getBinding("fieldsandmethods.title"),
				new IconView(Icons.getImage(Icons.FIELD_N_METHOD)),
				fieldsAndMethodsPane
		));
		addSideTab(new BoundTab(Lang.getBinding("hierarchy.title"),
				CarbonIcons.FLOW,
				inheritancePane
		));
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
