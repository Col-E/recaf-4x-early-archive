package software.coley.recaf.ui.pane;

import com.panemu.tiwulfx.control.dock.DetachableTab;
import com.panemu.tiwulfx.control.dock.DetachableTabPane;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import org.kordamp.ikonli.carbonicons.CarbonIcons;
import software.coley.recaf.ui.control.FontIconView;
import software.coley.recaf.ui.dock.RecafDockingPane;
import software.coley.recaf.util.Lang;
import software.coley.recaf.workspace.model.Workspace;

/**
 * Root panel for displaying a {@link Workspace}.
 * <br>
 * Provides:
 * <ul>
 *     <li>Searchable tree layout - {@link WorkspaceExplorerPane}</li>
 * </ul>
 *
 * @author Matt Coley
 */
@Dependent
public class WorkspaceRootPane extends BorderPane {
	@Inject
	public WorkspaceRootPane(@Nonnull WorkspaceExplorerPane explorerPane,
							 @Nonnull WorkspaceInformationPane informationPane) {
		getStyleClass().add("inset");

		// Add workspace explorer tree
		RecafDockingPane dockTree = new RecafDockingPane();
		dockTree.setCloseIfEmpty(false);
		DetachableTab treeTab = new DetachableTab();
		treeTab.setContent(explorerPane);
		treeTab.setGraphic(new FontIconView(CarbonIcons.TREE_VIEW));
		treeTab.textProperty().bind(Lang.getBinding("workspace.title"));
		treeTab.setClosable(false);
		treeTab.setDetachable(false);
		dockTree.getTabs().add(treeTab);

		// Add summary of workspace
		RecafDockingPane dockInfo = new RecafDockingPane();
		dockInfo.setCloseIfEmpty(true);
		DetachableTab infoTab = new DetachableTab();
		infoTab.setContent(informationPane);
		infoTab.setGraphic(new FontIconView(CarbonIcons.INFORMATION));
		infoTab.textProperty().bind(Lang.getBinding("workspace.info"));
		dockInfo.getTabs().add(infoTab);

		// Layout
		SplitPane split = new SplitPane(dockTree, dockInfo);
		SplitPane.setResizableWithParent(dockTree, false);
		split.setDividerPositions(0.333);
		setCenter(split);
	}
}
