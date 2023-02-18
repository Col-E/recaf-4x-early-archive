package software.coley.recaf.services.cell.builtin;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javafx.scene.control.ContextMenu;
import software.coley.recaf.info.AndroidClassInfo;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.services.cell.*;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.AndroidClassBundle;
import software.coley.recaf.workspace.model.bundle.ClassBundle;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

/**
 * Basic implementation for {@link ClassContextMenuProviderFactory}.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class BasicClassContextMenuProviderFactory implements ClassContextMenuProviderFactory {
	private final TextProviderService textService;
	private final IconProviderService iconService;

	@Inject
	public BasicClassContextMenuProviderFactory(@Nonnull TextProviderService textService,
												@Nonnull IconProviderService iconService) {
		this.textService = textService;
		this.iconService = iconService;
	}

	@Nonnull
	@Override
	public ContextMenuProvider getJvmClassInfoContextMenuProvider(@Nonnull ContextSource source,
																  @Nonnull Workspace workspace,
																  @Nonnull WorkspaceResource resource,
																  @Nonnull JvmClassBundle bundle,
																  @Nonnull JvmClassInfo info) {
		return () -> {
			ContextMenu menu = createCommon(workspace, resource, bundle, info);
			populateJvmMenu(menu, workspace, resource, bundle, info);
			return menu;
		};
	}

	@Nonnull
	@Override
	public ContextMenuProvider getAndroidClassInfoContextMenuProvider(@Nonnull ContextSource source,
																	  @Nonnull Workspace workspace,
																	  @Nonnull WorkspaceResource resource,
																	  @Nonnull AndroidClassBundle bundle,
																	  @Nonnull AndroidClassInfo info) {
		return () -> {
			ContextMenu menu = createCommon(workspace, resource, bundle, info);
			populateAndroidMenu(menu, workspace, resource, bundle, info);
			return menu;
		};
	}

	/**
	 * @param workspace
	 * 		Containing workspace.
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param info
	 * 		The class to create a menu for.
	 *
	 * @return Initial menu with common setup between JVM and Android classes.
	 */
	private ContextMenu createCommon(@Nonnull Workspace workspace,
									 @Nonnull WorkspaceResource resource,
									 @Nonnull ClassBundle<? extends ClassInfo> bundle,
									 @Nonnull ClassInfo info) {
		TextProvider nameProvider;
		IconProvider iconProvider;
		if (info.isJvmClass()) {
			nameProvider = textService.getJvmClassInfoTextProvider(workspace, resource,
					(JvmClassBundle) bundle, info.asJvmClass());
			iconProvider = iconService.getJvmClassInfoIconProvider(workspace, resource,
					(JvmClassBundle) bundle, info.asJvmClass());
		} else if (info.isAndroidClass()) {
			nameProvider = textService.getAndroidClassInfoTextProvider(workspace, resource,
					(AndroidClassBundle) bundle, info.asAndroidClass());
			iconProvider = iconService.getAndroidClassInfoIconProvider(workspace, resource,
					(AndroidClassBundle) bundle, info.asAndroidClass());
		} else {
			throw new IllegalStateException("Unknown class type: " + info.getClass().getName());
		}
		ContextMenu menu = new ContextMenu();
		addHeader(menu, nameProvider.makeText(), iconProvider.makeIcon());
		return menu;
	}

	/**
	 * Append JVM specific operations to the given menu.
	 *
	 * @param menu
	 * 		Menu to append content to.
	 * @param workspace
	 * 		Containing workspace.
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param info
	 * 		The class to create a menu for.
	 */
	private void populateJvmMenu(@Nonnull ContextMenu menu,
								 @Nonnull Workspace workspace,
								 @Nonnull WorkspaceResource resource,
								 @Nonnull JvmClassBundle bundle,
								 @Nonnull JvmClassInfo info) {
		// TODO: implement operations
	}

	/**
	 * Append Android specific operations to the given menu.
	 *
	 * @param menu
	 * 		Menu to append content to.
	 * @param workspace
	 * 		Containing workspace.
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param info
	 * 		The class to create a menu for.
	 */
	private void populateAndroidMenu(@Nonnull ContextMenu menu,
									 @Nonnull Workspace workspace,
									 @Nonnull WorkspaceResource resource,
									 @Nonnull AndroidClassBundle bundle,
									 @Nonnull AndroidClassInfo info) {
		// TODO: implement operations
	}
}
