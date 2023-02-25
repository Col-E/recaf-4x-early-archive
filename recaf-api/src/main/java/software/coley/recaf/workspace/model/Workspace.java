package software.coley.recaf.workspace.model;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.recaf.behavior.Closing;
import software.coley.recaf.info.AndroidClassInfo;
import software.coley.recaf.info.FileInfo;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.path.ClassPathNode;
import software.coley.recaf.path.FilePathNode;
import software.coley.recaf.path.WorkspacePathNode;
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
		List<WorkspaceResource> supportingResources = getSupportingResources();
		int supportingSize = supportingResources.size();
		if (includeInternal) {
			List<WorkspaceResource> internalSupportingResources = getInternalSupportingResources();
			List<WorkspaceResource> list = new ArrayList<>(1 + supportingSize + internalSupportingResources.size());
			list.add(getPrimaryResource());
			list.addAll(supportingResources);
			list.addAll(internalSupportingResources);
			return list;
		}
		List<WorkspaceResource> list = new ArrayList<>(1 + supportingSize);
		list.add(getPrimaryResource());
		list.addAll(supportingResources);
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
	@Nullable
	default ClassPathNode findAnyClass(@Nonnull String name) {
		ClassPathNode result = findJvmClass(name);
		if (result == null)
			result = findVersionedJvmClass(name);
		if (result == null)
			result = findAndroidClass(name);
		return result;
	}

	/**
	 * @param name
	 * 		Class name.
	 *
	 * @return Result of lookup.
	 */
	@Nullable
	default ClassPathNode findJvmClass(@Nonnull String name) {
		List<WorkspaceResource> resources = new ArrayList<>(getSupportingResources());
		resources.add(0, getPrimaryResource());
		resources.addAll(getInternalSupportingResources());
		for (WorkspaceResource resource : resources) {
			JvmClassBundle bundle = resource.getJvmClassBundle();
			JvmClassInfo classInfo = bundle.get(name);
			if (classInfo != null)
				return new WorkspacePathNode(this)
						.child(resource)
						.child(bundle)
						.child(classInfo.getPackageName())
						.child(classInfo);
		}
		return null;
	}

	/**
	 * @param name
	 * 		Class name.
	 *
	 * @return Result of lookup.
	 */
	@Nullable
	default ClassPathNode findVersionedJvmClass(@Nonnull String name) {
		List<WorkspaceResource> resources = new ArrayList<>(getSupportingResources());
		resources.add(0, getPrimaryResource());
		for (WorkspaceResource resource : resources) {
			for (JvmClassBundle bundle : resource.getVersionedJvmClassBundles().values()) {
				JvmClassInfo classInfo = bundle.get(name);
				if (classInfo != null)
					return new WorkspacePathNode(this)
							.child(resource)
							.child(bundle)
							.child(classInfo.getPackageName())
							.child(classInfo);
			}
		}
		return null;
	}

	/**
	 * @param name
	 * 		Class name.
	 *
	 * @return Result of lookup.
	 */
	@Nullable
	default ClassPathNode findAndroidClass(@Nonnull String name) {
		List<WorkspaceResource> resources = new ArrayList<>(getSupportingResources());
		resources.add(0, getPrimaryResource());
		for (WorkspaceResource resource : resources) {
			for (AndroidClassBundle bundle : resource.getAndroidClassBundles().values()) {
				AndroidClassInfo classInfo = bundle.get(name);
				if (classInfo != null)
					return new WorkspacePathNode(this)
							.child(resource)
							.child(bundle)
							.child(classInfo.getPackageName())
							.child(classInfo);
			}
		}
		return null;
	}

	/**
	 * @param name
	 * 		File name.
	 *
	 * @return Result of lookup.
	 */
	@Nullable
	default FilePathNode findFile(@Nonnull String name) {
		List<WorkspaceResource> resources = new ArrayList<>(getSupportingResources());
		resources.add(0, getPrimaryResource());
		for (WorkspaceResource resource : resources) {
			FileBundle bundle = resource.getFileBundle();
			FileInfo fileInfo = bundle.get(name);
			if (fileInfo != null)
				return new WorkspacePathNode(this)
						.child(resource)
						.child(bundle)
						.child(fileInfo.getDirectoryName())
						.child(fileInfo);
			for (WorkspaceFileResource embedded : resource.getEmbeddedResources().values()) {
				FileInfo embeddedFileInfo = embedded.getFileInfo();
				if (embeddedFileInfo.getName().equals(name))
					return new WorkspacePathNode(this)
							.child(resource)
							.child(bundle)
							.child(embeddedFileInfo.getDirectoryName())
							.child(embeddedFileInfo);
			}
		}
		return null;
	}
}
