package software.coley.recaf.services.cell.builtin;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import org.kordamp.ikonli.carbonicons.CarbonIcons;
import org.slf4j.Logger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.member.MethodMember;
import software.coley.recaf.path.ClassPathNode;
import software.coley.recaf.path.IncompletePathException;
import software.coley.recaf.path.WorkspacePathNode;
import software.coley.recaf.services.cell.*;
import software.coley.recaf.services.navigation.Actions;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.ClassBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import static software.coley.recaf.util.Menus.action;

/**
 * Basic implementation for {@link MethodContextMenuProviderFactory}.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class BasicMethodContextMenuProviderFactory implements MethodContextMenuProviderFactory {
	private static final Logger logger = Logging.get(BasicMethodContextMenuProviderFactory.class);
	private final TextProviderService textService;
	private final IconProviderService iconService;
	private final Actions actions;

	@Inject
	public BasicMethodContextMenuProviderFactory(@Nonnull TextProviderService textService,
												 @Nonnull IconProviderService iconService,
												 @Nonnull Actions actions) {
		this.textService = textService;
		this.iconService = iconService;
		this.actions = actions;
	}

	@Nonnull
	@Override
	public ContextMenuProvider getMethodContextMenuProvider(@Nonnull ContextSource source,
															@Nonnull Workspace workspace,
															@Nonnull WorkspaceResource resource,
															@Nonnull ClassBundle<? extends ClassInfo> bundle,
															@Nonnull ClassInfo declaringClass,
															@Nonnull MethodMember method) {
		return () -> {
			TextProvider nameProvider = textService.getMethodMemberTextProvider(workspace, resource, bundle, declaringClass, method);
			IconProvider iconProvider = iconService.getClassMemberIconProvider(workspace, resource, bundle, declaringClass, method);
			ContextMenu menu = new ContextMenu();
			addHeader(menu, nameProvider.makeText(), iconProvider.makeIcon());

			ObservableList<MenuItem> items = menu.getItems();
			items.add(action("menu.goto.method", CarbonIcons.ARROW_RIGHT,
					() -> {
						ClassPathNode classPath = new WorkspacePathNode(workspace)
								.child(resource)
								.child(bundle)
								.child(declaringClass.getPackageName())
								.child(declaringClass);
						try {
							actions.gotoDeclaration(classPath)
									.requestFocus(method);
						} catch (IncompletePathException ex) {
							logger.error("Cannot go to method due to incomplete path", ex);
						}
					}));
			// TODO: implement additional operations
			return menu;
		};
	}
}
