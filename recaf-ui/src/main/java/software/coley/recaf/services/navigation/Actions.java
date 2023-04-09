package software.coley.recaf.services.navigation;

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
import javafx.scene.control.Tab;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.kordamp.ikonli.carbonicons.CarbonIcons;
import org.slf4j.Logger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.info.AndroidClassInfo;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.path.*;
import software.coley.recaf.services.Service;
import software.coley.recaf.services.cell.IconProviderService;
import software.coley.recaf.services.cell.TextProviderService;
import software.coley.recaf.ui.docking.DockingManager;
import software.coley.recaf.ui.docking.DockingRegion;
import software.coley.recaf.ui.docking.DockingTab;
import software.coley.recaf.ui.pane.editing.android.AndroidClassPane;
import software.coley.recaf.ui.pane.editing.jvm.JvmClassEditorType;
import software.coley.recaf.ui.pane.editing.jvm.JvmClassPane;
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
public class Actions implements Service {
	public static final String ID = "actions";
	private static final Logger logger = Logging.get(Actions.class);
	private final NavigationManager navigationManager;
	private final DockingManager dockingManager;
	private final TextProviderService textService;
	private final IconProviderService iconService;
	private final Instance<JvmClassPane> jvmPaneProvider;
	private final Instance<AndroidClassPane> androidPaneProvider;
	private final ActionsConfig config;

	@Inject
	public Actions(@Nonnull ActionsConfig config,
				   @Nonnull NavigationManager navigationManager,
				   @Nonnull DockingManager dockingManager,
				   @Nonnull TextProviderService textService,
				   @Nonnull IconProviderService iconService,
				   @Nonnull Instance<JvmClassPane> jvmPaneProvider,
				   @Nonnull Instance<AndroidClassPane> androidPaneProvider) {
		this.config = config;
		this.navigationManager = navigationManager;
		this.dockingManager = dockingManager;
		this.textService = textService;
		this.iconService = iconService;
		this.jvmPaneProvider = jvmPaneProvider;
		this.androidPaneProvider = androidPaneProvider;
	}

	/**
	 * Brings a {@link ClassNavigable} component representing the given class into focus.
	 * If no such component exists, one is created.
	 * <br>
	 * Automatically calls the type-specific goto-declaration handling.
	 *
	 * @param path
	 * 		Path containing a class to open.
	 *
	 * @return Navigable content representing class content of the path.
	 *
	 * @throws IncompletePathException
	 * 		When the path is missing parent elements.
	 */
	@Nonnull
	public ClassNavigable gotoDeclaration(@Nonnull ClassPathNode path) throws IncompletePathException {
		Workspace workspace = path.getValueOfType(Workspace.class);
		WorkspaceResource resource = path.getValueOfType(WorkspaceResource.class);
		ClassBundle<?> bundle = path.getValueOfType(ClassBundle.class);
		ClassInfo info = path.getValue();
		if (workspace == null) {
			logger.error("Cannot handle goto-declaration for class '{}', missing workspace in path", info.getName());
			throw new IncompletePathException(Workspace.class);
		}
		if (resource == null) {
			logger.error("Cannot handle goto-declaration for class '{}', missing resource in path", info.getName());
			throw new IncompletePathException(WorkspaceResource.class);
		}
		if (bundle == null) {
			logger.error("Cannot handle goto-declaration for class '{}', missing bundle in path", info.getName());
			throw new IncompletePathException(ClassBundle.class);
		}

		// Handle JVM vs Android
		if (info.isJvmClass()) {
			return gotoDeclaration(workspace, resource, (JvmClassBundle) bundle, info.asJvmClass());
		} else if (info.isAndroidClass()) {
			return gotoDeclaration(workspace, resource, (AndroidClassBundle) bundle, info.asAndroidClass());
		}
		throw new IllegalStateException("Unsupported class type: " + info.getClass().getName());
	}

