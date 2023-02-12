package software.coley.recaf.ui.control.tree;

import jakarta.annotation.Nonnull;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeCell;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.Info;
import software.coley.recaf.services.cell.IconProviderService;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.AndroidClassBundle;
import software.coley.recaf.workspace.model.bundle.Bundle;
import software.coley.recaf.workspace.model.bundle.FileBundle;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

/**
 * Call for rendering {@link WorkspaceTreePath} items.
 *
 * @author Matt Coley
 */
public class WorkspaceTreeCell extends TreeCell<WorkspaceTreePath> {
	private final IconProviderService iconService;

	public WorkspaceTreeCell(@Nonnull IconProviderService iconService) {
		this.iconService = iconService;
	}

	@Override
	protected void updateItem(WorkspaceTreePath item, boolean empty) {
		super.updateItem(item, empty);
		if (empty || item == null) {
			setText(null);
			setGraphic(null);
			setContextMenu(null);
		} else {
			setText(textOf(item));
			setGraphic(graphicOf(item));
			setContextMenu(contextMenuOf(item));
		}
	}

	@SuppressWarnings("all") // for the NPE, doesn't understand 'hasX' contracts.
	public static String textOf(@Nonnull WorkspaceTreePath item) {
		if (item.hasInfo()) {
			String name = item.info().getName();
			return name.substring(name.lastIndexOf('/') + 1);
		} else if (item.hasLocalPath()) {
			String path = item.localPath();
			return path.substring(path.lastIndexOf('/') + 1);
		} else if (item.hasBundle()) {
			return item.bundle().getClass().getName();
		} else {
			return item.resource().getClass().getName();
		}
	}

	/**
	 * @param item
	 * 		Item to create graphic for.
	 *
	 * @return Icon for the item represented by the path.
	 */
	@SuppressWarnings("all")
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
			if (bundle instanceof FileBundle) {
				return iconService.getDirectoryIconProvider(workspace, resource,
						(FileBundle) bundle, item.localPath()).makeIcon();
			} else {
				return iconService.getPackageIconProvider(workspace, resource,
						(Bundle<? extends ClassInfo>) bundle, item.localPath()).makeIcon();
			}
		} else {
			return iconService.getResourceIconProvider(workspace, resource).makeIcon();
		}

		// No icon
		return null;
	}

	public static ContextMenu contextMenuOf(@Nonnull WorkspaceTreePath item) {
		return null;
	}
}
