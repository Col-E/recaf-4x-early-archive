package software.coley.recaf.workspace.model;

import jakarta.annotation.Nonnull;
import software.coley.recaf.behavior.Closing;
import software.coley.recaf.workspace.WorkspaceModificationListener;
import software.coley.recaf.workspace.WorkspaceManager;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Models a collection of user inputs, represented as {@link WorkspaceResource} instances.
 *
 * @author Matt Coley
 */
public interface Workspace extends Closing {
	/**
	 * @return The primary resource, holding classes and files to modify.
	 */
	@Nonnull
	WorkspaceResource getPrimaryResource();

	/**
	 * @return List of <i>all</i> supporting resources, including
	 * {@link #getInternalSupportingResources() internal supporting resources} that are added automatically.
	 * These resources do not support editing capabilities, but support other operations.
	 */
	@Nonnull
	List<WorkspaceResource> getSupportingResources();

	/**
	 * @return List of internal supporting resources. These are added automatically by Recaf to all workspaces.
	 */
	@Nonnull
	default List<WorkspaceResource> getInternalSupportingResources() {
		return getSupportingResources().stream()
				.filter(WorkspaceResource::isInternal)
				.collect(Collectors.toList());
	}

	/**
	 * @return Listeners for when the current workspace has its supporting resources updated.
	 */
	@Nonnull
	List<WorkspaceModificationListener> getWorkspaceModificationListeners();

	/**
	 * @param listener Modification listener to add.
	 */
	void addWorkspaceModificationListener(WorkspaceModificationListener listener);

	/**
	 * @param listener Modification listener to remove.
	 */
	void removeWorkspaceModificationListener(WorkspaceModificationListener listener);

	/**
	 * Called by {@link WorkspaceManager} when the workspace is closed.
	 */
	@Override
	default void close() {
		getWorkspaceModificationListeners().clear();
		getSupportingResources().forEach(Closing::close);
		getPrimaryResource().close();
	}
}
