package software.coley.recaf.services.compile;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.coley.recaf.config.BasicConfigContainer;
import software.coley.recaf.services.ServiceConfig;

/**
 * Config for {@link JavacCompiler}.
 * <br>
 * Not to be confused with {@link JavacArguments individual arguments} to be passed when invoking the compiler.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class JavacCompilerConfig extends BasicConfigContainer implements ServiceConfig {
	@Inject
	public JavacCompilerConfig() {
		super(JavacCompiler.SERVICE_ID + CONFIG_SUFFIX);
	}
}
