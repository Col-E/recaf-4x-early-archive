package software.coley.recaf.info;

/**
 * Outline of a ZIP file container.
 *
 * @author Matt Coley
 */
public interface ZipFileInfo extends FileInfo {
	@Override
	default ZipFileInfo asZipFile() {
		return this;
	}

	@Override
	default boolean isZipFile() {
		return true;
	}
}
