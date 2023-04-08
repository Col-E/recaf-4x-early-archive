package software.coley.recaf.services.cell.builtin;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import org.kordamp.ikonli.carbonicons.CarbonIcons;
import org.slf4j.Logger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.info.AndroidClassInfo;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.InnerClassInfo;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.path.ClassPathNode;
import software.coley.recaf.services.cell.*;
import software.coley.recaf.services.mapping.IntermediateMappings;
import software.coley.recaf.services.mapping.MappingApplier;
import software.coley.recaf.services.mapping.MappingResults;
import software.coley.recaf.services.navigation.Actions;
import software.coley.recaf.ui.control.ActionMenuItem;
import software.coley.recaf.ui.control.popup.NamePopup;
import software.coley.recaf.util.EscapeUtil;
import software.coley.recaf.util.Menus;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.AndroidClassBundle;
import software.coley.recaf.workspace.model.bundle.ClassBundle;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static software.coley.recaf.util.Menus.action;

/**
 * Basic implementation for {@link ClassContextMenuProviderFactory}.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class BasicClassContextMenuProviderFactory extends AbstractContextMenuProviderFactory
		implements ClassContextMenuProviderFactory {
	private static final Logger logger = Logging.get(BasicClassContextMenuProviderFactory.class);
	private final Instance<MappingApplier> applierProvider;

	@Inject
	public BasicClassContextMenuProviderFactory(@Nonnull TextProviderService textService,
												@Nonnull IconProviderService iconService,
												@Nonnull Actions actions,
												@Nonnull Instance<MappingApplier> applierProvider) {
		super(textService, iconService, actions);
		this.applierProvider = applierProvider;
	}

	@Nonnull
	@Override
	public ContextMenuProvider getJvmClassInfoContextMenuProvider(@Nonnull ContextSource source,
																  @Nonnull Workspace workspace,
																  @Nonnull WorkspaceResource resource,
																  @Nonnull JvmClassBundle bundle,
																  @Nonnull JvmClassInfo info) {
		return () -> {
			ContextMenu menu = createMenu(source, workspace, resource, bundle, info);
			populateJvmMenu(menu, source, workspace, resource, bundle, info);
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
			ContextMenu menu = createMenu(source, workspace, resource, bundle, info);
			populateAndroidMenu(menu, source, workspace, resource, bundle, info);
			return menu;
		};
	}

	/**
	 * @param source
	 * 		Context source.
	 * @param workspace
	 * 		Containing workspace.
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param info
	 * 		The class to create a menu for.
	 *
	 * @return Initial menu header for the class.
	 */
	private ContextMenu createMenu(@Nonnull ContextSource source,
								   @Nonnull Workspace workspace,
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
	 * @param source
	 * 		Context source.
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
								 @Nonnull ContextSource source,
								 @Nonnull Workspace workspace,
								 @Nonnull WorkspaceResource resource,
								 @Nonnull JvmClassBundle bundle,
								 @Nonnull JvmClassInfo info) {
		ObservableList<MenuItem> items = menu.getItems();
		if (source.isReference()) {
			items.add(action("menu.goto.class", CarbonIcons.ARROW_RIGHT,
					() -> actions.gotoDeclaration(workspace, resource, bundle, info)));
		} else if (source.isDeclaration()) {
			ActionMenuItem copy = action("menu.edit.copy", CarbonIcons.COPY_FILE, () -> {
				String originalName = info.getName();
				Consumer<String> copyTask = newName -> {
					// Create mappings
					IntermediateMappings mappings = new IntermediateMappings();
					mappings.addClass(originalName, newName);

					// Collect inner classes, we need to copy these as well
					List<JvmClassInfo> classesToCopy = new ArrayList<>();
					classesToCopy.add(info);
					for (InnerClassInfo inner : info.getInnerClasses()) {
						if (inner.isExternalReference()) continue;
						String innerClassName = inner.getInnerClassName();
						mappings.addClass(innerClassName, newName + innerClassName.substring(originalName.length()));
						JvmClassInfo innerClassInfo = bundle.get(innerClassName);
						if (innerClassInfo != null)
							classesToCopy.add(innerClassInfo);
						else
							logger.warn("Could not find inner class for copy-operation: {}", EscapeUtil.escapeStandard(innerClassName));
					}

					// Apply mappings to create copies of the affected classes, using the provided name.
					// Then dump the mapped classes into bundle.
					MappingApplier applier = applierProvider.get();
					MappingResults results = applier.applyToClasses(mappings, resource, bundle, classesToCopy);
					for (ClassPathNode mappedClassPath : results.getPostMappingPaths().values()) {
						JvmClassInfo mappedClass = mappedClassPath.getValue().asJvmClass();
						bundle.put(mappedClass);
					}
				};
				new NamePopup(copyTask)
						.withInitialClassName(originalName)
						.forClassCopy(bundle)
						.show();
			});
			ActionMenuItem delete = action("menu.edit.delete", CarbonIcons.DELETE, () -> {
				// TODO: Ask user if they are sure
				//  - Use config to check if "are you sure" prompts should be bypassed
				bundle.remove(info.getName());
			});
			// TODO: implement operations
			//  - Edit
			//    - (class assembler)
			//    - Add field
			//    - Add method
			//    - Add annotation
			//    - Remove fields
			//    - Remove methods
			//    - Remove annotations
			items.add(copy);
			items.add(delete);
		}
		// TODO: implement operations
		//  - Refactor
		//    - Rename
		//    - Move
		//  - Search references
		//  - View
		//    - Class hierarchy
		//  - Deobfuscate
		//    - Suggest class name / purpose
		//    - Suggest method names / purposes (get/set)
		//    - Organize fields (constants -> finals -> non-finals
	}

	/**
	 * Append Android specific operations to the given menu.
	 *
	 * @param menu
	 * 		Menu to append content to.
	 * @param source
	 * 		Context source.
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
									 ContextSource source, @Nonnull Workspace workspace,
									 @Nonnull WorkspaceResource resource,
									 @Nonnull AndroidClassBundle bundle,
									 @Nonnull AndroidClassInfo info) {
		// TODO: implement operations
		//  - Edit
		//    - (class assembler)
		//    - Add field
		//    - Add method
		//    - Add annotation
		//    - Remove fields
		//    - Remove methods
		//    - Remove annotations
		//  - Copy
		//  - Delete
		//  - Refactor
		//    - Rename
		//    - Move
		//  - Search references
		//  - View
		//    - Class hierarchy
		//  - Deobfuscate
		//    - Suggest class name / purpose
		//    - Suggest method names / purposes (get/set)
		ObservableList<MenuItem> items = menu.getItems();
		items.add(action("menu.goto.class", CarbonIcons.ARROW_RIGHT,
				() -> actions.gotoDeclaration(workspace, resource, bundle, info)));
	}
}
