package software.coley.recaf.services.search.result;

import jakarta.annotation.Nonnull;
import software.coley.recaf.info.annotation.AnnotationInfo;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

/**
 * Basic implementation of {@link AnnotationLocation}.
 *
 * @author Matt Coley
 */
public class BasicAnnotationLocation extends AbstractLocation implements AnnotationLocation {
	private final Location parent;
	private final AnnotationInfo annotationInfo;

	/**
	 * @param workspace
	 * 		Target workspace.
	 * @param resource
	 * 		Target resource.
	 * @param parent
	 * 		Parent location.
	 * @param annotationInfo
	 * 		Target annotation containing the result.
	 */
	public BasicAnnotationLocation(@Nonnull Workspace workspace, @Nonnull WorkspaceResource resource,
								   @Nonnull Location parent, @Nonnull AnnotationInfo annotationInfo) {
		super(workspace, resource);
		this.parent = parent;
		this.annotationInfo = annotationInfo;
	}

	@Nonnull
	@Override
	public AnnotationInfo getDeclaredAnnotation() {
		return annotationInfo;
	}

	@Nonnull
	@Override
	public Location getParent() {
		return parent;
	}

	@Nonnull
	@Override
	public String comparableString() {
		return parent.comparableString() + " @" + annotationInfo.getDescriptor();
	}
}
