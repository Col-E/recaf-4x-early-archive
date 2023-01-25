package software.coley.recaf.services.script;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jregex.Matcher;
import org.slf4j.Logger;
import software.coley.observables.ObservableCollection;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.services.Service;
import software.coley.recaf.util.RegexUtil;
import software.coley.recaf.util.StringUtil;
import software.coley.recaf.util.threading.ThreadPoolFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Manages local script files.
 *
 * @author Matt Coley
 * @see ScriptEngine Executor for scripts.
 * @see ScriptFile Local script file type.
 */
@ApplicationScoped
public class ScriptManager implements Service {
	public static final String SERVICE_ID = "script-manager";
	private static final String TAG_PATTERN = "//(\\s+)?@({key}\\S+)\\s+({value}.+)";
	private static final Logger logger = Logging.get(ScriptManager.class);
	private final ExecutorService watchPool = ThreadPoolFactory.newSingleThreadExecutor(SERVICE_ID);
	private final ObservableCollection<ScriptFile, List<ScriptFile>> scriptFiles = new ObservableCollection<>(ArrayList::new);
	private final ScriptManagerConfig config;

	@Inject
	public ScriptManager(ScriptManagerConfig config) {
		this.config = config;

		// Start watching files in scripts directory
		watchPool.submit(this::watch);
	}

	/**
	 * @param path
	 * 		Path to script file.
	 *
	 * @return Wrapper of script.
	 *
	 * @throws IOException
	 * 		When the script file cannot be read from.
	 */
	@Nonnull
	public ScriptFile read(@Nonnull Path path) throws IOException {
		String text = Files.readString(path);

		// Parse tags from beginning of file
		Map<String, String> tags = new HashMap<>();
		int metaEnd = text.indexOf("==/Metadata==");
		int lineMetaEnd = StringUtil.count("\n", text.substring(0, metaEnd));
		text.lines().limit(lineMetaEnd).forEach(line -> {
			if (line.startsWith("//")) {
				Matcher matcher = RegexUtil.getMatcher(TAG_PATTERN, line);
				if (matcher.matches()) {
					String key = matcher.group("key").toLowerCase();
					String value = matcher.group("value");
					tags.put(key, value);
				}
			}
		});

		return new ScriptFile(path, text, tags);
	}

	/**
	 * Watch for updates in the scripts directory.
	 */
	private void watch() {
		Path scriptsDirectory = config.getScriptsDirectory();
		try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
			WatchKey watchKey = scriptsDirectory.register(watchService,
					StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_MODIFY,
					StandardWatchEventKinds.ENTRY_DELETE);
			WatchKey key;
			while ((key = watchService.take()) != null) {
				for (WatchEvent<?> event : key.pollEvents()) {
					Path eventPath = (Path) event.context();
					if (Files.isRegularFile(eventPath)) {
						WatchEvent.Kind<?> kind = event.kind();
						try {
							if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
								onScriptCreate(eventPath);
							} else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
								onScriptUpdated(eventPath);
							} else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
								onScriptRemoved(eventPath);
							}
						} catch (Throwable t) {
							logger.error("Unhandled exception updating available scripts");
						}
					}
				}
				if (!key.reset())
					logger.warn("Key was unregistered: {}", key);
			}
			watchKey.cancel();
		} catch (IOException ex) {
			logger.error("IO exception when handling file watch on scripts directory", ex);
		} catch (InterruptedException ex) {
			logger.error("Fle watch on scripts directory was interrupted", ex);

		}
	}

	/**
	 * @param path
	 * 		Path of file newly created.
	 */
	private void onScriptCreate(@Nonnull Path path) {
		try {
			ScriptFile file = read(path);
			scriptFiles.add(file);
		} catch (IOException ex) {
			logger.error("Could not load script from path: {}", path, ex);
		}
	}

	/**
	 * @param path
	 * 		Path of file modified.
	 */
	private void onScriptUpdated(@Nonnull Path path) {
		try {
			ScriptFile updated = read(path);

			// Replace old file wrapper with new wrapper
			scriptFiles.removeIf(file -> path.equals(file.path()));
			scriptFiles.add(updated);
		} catch (IOException ex) {
			logger.error("Could not load script from path: {}", path, ex);
		}
	}

	/**
	 * @param path
	 * 		Path of file removed.
	 */
	private void onScriptRemoved(@Nonnull Path path) {
		scriptFiles.removeIf(file -> path.equals(file.path()));
	}

	/**
	 * @return Collection of local available script files.
	 */
	public ObservableCollection<ScriptFile, List<ScriptFile>> getScriptFiles() {
		return scriptFiles;
	}

	@Nonnull
	@Override
	public String getServiceId() {
		return SERVICE_ID;
	}

	@Nonnull
	@Override
	public ScriptManagerConfig getServiceConfig() {
		return config;
	}
}
