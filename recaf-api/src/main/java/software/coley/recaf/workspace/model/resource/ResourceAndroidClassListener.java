package software.coley.recaf.workspace.model.resource;

import software.coley.recaf.info.AndroidClassInfo;
import software.coley.recaf.workspace.model.bundle.AndroidClassBundle;

/**
 * Listener for handling updates to {@link AndroidClassInfo} values within a {@link AndroidClassBundle}
 * contained in a {@link WorkspaceResource}.
 *
 * @author Matt Coley
 */
public interface ResourceAndroidClassListener {
	/**
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param cls
	 * 		The new class.
	 */
	void onNewClass(WorkspaceResource resource, AndroidClassBundle bundle, AndroidClassInfo cls);

	/**
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param oldCls
	 * 		The old class value.
	 * @param newCls
	 * 		The new class value.
	 */
	void onUpdateClass(WorkspaceResource resource, AndroidClassBundle bundle, AndroidClassInfo oldCls, AndroidClassInfo newCls);

	/**
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param cls
	 * 		The removed class.
	 */
	void onRemoveClass(WorkspaceResource resource, AndroidClassBundle bundle, AndroidClassInfo cls);
}
