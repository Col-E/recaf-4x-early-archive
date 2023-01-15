package software.coley.recaf.services.attach;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import software.coley.collections.observable.ObservableList;
import software.coley.recaf.services.Service;
import software.coley.recaf.workspace.model.resource.WorkspaceRemoteVmResource;

import java.util.Properties;

/**
 * Outline for attach service.
 *
 * @author Matt Coley
 */
public interface AttachManager extends Service {
	String SERVICE_ID = "attach";

	/**
	 * @return {@code true} when attach is supported.
	 * Typically only {@code false} when the agent fails to extract onto the local file system.
	 */
	boolean canAttach();

	/**
	 * Refresh available remote JVMs.
	 */
	void scan();

	/**
	 * Connect to the given VM.
	 *
	 * @param item
	 * 		VM descriptor for VM to connect to.
	 *
	 * @return Agent client resource, not yet connected.
	 */
	WorkspaceRemoteVmResource connect(VirtualMachineDescriptor item);

	/**
	 * @param descriptor
	 * 		Lookup descriptor.
	 *
	 * @return Remote VM.
	 */
	VirtualMachine getVirtualMachine(VirtualMachineDescriptor descriptor);

	/**
	 * @param descriptor
	 * 		Lookup descriptor.
	 *
	 * @return Exception when attempting to connect to remote VM.
	 */
	Exception getVirtualMachineConnectionFailure(VirtualMachineDescriptor descriptor);

	/**
	 * @param descriptor
	 * 		Lookup descriptor.
	 *
	 * @return Remote VM PID, or {@code -1} if no PID is known for the remote VM.
	 */
	int getVirtualMachinePid(VirtualMachineDescriptor descriptor);

	/**
	 * @param descriptor
	 * 		Lookup descriptor.
	 *
	 * @return Remote VM {@link System#getProperties()}.
	 */
	Properties getVirtualMachineProperties(VirtualMachineDescriptor descriptor);

	/**
	 * @param descriptor
	 * 		Lookup descriptor.
	 *
	 * @return Remote main class of VM.
	 */
	String getVirtualMachineMainClass(VirtualMachineDescriptor descriptor);

	/**
	 * @return Observable list of virtual machine descriptors.
	 */
	ObservableList<VirtualMachineDescriptor> getVirtualMachineDescriptors();
}
