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
	 * @param resource
	 * 		Resource to add to {@link #getSupportingResources()}.
	 */
	void addSupportingResource(@Nonnull WorkspaceResource resource);

	/**
	 * @param resource
	 * 		Resource to remove from {@link #getSupportingResources()}.
	 *
	 * @return {@code true} when the resource was removed.
	 * {@code false} when it was not present.
	 */
	boolean removeSupportingResource(@Nonnull WorkspaceResource resource);

	/**
	 * @param includeInternal
	 * 		Flag to include internal supporting resources.
	 *
	 * @return List of all resources in the workspace. Includes primary, supporting, and internal support resources.
	 */
	@Nonnull
	default List<WorkspaceResource> getAllResources(boolean includeInternal) {
		List<WorkspaceResource> list = new ArrayList<>(getSupportingResources());
		if (includeInternal) list.addAll(getInternalSupportingResources());
		list.add(0, getPrimaryResource());
		return list;
	}

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
	default FindResult<? extends ClassInfo> findAnyClass(String name) {
		FindResult<? extends ClassInfo> result = findJvmClass(name);
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
	default FindResult<JvmClassInfo> findJvmClass(String name) {
		List<WorkspaceResource> resources = new ArrayList<>(getSupportingResources());
		resources.add(0, getPrimaryResource());
		resources.addAll(getInternalSupportingResources());
		for (WorkspaceResource resource : resources) {
			JvmClassBundle bundle = resource.getJvmClassBundle();
			JvmClassInfo classInfo = bundle.get(name);
			if (classInfo != null)
				return new FindResult<>(this, resource, bundle, classInfo);
		}
		return new FindResult<>(this, null, null, null);
	}

	/**
	 * @param name
	 * 		Class name.
	 *
	 * @return Result of lookup.
	 */
	default FindResult<JvmClassInfo> findVersionedJvmClass(String name) {
		List<WorkspaceResource> resources = new ArrayList<>(getSupportingResources());
		resources.add(0, getPrimaryResource());
		for (WorkspaceResource resource : resources) {
			for (JvmClassBundle bundle : resource.getVersionedJvmClassBundles().values()) {
				JvmClassInfo classInfo = bundle.get(name);
				if (classInfo != null)
					return new FindResult<>(this, resource, bundle, classInfo);
			}
		}
		return new FindResult<>(this, null, null, null);
	}

	/**
	 * @param name
	 * 		Class name.
	 *
	 * @return Result of lookup.
	 */
	default FindResult<AndroidClassInfo> findAndroidClass(String name) {
		List<WorkspaceResource> resources = new ArrayList<>(getSupportingResources());
		resources.add(0, getPrimaryResource());
		for (WorkspaceResource resource : resources) {
			for (AndroidClassBundle bundle : resource.getAndroidClassBundles().values()) {
				AndroidClassInfo classInfo = bundle.get(name);
				if (classInfo != null)
					return new FindResult<>(this, resource, bundle, classInfo);
			}
		}
		return new FindResult<>(this, null, null, null);
	}

	/**
	 * @param name
	 * 		File name.
	 *
	 * @return Result of lookup.
	 */
	default FindResult<FileInfo> findFile(String name) {
		List<WorkspaceResource> resources = new ArrayList<>(getSupportingResources());
		resources.add(0, getPrimaryResource());
		for (WorkspaceResource resource : resources) {
			FileBundle bundle = resource.getFileBundle();
			FileInfo fileInfo = bundle.get(name);
			if (fileInfo != null)
				return new FindResult<>(this, resource, bundle, fileInfo);
			for (WorkspaceFileResource embedded : resource.getEmbeddedResources().values()) {
				FileInfo embeddedFileInfo = embedded.getFileInfo();
				if (embeddedFileInfo.getName().equals(name))
					return new FindResult<>(this, resource, null, embeddedFileInfo);
			}
		}
		return new FindResult<>(this, null, null, null);
	}
}
