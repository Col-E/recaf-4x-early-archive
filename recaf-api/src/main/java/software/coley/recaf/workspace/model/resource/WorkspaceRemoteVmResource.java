package software.coley.recaf.workspace.model.resource;

import com.sun.tools.attach.VirtualMachine;
import jakarta.annotation.Nonnull;

/**
 * A resource sourced from a remote {@link VirtualMachine}.
 *
 * @author Matt Coley
 */
public interface WorkspaceRemoteVmResource extends WorkspaceResource {
	/**
	 * @return Virtual machine of the remote process attached to.
	 */
	@Nonnull
	VirtualMachine getVirtualMachine();
}
