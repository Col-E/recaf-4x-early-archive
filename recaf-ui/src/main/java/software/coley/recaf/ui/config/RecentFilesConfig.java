package software.coley.recaf.ui.config;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.coley.observables.ObservableCollection;
import software.coley.observables.ObservableInteger;
import software.coley.observables.ObservableObject;
import software.coley.observables.ObservableString;
import software.coley.recaf.config.BasicCollectionConfigValue;
import software.coley.recaf.config.BasicConfigContainer;
import software.coley.recaf.config.BasicConfigValue;
import software.coley.recaf.config.ConfigGroups;
import software.coley.recaf.util.StringUtil;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.resource.WorkspaceDirectoryResource;
import software.coley.recaf.workspace.model.resource.WorkspaceFileResource;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Config for tracking recent file interactions.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class RecentFilesConfig extends BasicConfigContainer {
	public static final String ID = "recent-workspaces";
	private final ObservableInteger maxRecentWorkspaces = new ObservableInteger(10);
	private final ObservableCollection<WorkspaceModel, List<WorkspaceModel>> recentWorkspaces = new ObservableCollection<>(ArrayList::new);
	private final ObservableString lastWorkspaceOpenDirectory = new ObservableString(System.getProperty("user.dir"));
	private final ObservableString lastWorkspaceExportDirectory = new ObservableString(System.getProperty("user.dir"));

	@Inject
	public RecentFilesConfig() {
		super(ConfigGroups.SERVICE_IO, ID + CONFIG_SUFFIX);
		// Add values
		addValue(new BasicConfigValue<>("max-recent-workspaces", Integer.class, maxRecentWorkspaces));
		addValue(new BasicCollectionConfigValue<>("recent-workspaces", List.class, WorkspaceModel.class, recentWorkspaces));
		addValue(new BasicConfigValue<>("last-workspace-open-path", String.class, lastWorkspaceOpenDirectory));
		addValue(new BasicConfigValue<>("last-workspace-export-path", String.class, lastWorkspaceExportDirectory));
	}

	/**
	 * @param workspace
	 * 		Workspace to add to {@link #getRecentWorkspaces()}.
	 */
	public void addWorkspace(@Nonnull Workspace workspace) {
		// Only allow serializable workspaces
		WorkspaceResource primaryResource = workspace.getPrimaryResource();
		if (!ResourceModel.isSupported(primaryResource))
			return;

		// Wrap to model
		ResourceModel primary = ResourceModel.from(primaryResource);
		List<ResourceModel> libraries = workspace.getSupportingResources().stream()
				.map(ResourceModel::from)
				.toList();
		WorkspaceModel workspaceModel = new WorkspaceModel(primary, libraries);

		// Update value
		if (recentWorkspaces.contains(workspaceModel)) {
			// Re-insert at 0'th position so that its at the "top" of the list.
			List<WorkspaceModel> updatedList = new ArrayList<>(recentWorkspaces.getValue());
			updatedList.remove(workspaceModel);
			updatedList.add(0, workspaceModel);
			recentWorkspaces.setValue(updatedList);
		} else {
			recentWorkspaces.add(workspaceModel);
		}
	}

	/**
	 * Refresh available workspaces, removing any items that cannot be loaded from the list.
	 *
	 * @see WorkspaceModel#canLoadWorkspace()
	 */
	public void refreshWorkspaces() {
		List<WorkspaceModel> current = recentWorkspaces.getValue();
		List<WorkspaceModel> loadable = current.stream()
				.filter(WorkspaceModel::canLoadWorkspace)
				.toList();
		if (current.size() != loadable.size())
			recentWorkspaces.setValue(loadable);
	}

	/**
	 * @return Number of recent items to track.
	 */
	public ObservableInteger getMaxRecentWorkspaces() {
		return maxRecentWorkspaces;
	}

	/**
	 * @return Recent workspaces.
	 */
	public ObservableObject<List<WorkspaceModel>> getRecentWorkspaces() {
		return recentWorkspaces;
	}

	/**
	 * @return Last path used to open a workspace with.
	 */
	public ObservableString getLastWorkspaceOpenDirectory() {
		return lastWorkspaceOpenDirectory;
	}

	/**
	 * @return Last path used to export a workspace to.
	 */
	public ObservableString getLastWorkspaceExportDirectory() {
		return lastWorkspaceExportDirectory;
	}

	/**
	 * Basic wrapper for workspaces.
	 *
	 * @author Matt Coley
	 * @see ResourceModel
	 */
	public static class WorkspaceModel {
		private final ResourceModel primary;
		private final List<ResourceModel> libraries;

		/**
		 * @param primary
		 * 		Primary resource of the workspace.
		 * @param libraries
		 * 		Workspace supporting libraries.
		 */
		public WorkspaceModel(ResourceModel primary, List<ResourceModel> libraries) {
			this.primary = primary;
			this.libraries = libraries;
		}

		/**
		 * @return Primary resource of the workspace.
		 */
		public ResourceModel getPrimary() {
			return primary;
		}

		/**
		 * @return Workspace supporting libraries.
		 */
		public List<ResourceModel> getLibraries() {
			return libraries;
		}

		/**
		 * @return {@code true} when the files still exist at their expected locations.
		 */
		public boolean canLoadWorkspace() {
			Path path = Paths.get(getPrimary().getPath());
			if (!Files.exists(path))
				return false;
			for (ResourceModel model : getLibraries()) {
				path = Paths.get(model.getPath());
				if (!Files.exists(path))
					return false;
			}
			return true;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			WorkspaceModel that = (WorkspaceModel) o;
			return primary.equals(that.primary) && libraries.equals(that.libraries);
		}

		@Override
		public int hashCode() {
			return Objects.hash(primary, libraries);
		}
	}

	/**
	 * Wrapper for a resources content source path.
	 *
	 * @author Matt Coley
	 */
	public static class ResourceModel {
		private final String path;

		/**
		 * @param path
		 * 		Path to the resource content source.
		 */
		public ResourceModel(String path) {
			this.path = path;
		}

		/**
		 * @param resource
		 * 		Some resource sourced from a file or directory.
		 *
		 * @return Representation of the content source.
		 */
		public static ResourceModel from(@Nonnull WorkspaceResource resource) {
			if (resource instanceof WorkspaceFileResource fileResource) {
				return new ResourceModel(fileResource.getFileInfo().getName());
			} else if (resource instanceof WorkspaceDirectoryResource fileResource) {
				return new ResourceModel(StringUtil.pathToAbsoluteString(fileResource.getDirectoryPath()));
			}
			throw new UnsupportedOperationException("Cannot serialize content source of type: " +
					resource.getClass().getName());
		}

		/**
		 * @param resource
		 * 		Some resource.
		 *
		 * @return {@code true} when it can be represented by this model.
		 */
		public static boolean isSupported(WorkspaceResource resource) {
			return resource instanceof WorkspaceFileResource || resource instanceof WorkspaceDirectoryResource;
		}

		/**
		 * @return Shortened path of resource's content source.
		 */
		public String getSimpleName() {
			String name = path;
			int slashIndex = name.lastIndexOf('/');
			if (slashIndex > 0)
				name = name.substring(slashIndex + 1);
			return name;
		}

		/**
		 * @return Path to the resource content source. Can be a file path, maven coordinates, or url.
		 */
		public String getPath() {
			return path;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ResourceModel that = (ResourceModel) o;
			return path.equals(that.path);
		}

		@Override
		public int hashCode() {
			return Objects.hash(path);
		}
	}
}
