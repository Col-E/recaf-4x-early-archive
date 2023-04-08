package software.coley.recaf.services.mapping;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import software.coley.recaf.cdi.WorkspaceScoped;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.info.properties.builtin.OriginalClassNameProperty;
import software.coley.recaf.services.inheritance.InheritanceGraph;
import software.coley.recaf.services.mapping.aggregate.AggregateMappingManager;
import software.coley.recaf.util.threading.ThreadPoolFactory;
import software.coley.recaf.util.threading.ThreadUtil;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

/**
 * Applies mappings to workspaces and workspace resources, wrapping the results in a {@link MappingResults}.
 * To update the workspace with the mapping results, use {@link MappingResults#apply()}.
 *
 * @author Matt Coley
 * @see MappingResults
 */
@WorkspaceScoped
public class MappingApplier {
	private static final ExecutorService applierThreadPool = ThreadPoolFactory.newFixedThreadPool("mapping-applier");
	private final InheritanceGraph inheritanceGraph;
	private final AggregateMappingManager aggregateMappingManager;
	private final Workspace workspace;

	@Inject
	public MappingApplier(@Nonnull InheritanceGraph inheritanceGraph,
						  @Nonnull AggregateMappingManager aggregateMappingManager,
						  @Nonnull Workspace workspace) {
		this.inheritanceGraph = inheritanceGraph;
		this.aggregateMappingManager = aggregateMappingManager;
		this.workspace = workspace;
	}

	/**
	 * Applies the mapping operation to the given classes.
	 *
	 * @param mappings
	 * 		The mappings to apply.
	 * @param resource
	 * 		Resource containing the classes.
	 * @param bundle
	 * 		Bundle containing the classes.
	 * @param classes
	 * 		Classes to apply mappings to.
	 *
	 * @return Result wrapper detailing affected classes from the mapping operation.
	 */
	@Nonnull
	public MappingResults applyToClasses(@Nonnull Mappings mappings,
										 @Nonnull WorkspaceResource resource,
										 @Nonnull JvmClassBundle bundle,
										 @Nonnull List<JvmClassInfo> classes) {
		enrich(mappings);
		MappingResults results = new MappingResults(mappings);

		// Apply mappings in the thread pool
		ExecutorService service = ThreadUtil.phasingService(applierThreadPool);
		for (JvmClassInfo classInfo : classes)
			service.execute(() -> applyMapping(results, workspace, resource, bundle, classInfo, mappings));
		ThreadUtil.blockUntilComplete(service);
		return results;
	}

	/**
	 * Applies the mapping operation to the current workspace's primary resource.
	 *
	 * @param mappings
	 * 		The mappings to apply.
	 *
	 * @return Result wrapper detailing affected classes from the mapping operation.
	 */
	@Nonnull
	public MappingResults applyToPrimary(@Nonnull Mappings mappings) {
		enrich(mappings);
		WorkspaceResource resource = workspace.getPrimaryResource();
		return applyMappingsWithoutAggregation(workspace, resource, mappings)
				.withAggregateManager(aggregateMappingManager);
	}

	private void enrich(Mappings mappings) {
		// Check if mappings can be enriched with type look-ups
		if (mappings instanceof MappingsAdapter adapter) {
			// If we have "Dog extends Animal" and both define "jump" this lets "Dog.jump()" see "Animal.jump()"
			// allowing mappings that aren't complete for their type hierarchies to be filled in.
			adapter.enableHierarchyLookup(inheritanceGraph);
		}
	}

	/**
	 * @param workspace
	 * 		Workspace to pull class info from when additional context is needed.
	 * @param resource
	 * 		Resource to apply mappings to.
	 * @param mappings
	 * 		The mappings to apply.
	 *
	 * @return Result wrapper detailing affected classes from the mapping operation.
	 */
	@Nonnull
	public static MappingResults applyMappingsWithoutAggregation(@Nonnull Workspace workspace,
																 @Nonnull WorkspaceResource resource,
																 @Nonnull Mappings mappings) {
		MappingResults results = new MappingResults(mappings);

		// Apply mappings in the thread pool
		ExecutorService service = ThreadUtil.phasingService(applierThreadPool);
		Stream.concat(resource.jvmClassBundleStream(), resource.versionedJvmClassBundleStream()).forEach(bundle -> {
			bundle.forEach(classInfo -> {
				service.execute(() -> applyMapping(results, workspace, resource, bundle, classInfo, mappings));
			});
		});
		ThreadUtil.blockUntilComplete(service);
		return results;
	}

	/**
	 * Applies mappings locally to the given
	 *
	 * @param results
	 * 		Results collection to insert into.
	 * @param workspace
	 * 		Containing workspace.
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param classInfo
	 * 		The class to apply mappings to.
	 * @param mappings
	 * 		The mappings to apply.
	 */
	public static void applyMapping(@Nonnull MappingResults results,
									@Nonnull Workspace workspace,
									@Nonnull WorkspaceResource resource,
									@Nonnull JvmClassBundle bundle,
									@Nonnull JvmClassInfo classInfo,
									@Nonnull Mappings mappings) {
		String originalName = classInfo.getName();

		// Apply renamer
		ClassWriter cw = new ClassWriter(0);
		ClassReader cr = classInfo.getClassReader();
		WorkspaceClassRemapper remapVisitor = new WorkspaceClassRemapper(cw, workspace, mappings);
		cr.accept(remapVisitor, 0);

		// Update class if it has any modified references
		if (remapVisitor.hasMappingBeenApplied()) {
			JvmClassInfo updatedInfo = classInfo.toBuilder()
					.adaptFrom(new ClassReader(cw.toByteArray()))
					.build();
			updatedInfo.setPropertyIfMissing(OriginalClassNameProperty.KEY,
					() -> new OriginalClassNameProperty(originalName));
			results.add(workspace, resource, bundle, classInfo, updatedInfo);
		}
	}
}
