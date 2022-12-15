package software.coley.recaf.workspace.model;

import jakarta.annotation.Nonnull;
import software.coley.recaf.behavior.Closing;
import software.coley.recaf.info.AndroidClassInfo;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.FileInfo;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.workspace.WorkspaceManager;
import software.coley.recaf.workspace.WorkspaceModificationListener;
import software.coley.recaf.workspace.model.bundle.AndroidClassBundle;
import software.coley.recaf.workspace.model.bundle.FileBundle;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceFileResource;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;
import software.coley.recaf.workspace.query.QueryResult;

import java.util.ArrayList;
import java.util.List;

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
	 * @return List of <i>all</i> supporting resources, not including
	 * {@link #getInternalSupportingResources() internal supporting resources}.
	 */
	@Nonnull
	List<WorkspaceResource> getSupportingResources();

	/**
	 * @return List of internal supporting resources. These are added automatically by Recaf to all workspaces.
	 */
	@Nonnull
	List<WorkspaceResource> getInternalSupportingResources();

	/**
	 * @return Listeners for when the current workspace has its supporting resources updated.
	 */
	@Nonnull
	List<WorkspaceModificationListener> getWorkspaceModificationListeners();

	/**
	 * @param listener
	 * 		Modification listener to add.
	 */
	void addWorkspaceModificationListener(WorkspaceModificationListener listener);

	/**
	 * @param listener
	 * 		Modification listener to remove.
	 */
	void removeWorkspaceModificationListener(WorkspaceModificationListener listener);

	/**
	 * @param name
	 * 		Class name.
	 *
	 * @return Result of lookup.
	 */
	default QueryResult<? extends ClassInfo> findAnyClass(String name) {
		QueryResult<? extends ClassInfo> result = findJvmClass(name);
		if (result.isEmpty())
			result = findVersionedJvmClass(name);
		if (result.isEmpty())
			result = findAndroidClass(name);
		return result;
	}

	/**
	 * @param name
	 * 		Class name.
	 *
	 * @return Result of lookup.
	 */
	default QueryResult<JvmClassInfo> findJvmClass(String name) {
		List<WorkspaceResource> resources = new ArrayList<>(getSupportingResources());
		resources.add(0, getPrimaryResource());
		resources.addAll(getInternalSupportingResources());
		for (WorkspaceResource resource : resources) {
			JvmClassBundle bundle = resource.getJvmClassBundle();
			JvmClassInfo classInfo = bundle.get(name);
			if (classInfo != null)
				return new QueryResult<>(this, resource, bundle, classInfo);
		}
		return new QueryResult<>(this, null, null, null);
	}

	/**
	 * @param name
	 * 		Class name.
	 *
	 * @return Result of lookup.
	 */
	default QueryResult<JvmClassInfo> findVersionedJvmClass(String name) {
		List<WorkspaceResource> resources = new ArrayList<>(getSupportingResources());
		resources.add(0, getPrimaryResource());
		for (WorkspaceResource resource : resources) {
			for (JvmClassBundle bundle : resource.getVersionedJvmClassBundles().values()) {
				JvmClassInfo classInfo = bundle.get(name);
				if (classInfo != null)
					return new QueryResult<>(this, resource, bundle, classInfo);
			}
		}
		return new QueryResult<>(this, null, null, null);
	}

	/**
	 * @param name
	 * 		Class name.
	 *
	 * @return Result of lookup.
	 */
	default QueryResult<AndroidClassInfo> findAndroidClass(String name) {
		List<WorkspaceResource> resources = new ArrayList<>(getSupportingResources());
		resources.add(0, getPrimaryResource());
		for (WorkspaceResource resource : resources) {
			for (AndroidClassBundle bundle : resource.getAndroidClassBundles().values()) {
				AndroidClassInfo classInfo = bundle.get(name);
				if (classInfo != null)
					return new QueryResult<>(this, resource, bundle, classInfo);
			}
		}
		return new QueryResult<>(this, null, null, null);
	}

	/**
	 * @param name
	 * 		File name.
	 *
	 * @return Result of lookup.
	 */
	default QueryResult<FileInfo> findFile(String name) {
		List<WorkspaceResource> resources = new ArrayList<>(getSupportingResources());
		resources.add(0, getPrimaryResource());
		for (WorkspaceResource resource : resources) {
			FileBundle bundle = resource.getFileBundle();
			FileInfo fileInfo = bundle.get(name);
			if (fileInfo != null)
				return new QueryResult<>(this, resource, bundle, fileInfo);
			for (WorkspaceFileResource embedded : resource.getEmbeddedResources().values()) {
				FileInfo embeddedFileInfo = embedded.getFileInfo();
				if (embeddedFileInfo.getName().equals(name))
					return new QueryResult<>(this, resource, null, embeddedFileInfo);
			}
		}
		return new QueryResult<>(this, null, null, null);
	}

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
