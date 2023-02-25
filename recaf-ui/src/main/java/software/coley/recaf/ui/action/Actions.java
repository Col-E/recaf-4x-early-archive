package software.coley.recaf.ui.action;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.kordamp.ikonli.carbonicons.CarbonIcons;
import org.slf4j.Logger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.info.AndroidClassInfo;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.services.cell.IconProviderService;
import software.coley.recaf.services.cell.TextProviderService;
import software.coley.recaf.ui.docking.DockingManager;
import software.coley.recaf.ui.docking.DockingRegion;
import software.coley.recaf.ui.docking.DockingTab;
import software.coley.recaf.ui.navigation.Navigable;
import software.coley.recaf.ui.navigation.NavigationManager;
import software.coley.recaf.ui.pane.editing.AndroidClassPane;
import software.coley.recaf.ui.pane.editing.JvmClassEditorType;
import software.coley.recaf.ui.pane.editing.JvmClassPane;
import software.coley.recaf.ui.path.ClassPathNode;
import software.coley.recaf.ui.path.PathNode;
import software.coley.recaf.ui.path.WorkspacePathNode;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.AndroidClassBundle;
import software.coley.recaf.workspace.model.bundle.Bundle;
import software.coley.recaf.workspace.model.bundle.ClassBundle;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.util.List;
import java.util.function.Supplier;

import static software.coley.recaf.util.Menus.*;

