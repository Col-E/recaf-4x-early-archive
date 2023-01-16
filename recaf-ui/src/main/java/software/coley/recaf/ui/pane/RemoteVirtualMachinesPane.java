package software.coley.recaf.ui.pane;

import atlantafx.base.theme.Styles;
import com.sun.tools.attach.VirtualMachineDescriptor;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.carbonicons.CarbonIcons;
import org.slf4j.Logger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.services.attach.AttachManager;
import software.coley.recaf.services.attach.AttachManagerConfig;
import software.coley.recaf.services.attach.PostScanListener;
import software.coley.recaf.ui.control.FontIconView;
import software.coley.recaf.ui.window.RemoteVirtualMachinesWindow;
import software.coley.recaf.util.Lang;
import software.coley.recaf.util.threading.ThreadUtil;

import java.util.Set;

/**
 * Pane for displaying available remote JVMs from {@link AttachManager}.
 *
 * @author Matt Coley
 * @see RemoteVirtualMachinesWindow
 */
@Dependent
public class RemoteVirtualMachinesPane extends BorderPane implements PostScanListener {
	private static final Logger logger = Logging.get(RemoteVirtualMachinesPane.class);
	private final AttachManager attachManager;
	private final AttachManagerConfig attachManagerConfig;

	@Inject
	public RemoteVirtualMachinesPane(AttachManager attachManager, AttachManagerConfig attachManagerConfig) {
		this.attachManager = attachManager;
		this.attachManagerConfig = attachManagerConfig;

		// Register this class as scan listener so we can update the UI live as updates come in.
		attachManager.addPostScanListener(this);

		// Setup UI
		if (attachManager.canAttach())
			initialize();
		else
			initializeWithoutAttach();
	}

	/**
	 * Sets up the UI, and binds passive scanning to only occur while this pane is displayed.
	 */
	private void initialize() {
		// Add listener so that passive scanning in the attach manager only occurs while this pane is visible.
		sceneProperty().addListener((obScene, initialScene, scene) -> {
			scene.windowProperty().addListener((obWindow, initialWindow, window) -> {
				window.showingProperty().addListener((obShowing, oldShowing, showing) -> {
					// When showing run a scan immediately.
					// We are already registered as a scan listener, so we can update the display after it finishes.
					if (showing)
						ThreadUtil.run(attachManager::scan);

					// Bind scanning to only run when the UI is displayed.
					logger.error("Passive JVM scanning: {}", showing ? "ENABLED" : "DISABLED");
					attachManagerConfig.getPassiveScanning().setValue(showing);
				});
			});
		});

		// TODO: Initialize UI components
		//  - Add option to always show JMX info: https://stackoverflow.com/questions/5200269/list-of-running-jvms-on-the-localhost/35096963#35096963
	}

	/**
	 * Place a warning box stating that the feature is not available.
	 */
	private void initializeWithoutAttach() {
		Label graphic = new Label();
		graphic.setGraphic(new FontIconView(CarbonIcons.ERROR, 128, Color.RED));
		graphic.setAlignment(Pos.CENTER);

		Label title = new Label();
		title.getStyleClass().add(Styles.TITLE_1);
		title.textProperty().bind(Lang.getBinding("attach.unsupported"));
		title.setAlignment(Pos.CENTER);

		Label description = new Label();
		description.getStyleClass().add(Styles.TITLE_4);
		description.textProperty().bind(Lang.getBinding("attach.unsupported.detail"));
		description.setAlignment(Pos.CENTER);

		VBox box = new VBox(graphic, title, description);
		box.setMaxHeight(Double.MAX_VALUE);
		box.setMaxWidth(Double.MAX_VALUE);
		box.setMinHeight(250);
		box.setMinWidth(300);
		box.setAlignment(Pos.CENTER);
		box.getStyleClass().add("tooltip");

		// Layout 'box' centered on the pane
		VBox vwrap = new VBox(box);
		vwrap.setAlignment(Pos.CENTER);
		vwrap.setMaxHeight(Double.MAX_VALUE);
		vwrap.setMaxWidth(Double.MAX_VALUE);
		HBox hwrap = new HBox(vwrap);
		hwrap.setAlignment(Pos.CENTER);
		hwrap.setMaxHeight(Double.MAX_VALUE);
		hwrap.setMaxWidth(Double.MAX_VALUE);
		hwrap.setMouseTransparent(true);

		setCenter(hwrap);
	}

	@Override
	public void onScanCompleted(@Nonnull Set<VirtualMachineDescriptor> added,
								@Nonnull Set<VirtualMachineDescriptor> removed) {
		// TODO: Update UI components with new/removed VM entries
	}
}