	/**
	 * Brings a {@link ClassNavigable} component representing the given class into focus.
	 * If no such component exists, one is created.
	 *
	 * @param workspace
	 * 		Containing workspace.
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param info
	 * 		Class to go to.
	 *
	 * @return Navigable content representing class content of the path.
	 */
	@Nonnull
	public ClassNavigable gotoDeclaration(@Nonnull Workspace workspace,
										  @Nonnull WorkspaceResource resource,
										  @Nonnull JvmClassBundle bundle,
										  @Nonnull JvmClassInfo info) {
		ClassPathNode path = buildPath(workspace, resource, bundle, info);
		return (ClassNavigable) getOrCreatePathContent(path, () -> {
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
			content.addPathUpdateListener(updatedPath -> {
				// Update tab graphic in case backing class details change.
				JvmClassInfo updatedInfo = updatedPath.getValue().asJvmClass();
				String updatedTitle = textService.getJvmClassInfoTextProvider(workspace, resource, bundle, updatedInfo).makeText();
				Node updatedGraphic = iconService.getJvmClassInfoIconProvider(workspace, resource, bundle, updatedInfo).makeIcon();
				tab.setText(updatedTitle);
				tab.setGraphic(updatedGraphic);
			});
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
	 * Brings a {@link ClassNavigable} component representing the given class into focus.
	 * If no such component exists, one is created.
	 *
	 * @param workspace
	 * 		Containing workspace.
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param info
	 * 		Class to go to.
	 *
	 * @return Navigable content representing class content of the path.
	 */
	@Nonnull
	public ClassNavigable gotoDeclaration(@Nonnull Workspace workspace,
										  @Nonnull WorkspaceResource resource,
										  @Nonnull AndroidClassBundle bundle,
										  @Nonnull AndroidClassInfo info) {
		ClassPathNode path = buildPath(workspace, resource, bundle, info);
		return (ClassNavigable) getOrCreatePathContent(path, () -> {
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
			content.addPathUpdateListener(updatedPath -> {
				// Update tab graphic in case backing class details change.
				AndroidClassInfo updatedInfo = updatedPath.getValue().asAndroidClass();
				String updatedTitle = textService.getAndroidClassInfoTextProvider(workspace, resource, bundle, updatedInfo).makeText();
				Node updatedGraphic = iconService.getAndroidClassInfoIconProvider(workspace, resource, bundle, updatedInfo).makeIcon();
				tab.setGraphic(updatedGraphic);
				tab.setText(updatedTitle);
			});
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
	 * Looks for the {@link Navigable} component representing the path and returns it if found.
	 * If no such component exists, it should be generated by the passed supplier, which then gets returned.
	 * <br>
	 * The tab containing the {@link Navigable} component is selected when returned.
	 *
	 * @param path
	 * 		Path to navigate to.
	 * @param factory
	 * 		Factory to create a tab for displaying content located at the given path,
	 * 		should a tab for the content not already exist.
	 * 		<br>
	 * 		<b>NOTE:</b> It is required/assumed that the {@link Tab#getContent()} is a
	 * 		component implementing {@link Navigable}.
	 *
	 * @return Navigable content representing content of the path.
	 */
	@Nonnull
	public Navigable getOrCreatePathContent(@Nonnull PathNode<?> path, @Nonnull Supplier<DockingTab> factory) {
		List<Navigable> children = navigationManager.getNavigableChildrenByPath(path);
		if (children.isEmpty()) {
			// Create the tab for the content, then display it.
			DockingTab tab = factory.get();
			tab.select();
			return (Navigable) tab.getContent();
		} else {
			// Content by path is already open.
			Navigable navigable = children.get(0);
			selectTab(navigable);
			navigable.requestFocus();
			return navigable;
		}
	}

	/**
	 * Selects the containing {@link DockingTab} that contains the content.
	 *
	 * @param navigable
	 * 		Navigable content to select in its containing {@link DockingRegion}.
	 */
	private static void selectTab(Navigable navigable) {
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
	private static void addCloseActions(@Nonnull ContextMenu menu, @Nonnull DockingTab currentTab) {
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
	private static ClassPathNode buildPath(@Nonnull Workspace workspace,
										   @Nonnull WorkspaceResource resource,
										   @Nonnull Bundle<?> bundle,
										   @Nonnull ClassInfo info) {
		return PathNodes.classPath(workspace, resource, bundle, info);
	}

	@Nonnull
	@Override
	public String getServiceId() {
		return ID;
	}

	@Nonnull
	@Override
	public ActionsConfig getServiceConfig() {
		return config;
	}
}
