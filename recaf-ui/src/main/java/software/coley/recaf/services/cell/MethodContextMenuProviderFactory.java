package software.coley.recaf.services.cell;

import jakarta.annotation.Nonnull;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.member.MethodMember;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.ClassBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

/**
 * Context menu provider for {@link MethodMember types}, to be plugged into {@link ContextMenuProviderService}
 * to allow for third party menu customization.
 *
 * @author Matt Coley
 */
public interface MethodContextMenuProviderFactory extends ContextMenuProviderFactory {
	/**
	 * @param workspace
	 * 		Containing workspace.
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param declaringClass
	 * 		Containing class.
	 * @param method
	 * 		The method to create a menu for.
	 *
	 * @return Menu provider for the method.
	 */
	@Nonnull
	default ContextMenuProvider getMethodContextMenuProvider(@Nonnull Workspace workspace,
															 @Nonnull WorkspaceResource resource,
															 @Nonnull ClassBundle<?> bundle,
															 @Nonnull ClassInfo declaringClass,
															 @Nonnull MethodMember method) {
		return emptyProvider();
	}
}
