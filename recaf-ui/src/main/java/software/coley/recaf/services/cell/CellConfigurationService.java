package software.coley.recaf.services.cell;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseButton;
import org.kordamp.ikonli.carbonicons.CarbonIcons;
import org.slf4j.Logger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.FileInfo;
import software.coley.recaf.info.member.ClassMember;
import software.coley.recaf.info.member.FieldMember;
import software.coley.recaf.info.member.MethodMember;
import software.coley.recaf.ui.action.Actions;
import software.coley.recaf.ui.control.FontIconView;
import software.coley.recaf.ui.control.tree.TreeItems;
import software.coley.recaf.ui.control.tree.WorkspaceTreeCell;
import software.coley.recaf.ui.path.*;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.*;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

/**
 * Common configuration handling for {@link Cell} types.
 *
 * @author Matt Coley
 * @see WorkspaceTreeCell Tree cell handling.
 */
@ApplicationScoped
public class CellConfigurationService {
	private static final String UNKNOWN_TEXT = "[ERROR]";
	private static final Node UNKNOWN_GRAPHIC = new FontIconView(CarbonIcons.MISUSE_ALT);
	private static final Logger logger = Logging.get(WorkspaceTreeCell.class);
	private final TextProviderService textService;
	private final IconProviderService iconService;
	private final ContextMenuProviderService contextMenuService;
	private final Actions actions;

	/**
	 * @param textService
	 * 		Service to provide text.
	 * @param iconService
	 * 		Service to provide icons.
	 * @param contextMenuService
	 * 		Service to provide context menus.
	 * @param actions
	 * 		Action handling.
	 */
	@Inject
	public CellConfigurationService(@Nonnull TextProviderService textService,
									@Nonnull IconProviderService iconService,
									@Nonnull ContextMenuProviderService contextMenuService,
									@Nonnull Actions actions) {
		this.textService = textService;
		this.iconService = iconService;
		this.contextMenuService = contextMenuService;
		this.actions = actions;
	}

	/**
	 * @param cell
	 * 		Cell to reset.
	 */
	public void reset(@Nonnull Cell<?> cell) {
		cell.setText(null);
		cell.setGraphic(null);
		cell.setContextMenu(null);
		cell.setOnMousePressed(null);
	}

	/**
	 * @param cell
	 * 		Cell to configure.
	 * @param item
	 * 		Content within the cell.
	 * @param source
	 * 		Origin source of the cell, for context menu specialization.
	 */
	public void configure(@Nonnull Cell<?> cell, @Nonnull PathNode<?> item, @Nonnull ContextSource source) {
		cell.setText(textOf(item));
		cell.setGraphic(graphicOf(item));
		cell.setOnMousePressed(e -> {
			if (e.getButton() == MouseButton.SECONDARY) {
				// Lazily populate context menus when secondary click is prompted.
				if (cell.getContextMenu() == null) cell.setContextMenu(contextMenuOf(source, item));
			} else {
				// Handle primary mouse actions.
				if (cell instanceof TreeCell<?> treeCell) {
					if (e.getButton() == MouseButton.PRIMARY) {
						// Double-clicking leafs should 'open' their content.
						// Branches should recursively open.
						TreeItem<?> treeItem = treeCell.getTreeItem();
						if (e.getClickCount() == 2)
							if (treeItem.isLeaf())
								openPath(item);
							else
								TreeItems.recurseOpen(treeItem);
					}
				}
			}
		});
	}

	/**
	 * @param item
	 * 		Item to open.
	 */
	private void openPath(PathNode<?> item) {
		if (item instanceof ClassPathNode classPathNode) {
			actions.gotoDeclaration(classPathNode);
		}
	}

