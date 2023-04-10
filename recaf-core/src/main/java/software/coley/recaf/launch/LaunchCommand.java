package software.coley.recaf.launch;

import jakarta.annotation.Nullable;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import software.coley.recaf.Bootstrap;
import software.coley.recaf.RecafBuildConfig;
import software.coley.recaf.services.Service;

import java.io.File;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Launch arguments for Recaf.
 *
 * @author Matt Coley
 * @see LaunchArguments Bean accesible form availble to CDI components.
 */
@Command(name = "recaf", mixinStandardHelpOptions = true, version = RecafBuildConfig.VERSION,
		description = "Recaf: The modern Java reverse engineering tool.")
public class LaunchCommand implements Callable<Void> {
	@Option(names = {"-i", "--input"}, description = "Input to load into a workspace on startup.")
	private File input;
	@Option(names = {"-s", "--script"}, description = "Script to run on startup.")
	private File script;
	@Option(names = {"-h", "--headless"}, description = "Flag to skip over initializing the UI.")
	private boolean headless;
	@Option(names = {"-v", "--version"}, description = "Display the version information.")
	private boolean version;
	@Option(names = {"-l", "--listservices"}, description = "Display the version information.")
	private boolean listServices;

	@Override
	public Void call() throws Exception {
		if (version || listServices)
			System.out.println("======================= RECAF =======================");
		if (version) {
			System.out.printf("""
							VERSION:    %s
							GIT-COMMIT: %s
							GIT-TIME:   %s
							GIT-BRANCH: %s
							=====================================================
							""",
					RecafBuildConfig.VERSION,
					RecafBuildConfig.GIT_SHA,
					RecafBuildConfig.GIT_DATE,
					RecafBuildConfig.GIT_BRANCH
			);
		}
		if (listServices) {
			BeanManager beanManager = Bootstrap.get().getContainer().getBeanManager();
			Set<Bean<?>> beans = beanManager.getBeans(Service.class);
			for (Bean<?> bean : beans)
				System.out.println(" - " + bean.getBeanClass().getName());
			System.out.println("=====================================================");
		}
		return null;
	}

	/**
	 * @return Input to load into a workspace on startup.
	 */
	@Nullable
	public File getInput() {
		return input;
	}

	/**
	 * @return Script to run on startup.
	 */
	@Nullable
	public File getScript() {
		return script;
	}

	/**
	 * @return Flag to skip over initializing the UI.
	 */
	public boolean isHeadless() {
		return headless;
	}
}
