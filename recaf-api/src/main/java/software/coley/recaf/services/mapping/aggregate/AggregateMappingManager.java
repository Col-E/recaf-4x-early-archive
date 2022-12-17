package software.coley.recaf.services.mapping.aggregate;

import jakarta.inject.Inject;
import software.coley.recaf.cdi.AutoRegisterWorkspaceListeners;
import software.coley.recaf.cdi.WorkspaceScoped;
import software.coley.recaf.services.Service;
import software.coley.recaf.services.mapping.Mappings;
import software.coley.recaf.workspace.WorkspaceCloseListener;
import software.coley.recaf.workspace.model.Workspace;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages tracking the state of mappings over time.
 *
 * @author Matt Coley
 * @author Marius Renner
 */
@WorkspaceScoped
@AutoRegisterWorkspaceListeners
public class AggregateMappingManager implements Service, WorkspaceCloseListener {
	public static final String SERVICE_ID = "mapping-aggregator";
	private final List<AggregatedMappingsListener> aggregateListeners = new ArrayList<>();
	private final AggregatedMappings aggregatedMappings = new AggregatedMappings();
	private final AggregateMappingManagerConfig config;

	@Inject
	public AggregateMappingManager(AggregateMappingManagerConfig config) {
		this.config = config;
	}

	@Override
	public void onWorkspaceClosed(Workspace workspace) {
		aggregateListeners.clear();
		clearAggregated();
	}

	/**
	 * Update the aggregate mappings for the workspace.
	 *
	 * @param newMappings
	 * 		The additional mappings that were added.
	 */
	public void updateAggregateMappings(Mappings newMappings) {
		aggregatedMappings.update(newMappings);
		aggregateListeners.forEach(listener -> listener.onAggregatedMappingsUpdated(getAggregatedMappings()));
	}

	/**
	 * Clears all mapping information.
	 */
	private void clearAggregated() {
		aggregatedMappings.clear();
		aggregateListeners.forEach(listener -> listener.onAggregatedMappingsUpdated(getAggregatedMappings()));
	}

	/**
	 * @param listener
	 * 		Listener to add.
	 */
	public void addAggregatedMappingsListener(AggregatedMappingsListener listener) {
		aggregateListeners.add(listener);
	}

	/**
	 * @param listener
	 * 		Listener to remove.
	 *
	 * @return {@code true} when the listener was removed.
	 * {@code false} if the listener was not in the list.
	 */
	public boolean removeAggregatedMappingListener(AggregatedMappingsListener listener) {
		return aggregateListeners.remove(listener);
	}

	/**
	 * @return Current aggregated mappings in the ASM format.
	 */
	public AggregatedMappings getAggregatedMappings() {
		return aggregatedMappings;
	}

	@Override
	public String getServiceId() {
		return SERVICE_ID;
	}

	@Override
	public AggregateMappingManagerConfig getServiceConfig() {
		return config;
	}
}
