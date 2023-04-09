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
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.info.AndroidClassInfo;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.InnerClassInfo;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.info.annotation.AnnotationInfo;
import software.coley.recaf.info.member.FieldMember;
import software.coley.recaf.info.member.MethodMember;
import software.coley.recaf.path.ClassPathNode;
import software.coley.recaf.services.cell.*;
import software.coley.recaf.services.mapping.IntermediateMappings;
import software.coley.recaf.services.mapping.MappingApplier;
import software.coley.recaf.services.mapping.MappingResults;
import software.coley.recaf.services.navigation.Actions;
import software.coley.recaf.ui.control.ActionMenuItem;
import software.coley.recaf.ui.control.popup.ItemSelectionPopup;
import software.coley.recaf.ui.control.popup.NamePopup;
import software.coley.recaf.util.EscapeUtil;
import software.coley.recaf.util.Lang;
import software.coley.recaf.util.Menus;
import software.coley.recaf.util.visitors.ClassAnnotationRemovingVisitor;
import software.coley.recaf.util.visitors.MemberPredicate;
import software.coley.recaf.util.visitors.MemberRemovingVisitor;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.AndroidClassBundle;
import software.coley.recaf.workspace.model.bundle.ClassBundle;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
					// Create mappings.
					IntermediateMappings mappings = new IntermediateMappings();
					mappings.addClass(originalName, newName);

					// Collect inner classes, we need to copy these as well.
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
			Menu edit = Menus.menu("menu.edit", CarbonIcons.EDIT);
			ActionMenuItem removeFields = action("menu.edit.remove.field", CarbonIcons.CLOSE, () -> {
				ItemSelectionPopup.forFields(info, fields -> {
							ClassWriter writer = new ClassWriter(0);
							MemberRemovingVisitor visitor = new MemberRemovingVisitor(writer, new MemberPredicate() {
								@Override
								public boolean matchField(int access, String name, String desc, String sig, Object value) {
									for (FieldMember field : fields)
										if (field.getName().equals(name) && field.getDescriptor().equals(desc))
											return true;
									return false;
								}

								@Override
								public boolean matchMethod(int access, String name, String desc, String sig, String[] exceptions) {
									return false;
								}
							});
							info.getClassReader().accept(visitor, 0);
							bundle.put(info.toBuilder()
									.adaptFrom(new ClassReader(writer.toByteArray()))
									.build());
						})
						.withMultipleSelection()
						.withTitle(Lang.getBinding("menu.edit.remove.field"))
						.withTextMapping(field -> textService.getFieldMemberTextProvider(workspace, resource, bundle, info, field).makeText())
						.withGraphicMapping(field -> iconService.getClassMemberIconProvider(workspace, resource, bundle, info, field).makeIcon())
						.show();
			});
			ActionMenuItem removeMethods = action("menu.edit.remove.method", CarbonIcons.CLOSE, () -> {
				ItemSelectionPopup.forMethods(info, methods -> {
							ClassWriter writer = new ClassWriter(0);
							MemberRemovingVisitor visitor = new MemberRemovingVisitor(writer, new MemberPredicate() {
								@Override
								public boolean matchField(int access, String name, String desc, String sig, Object value) {
									return false;
								}

								@Override
								public boolean matchMethod(int access, String name, String desc, String sig, String[] exceptions) {
									for (MethodMember method : methods)
										if (method.getName().equals(name) && method.getDescriptor().equals(desc))
											return true;
									return false;
								}
							});
							info.getClassReader().accept(visitor, 0);
							bundle.put(info.toBuilder()
									.adaptFrom(new ClassReader(writer.toByteArray()))
									.build());
						})
						.withMultipleSelection()
						.withTitle(Lang.getBinding("menu.edit.remove.method"))
						.withTextMapping(method -> textService.getMethodMemberTextProvider(workspace, resource, bundle, info, method).makeText())
						.withGraphicMapping(method -> iconService.getClassMemberIconProvider(workspace, resource, bundle, info, method).makeIcon())
						.show();
			});
			ActionMenuItem removeAnnotations = action("menu.edit.remove.annotation", CarbonIcons.CLOSE, () -> {
				ItemSelectionPopup.forAnnotationRemoval(info, annotations -> {
							List<String> names = annotations.stream()
									.map(AnnotationInfo::getDescriptor)
									.map(desc -> desc.substring(1, desc.length() - 1))
									.collect(Collectors.toList());
							ClassWriter writer = new ClassWriter(0);
							ClassAnnotationRemovingVisitor visitor = new ClassAnnotationRemovingVisitor(writer, names);
							info.getClassReader().accept(visitor, 0);
							bundle.put(info.toBuilder()
									.adaptFrom(new ClassReader(writer.toByteArray()))
									.build());
						})
						.withMultipleSelection()
						.withTitle(Lang.getBinding("menu.edit.remove.annotation"))
						.withTextMapping(anno -> textService.getAnnotationTextProvider(workspace, resource, bundle, info, anno).makeText())
						.withGraphicMapping(anno -> iconService.getAnnotationIconProvider(workspace, resource, bundle, info, anno).makeIcon())
						.show();
			});
			// TODO: Implement these operations after assembler is added.
			//  - For add operations, can use the assembler, using a template for each item
			ActionMenuItem editClass = action("menu.edit.assemble.class", CarbonIcons.EDIT, () -> {
			});
			ActionMenuItem addField = action("menu.edit.add.field", CarbonIcons.ADD_ALT, () -> {
			});
			ActionMenuItem addMethod = action("menu.edit.add.method", CarbonIcons.ADD_ALT, () -> {
			});
			ActionMenuItem addAnnotation = action("menu.edit.add.annotation", CarbonIcons.ADD_ALT, () -> {
			});
			edit.getItems().addAll(
					editClass,
					addField,
					addMethod,
					addAnnotation,
					removeFields,
					removeMethods,
					removeAnnotations
			);
			items.add(edit);
			items.add(copy);
			items.add(delete);
			// Disable items if not applicable
			removeFields.setDisable(info.getFields().isEmpty());
			removeMethods.setDisable(info.getMethods().isEmpty());
			removeAnnotations.setDisable(info.getAnnotations().isEmpty());
			editClass.setDisable(true);
			addField.setDisable(true);
			addMethod.setDisable(true);
			addAnnotation.setDisable(true);
		}
		Menu refactor = Menus.menu("menu.refactor", CarbonIcons.PAINT_BRUSH);
		ActionMenuItem move = action("menu.refactor.move", CarbonIcons.STACKED_MOVE, () -> {
			ItemSelectionPopup.forPackageNames(bundle, packages -> {
						// We only allow a single package, so the list should contain just one item.
						String oldPackage = info.getPackageName() + "/";
						String newPackage = packages.get(0) + "/";
						if (Objects.equals(oldPackage, newPackage)) return;

						// Create mapping for the class and any inner classes.
						String originalName = info.getName();
						String newName = newPackage + info.getName().substring(oldPackage.length());
						IntermediateMappings mappings = new IntermediateMappings();
						for (InnerClassInfo inner : info.getInnerClasses()) {
							if (inner.isExternalReference()) continue;
							String innerClassName = inner.getInnerClassName();
							mappings.addClass(innerClassName, newName + innerClassName.substring(originalName.length()));
						}

						// Apply the mappings.
						MappingApplier applier = applierProvider.get();
						MappingResults results = applier.applyToPrimary(mappings);
						results.apply();
					})
					.withTitle(Lang.getBinding("dialog.title.move-class"))
					.withTextMapping(name -> textService.getPackageTextProvider(workspace, resource, bundle, name).makeText())
					.withGraphicMapping(name -> iconService.getPackageIconProvider(workspace, resource, bundle, name).makeIcon())
					.show();
		});
		ActionMenuItem rename = action("menu.refactor.rename", CarbonIcons.TAG_EDIT, () -> {
			String originalName = info.getName();
			Consumer<String> renameTask = newName -> {
				// Create mapping for the class and any inner classes.
				IntermediateMappings mappings = new IntermediateMappings();
				mappings.addClass(originalName, newName);
				for (InnerClassInfo inner : info.getInnerClasses()) {
					if (inner.isExternalReference()) continue;
					String innerClassName = inner.getInnerClassName();
					mappings.addClass(innerClassName, newName + innerClassName.substring(originalName.length()));
				}

				// Apply the mappings.
				MappingApplier applier = applierProvider.get();
				MappingResults results = applier.applyToPrimary(mappings);
				results.apply();
			};
			new NamePopup(renameTask)
					.withInitialClassName(originalName)
					.forClassRename(bundle)
					.show();
		});
		refactor.getItems().addAll(move, rename);
		items.add(refactor);
		// TODO: implement operations
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
									 @Nonnull ContextSource source,
									 @Nonnull Workspace workspace,
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
