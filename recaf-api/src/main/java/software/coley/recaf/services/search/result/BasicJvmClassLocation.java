package software.coley.recaf.services.search.result;

import jakarta.annotation.Nonnull;
import software.coley.recaf.info.JarFileInfo;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.util.Map;

/**
 * Basic implementation of {@link JvmClassLocation}.
 *
 * @author Matt Coley
 */
public class BasicJvmClassLocation extends AbstractLocation implements JvmClassLocation {
	private final JvmClassBundle bundle;
	private final JvmClassInfo classInfo;

	/**
	 * @param workspace
	 * 		Target workspace.
	 * @param resource
	 * 		Target resource.
	 * @param bundle
	 * 		Target bundle.
	 * @param classInfo
	 * 		Target class containing the result.
	 */
	public BasicJvmClassLocation(@Nonnull Workspace workspace, @Nonnull WorkspaceResource resource,
								 @Nonnull JvmClassBundle bundle, @Nonnull JvmClassInfo classInfo) {
		super(workspace, resource);
		this.bundle = bundle;
		this.classInfo = classInfo;
	}

	@Override
	public JvmClassBundle getContainingBundle() {
		return bundle;
	}

	@Override
	public JvmClassInfo getClassInfo() {
		return classInfo;
	}

	@Nonnull
	@Override
	public String comparableString() {
		String name = classInfo.getName();

		// Check base case, plain path
		WorkspaceResource resource = getContainingResource();
		if (resource.getJvmClassBundle() == bundle)
			return name;

		// Check for versioned classes
		for (Map.Entry<Integer, JvmClassBundle> entry : resource.getVersionedJvmClassBundles().entrySet()) {
			if (bundle == entry.getValue())
				return JarFileInfo.MULTI_RELEASE_PREFIX + entry.getKey() + "/" + name;
		}

		// Unknown path, just use the class name
		return name;
	}
}
