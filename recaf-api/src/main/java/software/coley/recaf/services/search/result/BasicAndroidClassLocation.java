package software.coley.recaf.services.search.result;

import jakarta.annotation.Nonnull;
import software.coley.recaf.info.AndroidClassInfo;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.AndroidClassBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.util.Map;

/**
 * Basic implementation of {@link AndroidClassLocation}.
 *
 * @author Matt Coley
 */
public class BasicAndroidClassLocation extends AbstractLocation implements AndroidClassLocation {
	private final AndroidClassBundle bundle;
	private final AndroidClassInfo classInfo;

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
	public BasicAndroidClassLocation(@Nonnull Workspace workspace, @Nonnull WorkspaceResource resource,
									 @Nonnull AndroidClassBundle bundle, @Nonnull AndroidClassInfo classInfo) {
		super(workspace, resource);
		this.bundle = bundle;
		this.classInfo = classInfo;
	}

	@Override
	public AndroidClassBundle getContainingBundle() {
		return bundle;
	}

	@Override
	public AndroidClassInfo getClassInfo() {
		return classInfo;
	}

	@Nonnull
	@Override
	public String comparableString() {
		WorkspaceResource resource = getContainingResource();

		// Get dex bundle name to prefix on class name
		for (Map.Entry<String, AndroidClassBundle> entry : resource.getAndroidClassBundles().entrySet()) {
			if (entry.getValue() == bundle)
				return entry.getKey() + ":" + classInfo.getName();
		}

		// Couldn't figure out containing bundle context
		return "unknown.dex:" + classInfo.getName();
	}
}
