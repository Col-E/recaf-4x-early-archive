package software.coley.recaf.util;

import org.slf4j.Logger;
import software.coley.recaf.analytics.logging.Logging;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Validates JFX is on the classpath.
 *
 * @author Matt Coley
 */
public class JFXValidation {
	private static final Logger logger = Logging.get(JFXValidation.class);

	/**
	 * Ensures that the JavaFX runtime is on the class path.
	 */
	public static int validateJFX() {
		try {
			Class<?> versionClass = Class.forName("com.sun.javafx.runtime.VersionInfo");
			Method setupSystemProperties = versionClass.getDeclaredMethod("setupSystemProperties");
			setupSystemProperties.setAccessible(true);
			setupSystemProperties.invoke(null);
			logger.info("JavaFX initialized: {}", System.getProperty("javafx.version"));
			return 0;
		} catch (ClassNotFoundException ex) {
			logger.error("JFX validation failed", ex);
			return 100;
		} catch (NoSuchMethodException ex) {
			logger.error("JFX validation failed", ex);
			return 101;
		} catch (InvocationTargetException ex) {
			logger.error("JFX validation failed", ex);
			return 102;
		} catch (IllegalAccessException ex) {
			logger.error("JFX validation failed", ex);
			return 103;
		} catch (Exception ex) {
			logger.error("JFX validation failed due to unhandled exception", ex);
			return 200;
		}
	}
}