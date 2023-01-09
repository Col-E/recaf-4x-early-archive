package software.coley.recaf;

import org.slf4j.Logger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.plugin.Plugin;
import software.coley.recaf.plugin.PluginContainer;
import software.coley.recaf.services.file.RecafDirectoriesConfig;
import software.coley.recaf.services.plugin.PluginManager;
import software.coley.recaf.util.JFXValidation;
import software.coley.recaf.util.Lang;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Application entry-point for Recaf's UI.
 *
 * @author Matt Coley
 */
public class Main {
	private static final Logger logger = Logging.get(Main.class);
	private static String[] launchArgs;
	private static Recaf recaf;

	/**
	 * @param args
	 * 		Application arguments.
	 */
	public static void main(String[] args) {
		launchArgs = args;

		// Validate the JFX environment is available.
		// Abort if not available.
		int validationCode = JFXValidation.validateJFX();
		if (validationCode != 0) {
			System.exit(validationCode);
			return;
		}

		// Add a class reference for our UI module.
		Bootstrap.setWeldConsumer(weld -> weld.addPackage(true, Main.class));

		// Invoke the bootstrapper, initializing the UI once the container is built.
		recaf = Bootstrap.get();
		initialize();
	}

	/**
	 * Initialize the UI application.
	 */
	private static void initialize() {
		initLogging();
		initTranslations();
		initPlugins();
		RecafApplication.launch(RecafApplication.class, launchArgs);
	}

	/**
	 * Configure file logging appender and compress old logs.
	 */
	private static void initLogging() {
		RecafDirectoriesConfig directories = recaf.get(RecafDirectoriesConfig.class);

		// Setup appender
		String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		Path logFile = directories.getBaseDirectory().resolve("log-" + date + ".txt");
		Logging.addFileAppender(logFile);

		// Archive old logs
		try {
			Files.createDirectories(directories.getLogsDirectory());
			List<Path> oldLogs = Files.list(directories.getBaseDirectory())
					.filter(p -> p.getFileName().toString().matches("log-\\d+-\\d+-\\d+\\.txt"))
					.collect(Collectors.toList());

			// Do not treat the current log file as an old log file
			oldLogs.remove(logFile);

			// Handling old entries
			logger.trace("Compressing {} old log files", oldLogs.size());
			for (Path oldLog : oldLogs) {
				String originalFileName = oldLog.getFileName().toString();
				String archiveFileName = originalFileName.replace(".txt", ".zip");
				Path archivedLog = directories.getLogsDirectory().resolve(archiveFileName);

				// Compress the log into a zip
				try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(archivedLog.toFile()))) {
					zos.putNextEntry(new ZipEntry(originalFileName));
					Files.copy(oldLog, zos);
					zos.closeEntry();
				}

				// Remove the old file
				Files.delete(oldLog);
			}
		} catch (IOException ex) {
			logger.warn("Failed to compress old logs", ex);
		}
	}

	/**
	 * Load translations.
	 */
	private static void initTranslations() {
		Lang.initialize();
	}

	/**
	 * Load plugins.
	 */
	private static void initPlugins() {
		// Plugin loading is handled in the implementation's @PostConstruct handler
		PluginManager pluginManager = recaf.get(PluginManager.class);

		// Log the discovered plugins
		Collection<PluginContainer<? extends Plugin>> plugins = pluginManager.getPlugins();
		if (plugins.isEmpty()) {
			logger.info("Initialization: No plugins found");
		} else {
			String split = "\n - ";
			logger.info("Initialization: {} plugins found:" + split + "{}",
					plugins.size(),
					plugins.stream().map(PluginContainer::getInformation)
							.map(info -> info.getName() + " - " + info.getVersion())
							.collect(Collectors.joining(split)));
		}
	}
}
