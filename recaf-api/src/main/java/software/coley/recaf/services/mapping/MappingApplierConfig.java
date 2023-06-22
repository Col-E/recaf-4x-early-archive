package software.coley.recaf.services.mapping;

import jakarta.inject.Inject;
import software.coley.recaf.config.BasicConfigContainer;
import software.coley.recaf.config.ConfigGroups;
import software.coley.recaf.services.ServiceConfig;

/**
 * Config for {@link MappingApplier}
 *
 * @author Matt Coley
 */
public class MappingApplierConfig extends BasicConfigContainer implements ServiceConfig {
	@Inject
	public MappingApplierConfig() {
		super(ConfigGroups.SERVICE_MAPPING, MappingApplier.SERVICE_ID + CONFIG_SUFFIX);
	}
}