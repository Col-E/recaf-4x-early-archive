package software.coley.recaf.ui.window;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.scene.Scene;
import software.coley.recaf.services.window.WindowManager;
import software.coley.recaf.ui.pane.RemoteVirtualMachinesPane;
import software.coley.recaf.util.Lang;

/**
 * Window wrapper for {@link RemoteVirtualMachinesPane}.
 *
 * @author Matt Coley
 * @see RemoteVirtualMachinesPane
 */
@Dependent
public class RemoteVirtualMachinesWindow extends AbstractIdentifiableStage {
	@Inject
	public RemoteVirtualMachinesWindow(RemoteVirtualMachinesPane remoteVirtualMachinesPane) {
		super(WindowManager.WIN_REMOTE_VMS);

		// Layout
		titleProperty().bind(Lang.getBinding("menu.file.attach"));
		setWidth(750);
		setHeight(450);
		setMinWidth(750);
		setMinHeight(450);
		setScene(new Scene(remoteVirtualMachinesPane));
	}
}
