package software.coley.recaf.services.cell.builtin;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import software.coley.recaf.services.cell.IconProvider;
import software.coley.recaf.services.cell.PackageIconProviderFactory;
import software.coley.recaf.services.cell.ResourceIconProviderFactory;
import software.coley.recaf.util.Icons;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

/**
 * Basic implementation for {@link PackageIconProviderFactory}.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class BasicResourceIconProviderFactory implements ResourceIconProviderFactory {
	private static final IconProvider PROVIDER = Icons.createProvider(Icons.FILE_JAR);

	@Nonnull
	@Override
	public IconProvider getResourceIconProvider(@Nonnull Workspace workspace,
												@Nonnull WorkspaceResource resource) {
		return PROVIDER;
	}
}