/**
 * Common actions integration.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class Actions {
	private static final Logger logger = Logging.get(Actions.class);
	private final NavigationManager navigationManager;
	private final DockingManager dockingManager;
	private final TextProviderService textService;
	private final IconProviderService iconService;
	private final Instance<JvmClassPane> jvmPaneProvider;
	private final Instance<AndroidClassPane> androidPaneProvider;

	@Inject
	public Actions(@Nonnull NavigationManager navigationManager,
				   @Nonnull DockingManager dockingManager,
				   @Nonnull TextProviderService textService,
				   @Nonnull IconProviderService iconService,
				   @Nonnull Instance<JvmClassPane> jvmPaneProvider,
				   @Nonnull Instance<AndroidClassPane> androidPaneProvider) {
		this.navigationManager = navigationManager;
		this.dockingManager = dockingManager;
		this.textService = textService;
		this.iconService = iconService;
		this.jvmPaneProvider = jvmPaneProvider;
		this.androidPaneProvider = androidPaneProvider;
	}

	/**
	 * Automatically calls the type-specific goto-declaration handling.
	 *
	 * @param path
	 * 		Path containing a class to open.
	 */
	public void gotoDeclaration(@Nonnull ClassPathNode path) {
		Workspace workspace = path.getValueOfType(Workspace.class);
		WorkspaceResource resource = path.getValueOfType(WorkspaceResource.class);
		ClassBundle<?> bundle = path.getValueOfType(ClassBundle.class);
		ClassInfo info = path.getValue();
		if (workspace == null) {
			logger.error("Cannot handle goto-declaration for class '{}', missing workspace in path", info.getName());
			return;
		}
		if (resource == null) {
			logger.error("Cannot handle goto-declaration for class '{}', missing resource in path", info.getName());
			return;
		}
		if (bundle == null) {
			logger.error("Cannot handle goto-declaration for class '{}', missing bundle in path", info.getName());
			return;
		}

		// Handle JVM vs Android
		if (info.isJvmClass()) {
			gotoDeclaration(workspace, resource, (JvmClassBundle) bundle, info.asJvmClass());
		} else if (info.isAndroidClass()) {
			gotoDeclaration(workspace, resource, (AndroidClassBundle) bundle, info.asAndroidClass());
		}
	}

	/**
	 * @param workspace
	 * 		Containing workspace.
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param info
	 * 		Class to go to.
	 */
	public void gotoDeclaration(@Nonnull Workspace workspace,
								@Nonnull WorkspaceResource resource,
								@Nonnull JvmClassBundle bundle,
								@Nonnull JvmClassInfo info) {
		ClassPathNode path = buildPath(workspace, resource, bundle, info);
		getOrCreatePathContent(path, () -> {
			// Create text/graphic for the tab to create.
			String title = textService.getJvmClassInfoTextProvider(workspace, resource, bundle, info).makeText();
			Node graphic = iconService.getJvmClassInfoIconProvider(workspace, resource, bundle, info).makeIcon();
			if (title == null) throw new IllegalStateException("Missing title");
			if (graphic == null) throw new IllegalStateException("Missing graphic");

			// Create content for the tab.
			JvmClassPane content = jvmPaneProvider.get();
			content.onUpdatePath(path);

			// Build the tab.
			DockingTab tab = createTab(dockingManager.getPrimaryRegion(), title, graphic, content);
			ContextMenu menu = new ContextMenu();
			ObservableList<MenuItem> items = menu.getItems();
			Menu mode = menu("menu.mode", CarbonIcons.VIEW);
			mode.getItems().addAll(
					action("menu.mode.class.decompile", CarbonIcons.CODE,
							() -> content.setEditorType(JvmClassEditorType.DECOMPILE)),
					action("menu.mode.file.hex", CarbonIcons.NUMBER_0,
							() -> content.setEditorType(JvmClassEditorType.HEX))
			);
			items.add(mode);
			items.add(action("menu.tab.copypath", CarbonIcons.COPY_LINK, () -> {
				ClipboardContent clipboard = new ClipboardContent();
				clipboard.putString(info.getName());
				Clipboard.getSystemClipboard().setContent(clipboard);
			}));
			items.add(separator());
			addCloseActions(menu, tab);
			tab.setContextMenu(menu);
			return tab;
		});
	}

	/**
	 * @param workspace
	 * 		Containing workspace.
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param info
	 * 		Class to go to.
	 */
	public void gotoDeclaration(@Nonnull Workspace workspace,
								@Nonnull WorkspaceResource resource,
								@Nonnull AndroidClassBundle bundle,
								@Nonnull AndroidClassInfo info) {
		ClassPathNode path = buildPath(workspace, resource, bundle, info);
		getOrCreatePathContent(path, () -> {
			// Create text/graphic for the tab to create.
			String title = textService.getAndroidClassInfoTextProvider(workspace, resource, bundle, info).makeText();
			Node graphic = iconService.getAndroidClassInfoIconProvider(workspace, resource, bundle, info).makeIcon();
			if (title == null) throw new IllegalStateException("Missing title");
			if (graphic == null) throw new IllegalStateException("Missing graphic");

			// Create content for the tab.
			AndroidClassPane content = androidPaneProvider.get();
			content.onUpdatePath(path);

			// Build the tab.
			DockingTab tab = createTab(dockingManager.getPrimaryRegion(), title, graphic, content);
			ContextMenu menu = new ContextMenu();
			ObservableList<MenuItem> items = menu.getItems();
			items.add(action("menu.tab.copypath", () -> {
				ClipboardContent clipboard = new ClipboardContent();
				clipboard.putString(info.getName());
				Clipboard.getSystemClipboard().setContent(clipboard);
			}));
			items.add(separator());
			addCloseActions(menu, tab);
			tab.setContextMenu(menu);
			return tab;
		});
	}

	/**
	 * @param path
	 * 		Path to navigate to.
	 * @param factory
	 * 		Factory to create a tab for displaying content located at the given path,
	 * 		should a tab for the content not already exist.
	 */
	private void getOrCreatePathContent(@Nonnull PathNode<?> path, @Nonnull Supplier<DockingTab> factory) {
		List<Navigable> children = navigationManager.getNavigableChildrenByPath(path);
		if (children.isEmpty()) {
			// Create the tab for the content, then display it.
			DockingTab tab = factory.get();
			tab.select();
		} else {
			// Content by path is already open.
			Navigable navigable = children.get(0);
			selectTab(navigable);
			navigable.requestFocus();
		}
	}

	private void selectTab(Navigable navigable) {
		if (navigable instanceof Node node) {
			while (node != null) {
				// Get the parent of the node, skip the intermediate 'content area' from tab-pane default skin.
				Parent parent = node.getParent();
				if (parent.getStyleClass().contains("tab-content-area"))
					parent = parent.getParent();

				// If the tab content is the node, select it and return.
				if (parent instanceof DockingRegion tabParent)
					for (DockingTab tab : tabParent.getDockTabs())
						if (tab.getContent() == node) {
							tab.select();
							return;
						}

				// Next parent.
				node = parent;
			}
		}
	}

	/**
	 * Shorthand for tab-creation + graphic setting.
	 *
	 * @param region
	 * 		Parent region to spawn in.
	 * @param title
	 * 		Tab title.
	 * @param graphic
	 * 		Tab graphic.
	 * @param content
	 * 		Tab content.
	 *
	 * @return Created tab.
	 */
	private static DockingTab createTab(@Nonnull DockingRegion region,
										@Nonnull String title,
										@Nonnull Node graphic,
										@Nonnull Node content) {
		DockingTab tab = region.createTab(title, content);
		tab.setGraphic(graphic);
		return tab;
	}

	/**
	 * Adds close actions to the given menu.
	 * <ul>
	 *     <li>Close</li>
	 *     <li>Close others</li>
	 *     <li>Close all</li>
	 * </ul>
	 *
	 * @param menu
	 * 		Menu to add to.
	 * @param currentTab
	 * 		Current tab reference.
	 */
	private void addCloseActions(@Nonnull ContextMenu menu, @Nonnull DockingTab currentTab) {
		menu.getItems().addAll(
				action("menu.tab.close", CarbonIcons.CLOSE, currentTab::close),
				action("menu.tab.closeothers", CarbonIcons.CLOSE, () -> {
					for (DockingTab regionTab : currentTab.getRegion().getDockTabs()) {
						if (regionTab != currentTab)
							regionTab.close();
					}
				}),
				action("menu.tab.closeall", CarbonIcons.CLOSE, () -> {
					for (DockingTab regionTab : currentTab.getRegion().getDockTabs())
						regionTab.close();
				})
		);
	}

	/**
	 * @param workspace
	 * 		Containing workspace.
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param info
	 * 		Class item to end path with.
	 *
	 * @return Class path node.
	 */
	@SuppressWarnings("unchecked")
	private static ClassPathNode buildPath(@Nonnull Workspace workspace,
										   @Nonnull WorkspaceResource resource,
										   @Nonnull Bundle<?> bundle,
										   @Nonnull ClassInfo info) {
		return new WorkspacePathNode(workspace)
				.child(resource)
				.child(bundle)
				.child(info.getPackageName())
				.child(info);
	}
}
