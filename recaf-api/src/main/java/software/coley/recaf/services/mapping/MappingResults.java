package software.coley.recaf.services.mapping;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.path.BundlePathNode;
import software.coley.recaf.path.ClassPathNode;
import software.coley.recaf.path.WorkspacePathNode;
import software.coley.recaf.services.mapping.aggregate.AggregateMappingManager;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.Bundle;
import software.coley.recaf.workspace.model.bundle.ClassBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.util.HashMap;
import java.util.Map;

/**
 * Result wrapper for {@link MappingApplier} operations.
 * Can serve as a preview for mapping operations before updating the affected {@link Workspace}.
 * <br>
 * Use {@link #apply()} to apply the mappings to the {@link WorkspaceResource} targeted in the mapping operation.
 *
 * @author Matt Coley
 */
public class MappingResults {
	private final Map<String, String> mappedClasses = new HashMap<>();
	private final Map<String, ClassPathNode> preMappingPaths = new HashMap<>();
	private final Map<String, ClassPathNode> postMappingPaths = new HashMap<>();
	private final Mappings mappings;
	private AggregateMappingManager aggregateMappingManager;

	/**
	 * @param mappings
	 * 		The mappings implementation used in the operation.
	 */
	public MappingResults(@Nonnull Mappings mappings) {
		this.mappings = mappings;
	}

	/**
	 * @param aggregateMappingManager
	 * 		Aggregate mapping manager to track mapping applications in.
	 *
	 * @return Self.
	 */
	@Nonnull
	public MappingResults withAggregateManager(@Nonnull AggregateMappingManager aggregateMappingManager) {
		this.aggregateMappingManager = aggregateMappingManager;
		return this;
	}

	/**
	 * @param workspace
	 * 		Workspace containing the class.
	 * @param resource
	 * 		Resource containing the class.
	 * @param bundle
	 * 		Bundle containing the class.
	 * @param preMapping
	 * 		The pre-mapped class.
	 * @param postMapping
	 * 		The post-mapped class.
	 */
	public void add(@Nonnull Workspace workspace,
					@Nonnull WorkspaceResource resource,
					@Nonnull ClassBundle<?> bundle,
					@Nonnull ClassInfo preMapping,
					@Nonnull ClassInfo postMapping) {
		String preMappingName = preMapping.getName();
		String postMappingName = postMapping.getName();
		BundlePathNode bundlePath = new WorkspacePathNode(workspace).child(resource).child(bundle);
		ClassPathNode preMappingPath = bundlePath.child(preMapping.getPackageName()).child(preMapping);
		ClassPathNode postMappingPath = bundlePath.child(postMapping.getPackageName()).child(postMapping);
		synchronized (mappedClasses) {
			mappedClasses.put(preMappingName, postMappingName);
		}
		synchronized (preMappingPaths) {
			preMappingPaths.put(preMappingName, preMappingPath);
		}
		synchronized (postMappingPaths) {
			postMappingPaths.put(postMappingName, postMappingPath);
		}
	}

	/**
	 * Applies the mappings to the {@link Workspace} / {@link WorkspaceResource} from {@link MappingApplier}.
	 */
	@SuppressWarnings("unchecked")
	public void apply() {
		for (Map.Entry<String, String> entry : mappedClasses.entrySet()) {
			String preMappedName = entry.getKey();
			String postMappedName = entry.getValue();
			ClassPathNode preMappedPath = preMappingPaths.get(preMappedName);
			ClassPathNode postMappedPath = postMappingPaths.get(postMappedName);
			if (preMappedPath != null && postMappedPath != null) {
				ClassBundle<ClassInfo> bundle = (ClassBundle<ClassInfo>) postMappedPath.getValueOfType(Bundle.class);
				if (bundle == null)
					throw new IllegalStateException("Cannot apply mapping for '" + preMappedName + "', path missing bundle");

				// Put mapped class into bundle
				ClassInfo postMappedClass = postMappedPath.getValue();
				bundle.put(postMappedClass);

				// Remove old classes if they have been renamed and do not occur
				// in a set of newly applied names
				if (!preMappedName.equals(postMappedName))
					bundle.remove(preMappedName);
			}
		}

		// Track changes in aggregate manager, if given.
		if (aggregateMappingManager != null)
			aggregateMappingManager.updateAggregateMappings(mappings);
	}

	/**
	 * @return The mappings implementation used in the operation.
	 */
	@Nonnull
	public Mappings getMappings() {
		return mappings;
	}

	/**
	 * @param preMappedName
	 * 		Pre-mapping name.
	 *
	 * @return {@code true} when the class was affected by the mapping operation.
	 */
	public boolean wasMapped(@Nonnull String preMappedName) {
		return mappedClasses.containsKey(preMappedName);
	}

	/**
	 * @param preMappingName
	 * 		Pre-mapping name.
	 *
	 * @return Post-mapped class info.
	 * May be {@code null} if no the given pre-mapped name was not affected by the mapping operation.
	 */
	@Nullable
	public ClassInfo getPostMappingClass(@Nonnull String preMappingName) {
		ClassPathNode postMappingPath = getPostMappingPath(preMappingName);
		if (postMappingPath == null) return null;
		return postMappingPath.getValue();
	}

	/**
	 * @param preMappingName
	 * 		Pre-mapping name.
	 *
	 * @return Path node of post-mapped class.
	 * May be {@code null} if no the given pre-mapped name was not affected by the mapping operation.
	 */
	@Nullable
	public ClassPathNode getPostMappingPath(@Nonnull String preMappingName) {
		String postMappingName = mappedClasses.get(preMappingName);
		if (postMappingName == null) return null;
		return postMappingPaths.get(postMappingName);
	}

	/**
	 * @return Mapping of affected classes, to their new names.
	 * If a class was affected, but the name not changed, the key and value for that entry will be the same.
	 */
	@Nonnull
	public Map<String, String> getMappedClasses() {
		return mappedClasses;
	}

	/**
	 * @return Mapping of pre-mapped names to their path nodes.
	 */
	@Nonnull
	public Map<String, ClassPathNode> getPreMappingPaths() {
		return preMappingPaths;
	}

	/**
	 * @return Mapping of post-mapped names to their path nodes.
	 */
	@Nonnull
	public Map<String, ClassPathNode> getPostMappingPaths() {
		return postMappingPaths;
	}
}
