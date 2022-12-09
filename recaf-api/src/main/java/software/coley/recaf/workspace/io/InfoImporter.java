package software.coley.recaf.workspace.io;

import jakarta.annotation.Nonnull;
import software.coley.recaf.info.Info;
import software.coley.recaf.util.io.ByteSource;

/**
 * Service outline for creating various {@link Info} types from a basic name, {@link ByteSource} pair.
 *
 * @author Matt Coley
 */
public interface InfoImporter {
	/**
	 * @param name
	 * 		Name to pass for {@link Info#getName()} if it cannot be inferred from the content source.
	 * @param source
	 * 		Source of content to read data from.
	 *
	 * @return Info instance.
	 */
	@Nonnull
	Info readInfo(String name, ByteSource source);
}
