package software.coley.recaf.services.script;

import software.coley.recaf.services.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Outline for script execution.
 *
 * @author Matt Coley
 */
public interface ScriptEngine extends Service {
	String SERVICE_ID = "script-engine";

	/**
	 * @param scriptSource
	 * 		Script source to execute.
	 *
	 * @return Future of script execution.
	 */
	CompletableFuture<ScriptResult> run(String scriptSource);
}
