package software.coley.recaf.services.cell;

import jakarta.annotation.Nonnull;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.member.FieldMember;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.ClassBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

/**
 * Icon provider for {@link FieldMember fields}, to be plugged into {@link IconProviderService}
 * to allow for third party icon customization.
 *
 * @author Matt Coley
 */
public interface FieldIconProviderFactory extends IconProviderFactory {
	/**
	 * @param workspace
	 * 		Containing workspace.
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param declaringClass
	 * 		Containing class.
	 * @param field
	 * 		The field to create an icon for.
	 *
	 * @return Icon provider for the field.
	 */
	@Nonnull
	default IconProvider getFieldMemberIconProvider(@Nonnull Workspace workspace,
													@Nonnull WorkspaceResource resource,
													@Nonnull ClassBundle<?> bundle,
													@Nonnull ClassInfo declaringClass,
													@Nonnull FieldMember field) {
		return emptyProvider();
	}
}
