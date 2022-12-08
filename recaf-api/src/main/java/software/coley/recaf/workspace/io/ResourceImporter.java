package software.coley.recaf.workspace.io;

import com.sun.tools.attach.VirtualMachineDescriptor;
import software.coley.recaf.workspace.model.resource.WorkspaceFileResource;
import software.coley.recaf.workspace.model.resource.WorkspaceRemoteVmResource;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;
import software.coley.recaf.workspace.model.resource.WorkspaceUriResource;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

/**
 * Service outline for supporting creation of {@link WorkspaceResource} instances.
 *
 * @author Matt Coley
 */
public interface ResourceImporter {
	/**
	 * @param file File to import from.
	 * @return Workspace resource representing the file.
	 */
	default WorkspaceFileResource importResource(File file) {
		return importResource(file);
	}
	/**
	 * @param path File path to import from.
	 * @return Workspace resource representing the file.
	 */
	WorkspaceFileResource importResource(Path path);

	/**
	 * @param url URL to content to import from.
	 * @return Workspace resource representing the remote content.
	 * @throws URISyntaxException When the URL cannot be converted to a URI.
	 */
	default WorkspaceUriResource importResource(URL url) throws URISyntaxException {
		return importResource(url.toURI());
	}

	/**
	 * @param uri URI to content to import from.
	 * @return Workspace resource representing the remote content.
	 */
	WorkspaceUriResource importResource(URI uri);

	/**
	 * @param virtualMachineDescriptor Descriptor of the remote JVM to attach to.
	 * @return Workspace resource representing the remote JVM.
	 */
	WorkspaceRemoteVmResource importResource(VirtualMachineDescriptor virtualMachineDescriptor);
}
