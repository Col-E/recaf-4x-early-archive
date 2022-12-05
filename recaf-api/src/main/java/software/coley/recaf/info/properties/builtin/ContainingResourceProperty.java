package software.coley.recaf.info.properties.builtin;

import jakarta.annotation.Nullable;
import software.coley.recaf.info.properties.BasicProperty;
import software.coley.recaf.info.properties.PropertyContainer;
import software.coley.recaf.workspace.model.WorkspaceResource;

/**
 * Built in property associating a property container info-type with a {@link WorkspaceResource}.
 *
 * @author Matt Coley
 */
public class ContainingResourceProperty extends BasicProperty<WorkspaceResource> {
	public static final String KEY = "associated-workspace-resource";

	// TODO: When creating a new workspace, iterate over all info objects and assign this property

	/**
	 * @param value
	 * 		Workspace resource
	 */
	public ContainingResourceProperty(WorkspaceResource value) {
		super(KEY, value);
	}

	/**
	 * @param container
	 * 		Container to associate with the resource.
	 * @param resource
	 * 		Resource to associate with.
	 */
	public static void set(PropertyContainer container, WorkspaceResource resource) {
		container.setProperty(KEY, new ContainingResourceProperty(resource));
	}

	/**
	 * @param container
	 * 		Container to retrieve value in.
	 *
	 * @return Associated resource, or {@code null} if no association exists.
	 */
	@Nullable
	public static WorkspaceResource get(PropertyContainer container) {
		return container.getPropertyValueOrNull(KEY);
	}
}
