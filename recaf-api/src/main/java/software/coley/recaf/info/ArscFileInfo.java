package software.coley.recaf.info;

/**
 * Outline of a ARSC file, used by Android APK's.
 *
 * @author Matt Coley
 */
public interface ArscFileInfo extends AndroidChunkFileInfo {
	/**
	 * Standard name of ARSC resource file in APK files.
	 */
	String NAME = "resources.arsc";
}
