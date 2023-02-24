package software.coley.recaf.ui.docking;


import com.panemu.tiwulfx.control.dock.DetachableTab;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import software.coley.recaf.util.FxThreadUtil;

/**
 * {@link Tab} extension to track additional information required for {@link DockingManager} operations.
 *
 * @author Matt Coley
 */
public class DockingTab extends DetachableTab {
	/**
	 * @param title
	 * 		Initial tab title.
	 * 		Non-observable and effectively the final title text of the tab.
	 * @param content
	 * 		Initial tab content.
	 */
	public DockingTab(String title, Node content) {
		textProperty().setValue(title);
		setContent(content);
	}

	/**
	 * @param title
	 * 		Initial tab title.
	 * @param content
	 * 		Initial tab content.
	 */
	public DockingTab(ObservableValue<String> title, Node content) {
		textProperty().bind(title);
		setContent(content);
	}

	/**
	 * @return Parent docking region that contains the tab.
	 */
	public DockingRegion getRegion() {
		return (DockingRegion) getTabPane();
	}

	/**
	 * Close the current tab.
	 */
	public void close() {
		TabPane tabPane = getTabPane();
		Event.fireEvent(this, new Event(Tab.CLOSED_EVENT));
		if (tabPane != null)
			FxThreadUtil.run(() -> tabPane.getTabs().remove(this));
	}

	/**
	 * Select the current tab.
	 */
	public void select() {
		TabPane parent = getTabPane();
		if (parent != null)
			parent.getSelectionModel().select(this);

		Node content = getContent();
		if (content != null)
			content.requestFocus();
	}
}