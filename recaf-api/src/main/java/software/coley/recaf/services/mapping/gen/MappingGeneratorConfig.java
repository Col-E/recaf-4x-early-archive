package software.coley.recaf.services.mapping.gen;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.coley.recaf.config.BasicConfigContainer;
import software.coley.recaf.services.ServiceConfig;

/**
 * Config for {@link MappingGenerator}.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class MappingGeneratorConfig extends BasicConfigContainer implements ServiceConfig {
	@Inject
	public MappingGeneratorConfig() {
		super(MappingGenerator.SERVICE_ID + CONFIG_SUFFIX);
	}
}
