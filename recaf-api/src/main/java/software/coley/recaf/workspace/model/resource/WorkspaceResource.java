package software.coley.recaf.workspace.model.resource;

import jakarta.annotation.Nonnull;
import software.coley.recaf.behavior.Closing;
import software.coley.recaf.workspace.model.bundle.AndroidClassBundle;
import software.coley.recaf.workspace.model.bundle.FileBundle;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;
import software.coley.recaf.workspace.model.Workspace;

import java.util.Map;

/**
 * Component of a {@link Workspace}. Contain classes and files.
 *
 * @author Matt Coley
 */
public interface WorkspaceResource extends Closing {
	/**
	 * Contains the classes within the resource.
	 * <br>
	 * For JAR files, these are the standard classes you'd expect.
	 * If the JAR has version-specific classes, those will be in versioned bundles
	 * accessible via {@link #getJvmClassBundles()}.
	 * <br>
	 * For Android files (APK) this will always be empty.
	 * Android APK's classes reside in embedded dex files, which are accessible via {@link #getAndroidClassBundles()}.
	 *
	 * @return Immediate classes within the resource.
	 */
	@Nonnull
	JvmClassBundle getPrimaryClassBundle();

	/**
	 * @return Immediate files within the resource.
	 */
	@Nonnull
	FileBundle getPrimaryFileBundle();

	/**
	 * Contains additional class bundles.
	 * <br>
	 * For JAR files:
	 * <ul>
	 *     <li>If there is another JAR within the JAR containing classes,
	 *     the inner JAR becomes a bundle with its classes available here.</li>
	 *     <li>If the JAR contains versioned content, each version scope
	 *     becomes a bundle with its classes available here</li>
	 * </ul>
	 *
	 * @return Map of JVM class bundles.
	 */
	@Nonnull
	Map<String, JvmClassBundle> getJvmClassBundles();

	/**
	 * Contains Android class bundles.
	 * <br>
	 * Android bundles one or more DEX files into an APK. Each DEX becomes a bundle with its classes
	 * accessible here.
	 *
	 * @return Map of Android class bundles.
	 */
	@Nonnull
	Map<String, AndroidClassBundle> getAndroidClassBundles();

	// TODO: Specific Android resource bundle for 'resources.arsc'?

	/**
	 * Contains file bundles.
	 * <br>
	 * For JAR/ZIP files:
	 * <ul>
	 *      <li>If there is another JAR/ZIP within the JAR/ZIP containing files,
	 * 	     the inner JAR/ZIP becomes a bundle with its files available here.</li>
	 * </ul>
	 *
	 * @return Map of file bundles.
	 */
	@Nonnull
	Map<String, FileBundle> getFileBundles();

	/**
	 * @return {@code true} when this resource represents an internally managed resource within a {@link Workspace}.
	 * These resources are not explicitly created by users and thus should not be visible to them. However, they will
	 * supplement workspace capabilities as any other supporting resource.
	 */
	default boolean isInternal() {
		return false;
	}

	/**
	 * Called by containing {@link Workspace#close()}.
	 */
	@Override
	default void close() {
		// TODO: Clear listeners
	}
}
