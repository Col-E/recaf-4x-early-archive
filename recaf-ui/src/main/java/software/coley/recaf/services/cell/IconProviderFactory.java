package software.coley.recaf.services.cell;

import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.FileInfo;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.workspace.model.bundle.AndroidClassBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

/**
 * Base icon provider factory.
 *
 * @author Matt Coley
 * @see ClassIconProviderFactory For {@link JvmClassInfo} and {@link AndroidClassBundle} entries.
 * @see DirectoryIconProviderFactory For directory entries, not linked to a specific {@link FileInfo}.
 * @see FileIconProviderFactory For {@link FileInfo} entries.
 * @see PackageIconProviderFactory  For package entries, not linked to a specific {@link ClassInfo}.
 * @see ResourceIconProviderFactory For {@link WorkspaceResource} entries.
 */
public interface IconProviderFactory {
	/**
	 * @return Icon provider that provides {@code null}.
	 */
	default IconProvider emptyProvider() {
		return () -> null;
	}
}
