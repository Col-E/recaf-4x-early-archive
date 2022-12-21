package software.coley.recaf.services.mapping;

import jakarta.inject.Inject;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import software.coley.recaf.cdi.WorkspaceScoped;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.services.inheritance.InheritanceGraph;
import software.coley.recaf.services.mapping.aggregate.AggregateMappingManager;
import software.coley.recaf.util.threading.ThreadPoolFactory;
import software.coley.recaf.util.threading.ThreadUtil;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

/**
 * Applies mappings to workspaces and workspace resources.
 *
 * @author Matt Coley
 */
@WorkspaceScoped
public class MappingApplier {
	private static final ExecutorService applierThreadPool = ThreadPoolFactory.newFixedThreadPool("mapping-applier");
	private final InheritanceGraph inheritanceGraph;
	private final AggregateMappingManager aggregateMappingManager;

	@Inject
	public MappingApplier(InheritanceGraph inheritanceGraph, AggregateMappingManager aggregateMappingManager) {
		this.inheritanceGraph = inheritanceGraph;
		this.aggregateMappingManager = aggregateMappingManager;
	}

	/**
	 * @param mappings
	 * 		The mappings to apply.
	 * @param workspace
	 * 		Workspace to apply mappings to <i>(Using primary resource)</i>.
	 *
	 * @return Names of the classes in the resource that had modifications as a result of the mapping operation.
	 */
	public Set<String> apply(Mappings mappings, Workspace workspace) {
		return apply(mappings, workspace.getPrimaryResource());
	}

	/**
	 * @param mappings
	 * 		The mappings to apply.
	 * @param resource
	 * 		Resource to apply mappings to.
	 *
	 * @return Names of the classes in the resource that had modifications as a result of the mapping operation.
	 */
	public Set<String> apply(Mappings mappings, WorkspaceResource resource) {
		// Check if mappings can be enriched with type look-ups
		if (inheritanceGraph != null && mappings instanceof MappingsAdapter) {
			// If we have "Dog extends Animal" and both define "jump" this lets "Dog.jump()" see "Animal.jump()"
			// allowing mappings that aren't complete for their type hierarchies to be filled in.
			MappingsAdapter adapter = (MappingsAdapter) mappings;
			adapter.enableHierarchyLookup(inheritanceGraph);
		}

		Set<String> modifiedClasses = applyMappingsWithoutAggregation(resource, mappings);
		if (aggregateMappingManager != null)
			aggregateMappingManager.updateAggregateMappings(mappings);
		return modifiedClasses;
	}

	/**
	 * @param resource
	 * 		Resource to apply mappings to.
	 * @param mappings
	 * 		The mappings to apply.
	 *
	 * @return Names of the classes in the resource that had modifications as a result of the mapping operation.
	 */
	private static Set<String> applyMappingsWithoutAggregation(WorkspaceResource resource, Mappings mappings) {
		ExecutorService service = ThreadUtil.phasingService(applierThreadPool);
		Set<String> modifiedClasses = ConcurrentHashMap.newKeySet();
		Set<String> newNames = new HashSet<>();
		Stream.concat(resource.jvmClassBundleStream(), resource.versionedJvmClassBundleStream()).forEach(bundle -> {
			bundle.forEach(classInfo -> {
				service.execute(() -> {
					String originalName = classInfo.getName();

					// Apply renamer
					ClassWriter cw = new ClassWriter(0);
					ClassReader cr = classInfo.getClassReader();
					RemappingVisitor remapVisitor = new RemappingVisitor(cw, mappings);
					cr.accept(remapVisitor, 0);

					// Update class if it has any modified references
					if (remapVisitor.hasMappingBeenApplied()) {
						modifiedClasses.add(originalName);
						JvmClassInfo updatedInfo = classInfo.toBuilder()
								.adaptFrom(new ClassReader(cw.toByteArray()))
								.build();
						String newName = updatedInfo.getName();
						synchronized (resource) {
							newNames.add(newName);
							bundle.put(updatedInfo);

							// Remove old classes if they have been renamed and do not occur
							// in a set of newly applied names
							if (!originalName.equals(newName) && !newNames.contains(originalName)) {
								bundle.remove(originalName);
							}
						}
					}
				});
			});
		});
		ThreadUtil.blockUntilComplete(service);
		return modifiedClasses;
	}
}