	/**
	 * @param item
	 * 		Item to create text for.
	 *
	 * @return Text for the item represented by the path.
	 */
	@SuppressWarnings("unchecked")
	private String textOf(@Nonnull PathNode<?> item) {
		Workspace workspace = item.getValueOfType(Workspace.class);
		WorkspaceResource resource = item.getValueOfType(WorkspaceResource.class);

		if (workspace == null) {
			logger.error("Path node missing workspace section: {}", item);
			return UNKNOWN_TEXT;
		}
		if (resource == null) {
			logger.error("Path node missing resource section: {}", item);
			return UNKNOWN_TEXT;
		}

		if (item instanceof ClassPathNode classPath) {
			ClassBundle<?> bundle = classPath.getValueOfType(ClassBundle.class);
			if (bundle == null) {
				logger.error("Class path node missing bundle section: {}", item);
				return UNKNOWN_TEXT;
			}

			ClassInfo info = classPath.getValue();
			if (info.isJvmClass()) {
				return textService.getJvmClassInfoTextProvider(workspace, resource,
						(JvmClassBundle) bundle, info.asJvmClass()).makeText();
			} else if (info.isAndroidClass()) {
				return textService.getAndroidClassInfoTextProvider(workspace, resource,
						(AndroidClassBundle) bundle, info.asAndroidClass()).makeText();
			}
		} else if (item instanceof FilePathNode filePath) {
			FileBundle bundle = filePath.getValueOfType(FileBundle.class);
			if (bundle == null) {
				logger.error("File path node missing bundle section: {}", item);
				return UNKNOWN_TEXT;
			}

			FileInfo info = filePath.getValue();
			return textService.getFileInfoTextProvider(workspace, resource, bundle, info).makeText();
		} else if (item instanceof ClassMemberPathNode memberNode) {
			ClassBundle<?> bundle = memberNode.getValueOfType(ClassBundle.class);
			if (bundle == null) {
				logger.error("Member path node missing bundle section: {}", item);
				return null;
			}

			ClassInfo classInfo = memberNode.getValueOfType(ClassInfo.class);
			if (classInfo == null) {
				logger.error("Member path node missing class section: {}", item);
				return null;
			}

			ClassMember member = memberNode.getValue();
			if (member instanceof FieldMember fieldMember) {
				return textService.getFieldMemberTextProvider(workspace, resource, bundle, classInfo, fieldMember).makeText();
			} else if (member instanceof MethodMember methodMember) {
				return textService.getMethodMemberTextProvider(workspace, resource, bundle, classInfo, methodMember).makeText();
			}
		} else if (item instanceof DirectoryPathNode directoryPath) {
			Bundle<?> bundle = directoryPath.getValueOfType(Bundle.class);
			if (bundle == null) {
				logger.error("Directory/package path node missing bundle section: {}", item);
				return UNKNOWN_TEXT;
			}

			if (bundle instanceof FileBundle fileBundle) {
				return textService.getDirectoryTextProvider(workspace, resource, fileBundle, directoryPath.getValue()).makeText();
			} else if (bundle instanceof ClassBundle<?> classBundle) {
				return textService.getPackageTextProvider(workspace, resource, classBundle, directoryPath.getValue()).makeText();
			}
		} else if (item instanceof BundlePathNode bundlePath) {
			return textService.getBundleTextProvider(workspace, resource, bundlePath.getValue()).makeText();
		} else if (item instanceof ResourcePathNode) {
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
	@SuppressWarnings("unchecked")
	private Node graphicOf(@Nonnull PathNode<?> item) {
		Workspace workspace = item.getValueOfType(Workspace.class);
		WorkspaceResource resource = item.getValueOfType(WorkspaceResource.class);

		if (workspace == null) {
			logger.error("Path node missing workspace section: {}", item);
			return UNKNOWN_GRAPHIC;
		}
		if (resource == null) {
			logger.error("Path node missing resource section: {}", item);
			return UNKNOWN_GRAPHIC;
		}

		if (item instanceof ClassPathNode classPath) {
			ClassBundle<?> bundle = classPath.getValueOfType(ClassBundle.class);
			if (bundle == null) {
				logger.error("Class path node missing bundle section: {}", item);
				return UNKNOWN_GRAPHIC;
			}

			ClassInfo info = classPath.getValue();
			if (info.isJvmClass()) {
				return iconService.getJvmClassInfoIconProvider(workspace, resource,
						(JvmClassBundle) bundle, info.asJvmClass()).makeIcon();
			} else if (info.isAndroidClass()) {
				return iconService.getAndroidClassInfoIconProvider(workspace, resource,
						(AndroidClassBundle) bundle, info.asAndroidClass()).makeIcon();
			}
		} else if (item instanceof FilePathNode filePath) {
			FileBundle bundle = filePath.getValueOfType(FileBundle.class);
			if (bundle == null) {
				logger.error("File path node missing bundle section: {}", item);
				return UNKNOWN_GRAPHIC;
			}

			FileInfo info = filePath.getValue();
			return iconService.getFileInfoIconProvider(workspace, resource, bundle, info).makeIcon();
		} else if (item instanceof ClassMemberPathNode memberNode) {
			ClassBundle<?> bundle = memberNode.getValueOfType(ClassBundle.class);
			if (bundle == null) {
				logger.error("Member path node missing bundle section: {}", item);
				return null;
			}

			ClassInfo classInfo = memberNode.getValueOfType(ClassInfo.class);
			if (classInfo == null) {
				logger.error("Member path node missing class section: {}", item);
				return null;
			}

			ClassMember member = memberNode.getValue();
			return iconService.getClassMemberIconProvider(workspace, resource, bundle, classInfo, member).makeIcon();
		} else if (item instanceof DirectoryPathNode directoryPath) {
			Bundle<?> bundle = directoryPath.getValueOfType(Bundle.class);
			if (bundle == null) {
				logger.error("Directory/package path node missing bundle section: {}", item);
				return UNKNOWN_GRAPHIC;
			}

			if (bundle instanceof FileBundle fileBundle) {
				return iconService.getDirectoryIconProvider(workspace, resource, fileBundle, directoryPath.getValue()).makeIcon();
			} else if (bundle instanceof ClassBundle<?> classBundle) {
				return iconService.getPackageIconProvider(workspace, resource, classBundle, directoryPath.getValue()).makeIcon();
			}
		} else if (item instanceof BundlePathNode bundlePath) {
			return iconService.getBundleIconProvider(workspace, resource, bundlePath.getValue()).makeIcon();
		} else if (item instanceof ResourcePathNode) {
			return iconService.getResourceIconProvider(workspace, resource).makeIcon();
		}

		// No graphic
		return null;
	}

	/**
	 * @param source
	 * 		Origin source of the cell, for context menu specialization.
	 * @param item
	 * 		Item to create a context-menu for.
	 *
	 * @return Context-menu for the item represented by the path.
	 */
	@SuppressWarnings("unchecked")
	private ContextMenu contextMenuOf(@Nonnull ContextSource source, @Nonnull PathNode<?> item) {
		Workspace workspace = item.getValueOfType(Workspace.class);
		WorkspaceResource resource = item.getValueOfType(WorkspaceResource.class);

		if (workspace == null) {
			logger.error("Path node missing workspace section: {}", item);
			return null;
		}
		if (resource == null) {
			logger.error("Path node missing resource section: {}", item);
			return null;
		}

		if (item instanceof ClassPathNode classPath) {
			ClassBundle<?> bundle = classPath.getValueOfType(ClassBundle.class);
			if (bundle == null) {
				logger.error("Class path node missing bundle section: {}", item);
				return null;
			}

			ClassInfo info = classPath.getValue();
			if (info.isJvmClass()) {
				return contextMenuService.getJvmClassInfoContextMenuProvider(source, workspace, resource,
						(JvmClassBundle) bundle, info.asJvmClass()).makeMenu();
			} else if (info.isAndroidClass()) {
				return contextMenuService.getAndroidClassInfoContextMenuProvider(source, workspace, resource,
						(AndroidClassBundle) bundle, info.asAndroidClass()).makeMenu();
			}
		} else if (item instanceof FilePathNode filePath) {
			FileBundle bundle = filePath.getValueOfType(FileBundle.class);
			if (bundle == null) {
				logger.error("File path node missing bundle section: {}", item);
				return null;
			}

			FileInfo info = filePath.getValue();
			return contextMenuService.getFileInfoContextMenuProvider(source, workspace, resource, bundle, info).makeMenu();
		} else if (item instanceof ClassMemberPathNode memberNode) {
			ClassBundle<?> bundle = memberNode.getValueOfType(ClassBundle.class);
			if (bundle == null) {
				logger.error("Member path node missing bundle section: {}", item);
				return null;
			}

			ClassInfo classInfo = memberNode.getValueOfType(ClassInfo.class);
			if (classInfo == null) {
				logger.error("Member path node missing class section: {}", item);
				return null;
			}

			ClassMember member = memberNode.getValue();
			return contextMenuService.getClassMemberContextMenuProvider(source, workspace, resource, bundle, classInfo, member).makeMenu();
		} else if (item instanceof DirectoryPathNode directoryPath) {
			Bundle<?> bundle = directoryPath.getValueOfType(Bundle.class);
			if (bundle == null) {
				logger.error("Directory/package path node missing bundle section: {}", item);
				return null;
			}

			if (bundle instanceof FileBundle fileBundle) {
				return contextMenuService.getDirectoryContextMenuProvider(source, workspace, resource, fileBundle, directoryPath.getValue()).makeMenu();
			} else if (bundle instanceof ClassBundle<?> classBundle) {
				return contextMenuService.getPackageContextMenuProvider(source, workspace, resource, classBundle, directoryPath.getValue()).makeMenu();
			}
		} else if (item instanceof BundlePathNode bundlePath) {
			return contextMenuService.getBundleContextMenuProvider(source, workspace, resource, bundlePath.getValue()).makeMenu();
		} else if (item instanceof ResourcePathNode) {
			return contextMenuService.getResourceContextMenuProvider(source, workspace, resource).makeMenu();
		}

		// No menu
		return null;
	}
}
