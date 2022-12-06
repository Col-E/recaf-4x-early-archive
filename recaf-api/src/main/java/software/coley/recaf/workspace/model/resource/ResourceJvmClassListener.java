package software.coley.recaf.workspace.model.resource;

import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;

/**
 * Listener for handling updates to {@link JvmClassInfo} values within a {@link JvmClassBundle}
 * contained in a {@link WorkspaceResource}.
 *
 * @author Matt Coley
 */
public interface ResourceJvmClassListener {
	/**
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param cls
	 * 		The new class.
	 */
	void onNewClass(WorkspaceResource resource, JvmClassBundle bundle, JvmClassInfo cls);

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
	void onUpdateClass(WorkspaceResource resource, JvmClassBundle bundle, JvmClassInfo oldCls, JvmClassInfo newCls);

	/**
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param cls
	 * 		The removed class.
	 */
	void onRemoveClass(WorkspaceResource resource, JvmClassBundle bundle, JvmClassInfo cls);
}
