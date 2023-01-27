package software.coley.recaf.info.properties.builtin;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.recaf.info.properties.BasicProperty;
import software.coley.recaf.info.properties.PropertyContainer;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

/**
 * Built in property associating a property container info-type with a {@link WorkspaceResource}.
 *
 * @author Matt Coley
 */
public class ContainingResourceProperty extends BasicProperty<WorkspaceResource> {
	public static final String KEY = "associated-workspace-resource";

	/**
	 * @param value
	 * 		Workspace resource
	 */
	public ContainingResourceProperty(@Nonnull WorkspaceResource value) {
		super(KEY, value);
	}

	/**
	 * @param container
	 * 		Container to associate with the resource.
	 * @param resource
	 * 		Resource to associate with.
	 */
	public static void set(@Nonnull PropertyContainer container, @Nonnull WorkspaceResource resource) {
		container.setProperty(new ContainingResourceProperty(resource));
	}

	/**
	 * @param container
	 * 		Container to disassociate with any resource.
	 */
	public static void remove(@Nonnull PropertyContainer container) {
		container.removeProperty(KEY);
	}

	/**
	 * @param container
	 * 		Container to retrieve value in.
	 *
	 * @return Associated resource, or {@code null} if no association exists.
	 */
	@Nullable
	public static WorkspaceResource get(@Nonnull PropertyContainer container) {
		return container.getPropertyValueOrNull(KEY);
	}
}
