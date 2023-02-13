package software.coley.recaf.ui.control.tree;

import jakarta.annotation.Nonnull;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeCell;
import javafx.scene.input.MouseButton;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.Info;
import software.coley.recaf.services.cell.ContextMenuProviderService;
import software.coley.recaf.services.cell.IconProviderService;
import software.coley.recaf.services.cell.TextProviderService;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.*;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

/**
 * Cell for rendering {@link WorkspaceTreePath} items.
 *
 * @author Matt Coley
 */
public class WorkspaceTreeCell extends TreeCell<WorkspaceTreePath> {
	private final TextProviderService textService;
	private final IconProviderService iconService;
	private final ContextMenuProviderService contextMenuService;

	/**
	 * @param textService
	 * 		Service to provide text.
	 * @param iconService
	 * 		Service to provide icons.
	 * @param contextMenuService
	 * 		Service to provide context menus.
	 */
	public WorkspaceTreeCell(@Nonnull TextProviderService textService,
							 @Nonnull IconProviderService iconService,
							 @Nonnull ContextMenuProviderService contextMenuService) {
		this.textService = textService;
		this.iconService = iconService;
		this.contextMenuService = contextMenuService;
	}

	@Override
	protected void updateItem(WorkspaceTreePath item, boolean empty) {
		super.updateItem(item, empty);

		// TODO: Abstract away to 'CellConfiguratorService' which has
		//    - 'configure(IndexedCell)'
		//    - 'unconfigure(IndexedCell)'
		//  which is the same as this code. That way when we make list-cell impls we can just plug in the service.
		//  Only problem: Abstract away 'WorkspaceTreePath' to be applicable to 'Info' types and wrapping content.
		if (empty || item == null) {
			setText(null);
			setGraphic(null);
			setContextMenu(null);
			setOnMousePressed(null);
		} else {
			setText(textOf(item));
			setGraphic(graphicOf(item));
			setOnMousePressed(e -> { // Lazily populate context menus
				if (e.getButton() == MouseButton.SECONDARY && getContextMenu() == null) {
					setContextMenu(contextMenuOf(item));
					setOnMousePressed(null);
				}
			});
		}
	}

	/**
	 * @param item
	 * 		Item to create text for.
	 *
	 * @return Text for the item represented by the path.
	 */
	@SuppressWarnings("all") // for the NPE, doesn't understand 'hasX' contracts.
	private String textOf(@Nonnull WorkspaceTreePath item) {
		Workspace workspace = item.workspace();
		WorkspaceResource resource = item.resource();
		Bundle<? extends Info> bundle = item.bundle();
		if (item.hasInfo()) {
			Info info = item.info();
			if (info.isFile()) {
				return textService.getFileInfoTextProvider(workspace, resource,
						(FileBundle) bundle, info.asFile()).makeText();
			} else if (info.isClass()) {
				ClassInfo classInfo = info.asClass();
				if (classInfo.isAndroidClass()) {
					return textService.getAndroidClassInfoTextProvider(workspace, resource,
							(AndroidClassBundle) bundle, classInfo.asAndroidClass()).makeText();
				} else {
					return textService.getJvmClassInfoTextProvider(workspace, resource,
							(JvmClassBundle) bundle, classInfo.asJvmClass()).makeText();
				}
			}
		} else if (item.hasLocalPath()) {
			if (bundle instanceof FileBundle fileBundle) {
				return textService.getDirectoryTextProvider(workspace, resource, fileBundle, item.localPath()).makeText();
			} else if (bundle instanceof ClassBundle<? extends ClassInfo> classBundle) {
				return textService.getPackageTextProvider(workspace, resource, classBundle, item.localPath()).makeText();
			}
		} else if (item.hasBundle()) {
			return textService.getBundleTextProvider(workspace, resource, bundle).makeText();
		} else {
			return textService.getResourceTextProvider(workspace, resource).makeText();
		}

		// No text
		return null;
	}

	/**
	 * @param item
	 * 		Item to create graphic for.
	 *
	 * @return Icon for the item represented by the path.
	 */
	@SuppressWarnings("all") // for the NPE, doesn't understand 'hasX' contracts.
	private Node graphicOf(@Nonnull WorkspaceTreePath item) {
		Workspace workspace = item.workspace();
		WorkspaceResource resource = item.resource();
		Bundle<? extends Info> bundle = item.bundle();
		if (item.hasInfo()) {
			Info info = item.info();
			if (info.isFile()) {
				return iconService.getFileInfoIconProvider(workspace, resource,
						(FileBundle) bundle, info.asFile()).makeIcon();
			} else if (info.isClass()) {
				ClassInfo classInfo = info.asClass();
				if (classInfo.isAndroidClass()) {
					return iconService.getAndroidClassInfoIconProvider(workspace, resource,
							(AndroidClassBundle) bundle, classInfo.asAndroidClass()).makeIcon();
				} else {
					return iconService.getJvmClassInfoIconProvider(workspace, resource,
							(JvmClassBundle) bundle, classInfo.asJvmClass()).makeIcon();
				}
			}
		} else if (item.hasLocalPath()) {
			if (bundle instanceof FileBundle fileBundle) {
				return iconService.getDirectoryIconProvider(workspace, resource, fileBundle, item.localPath()).makeIcon();
			} else if (bundle instanceof ClassBundle<? extends ClassInfo> classBundle) {
				return iconService.getPackageIconProvider(workspace, resource, classBundle, item.localPath()).makeIcon();
			}
		} else if (item.hasBundle()) {
			return iconService.getBundleIconProvider(workspace, resource, bundle).makeIcon();
		} else {
			return iconService.getResourceIconProvider(workspace, resource).makeIcon();
		}

		// No icon
		return null;
	}

	/**
	 * @param item
	 * 		Item to create a context-menu for.
	 *
	 * @return Context-menu for the item represented by the path.
	 */
	@SuppressWarnings("all") // for the NPE, doesn't understand 'hasX' contracts.
	private ContextMenu contextMenuOf(@Nonnull WorkspaceTreePath item) {
		Workspace workspace = item.workspace();
		WorkspaceResource resource = item.resource();
		Bundle<? extends Info> bundle = item.bundle();
		if (item.hasInfo()) {
			Info info = item.info();
			if (info.isFile()) {
				return contextMenuService.getFileInfoContextMenuProvider(workspace, resource,
						(FileBundle) bundle, info.asFile()).makeMenu();
			} else if (info.isClass()) {
				ClassInfo classInfo = info.asClass();
				if (classInfo.isAndroidClass()) {
					return contextMenuService.getAndroidClassInfoContextMenuProvider(workspace, resource,
							(AndroidClassBundle) bundle, classInfo.asAndroidClass()).makeMenu();
				} else {
					return contextMenuService.getJvmClassInfoContextMenuProvider(workspace, resource,
							(JvmClassBundle) bundle, classInfo.asJvmClass()).makeMenu();
				}
			}
		} else if (item.hasLocalPath()) {
			if (bundle instanceof FileBundle fileBundle) {
				return contextMenuService.getDirectoryContextMenuProvider(workspace, resource, fileBundle,
						item.localPath()).makeMenu();
			} else if (bundle instanceof ClassBundle<? extends ClassInfo> classBundle) {
				return contextMenuService.getPackageContextMenuProvider(workspace, resource, classBundle,
						item.localPath()).makeMenu();
			}
		} else if (item.hasBundle()) {
			return contextMenuService.getBundleContextMenuProvider(workspace, resource, bundle).makeMenu();
		} else {
			return contextMenuService.getResourceContextMenuProvider(workspace, resource).makeMenu();
		}

		// No menu
		return null;
	}
}
