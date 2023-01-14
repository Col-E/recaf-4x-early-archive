package software.coley.recaf.services.window;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.coley.recaf.config.BasicConfigContainer;
import software.coley.recaf.services.ServiceConfig;

/**
 * Config for {@link WindowManager}.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class WindowManagerConfig extends BasicConfigContainer implements ServiceConfig {
	@Inject
	public WindowManagerConfig() {
		super(WindowManager.SERVICE_ID + CONFIG_SUFFIX);
	}
}
