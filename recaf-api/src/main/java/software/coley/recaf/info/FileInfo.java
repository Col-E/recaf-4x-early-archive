package software.coley.recaf.info;

import jakarta.annotation.Nonnull;
import software.coley.recaf.info.builder.FileInfoBuilder;

/**
 * Outline of a file.
 *
 * @author Matt Coley
 */
public interface FileInfo extends Info {
	/**
	 * @return Raw bytes of file content.
	 */
	byte[] getRawContent();

	/**
	 * @return New builder wrapping this file information.
	 */
	default FileInfoBuilder<?> toBuilder() {
		return FileInfoBuilder.forFile(this);
	}

	@Nonnull
	@Override
	default ClassInfo asClass() {
		throw new IllegalStateException("File cannot be cast to generic class");
	}

	@Nonnull
	@Override
	default FileInfo asFile() {
		return this;
	}

	/**
	 * @return Self cast to zip file.
	 */
	default ZipFileInfo asZipFile() {
		throw new IllegalStateException("Non-zip file cannot be cast to zip file");
	}

	@Override
	default boolean isClass() {
		return false;
	}

	@Override
	default boolean isFile() {
		return true;
	}

	/**
	 * @return {@code true} if self is a zip file.
	 */
	default boolean isZipFile() {
		return false;
	}
}
