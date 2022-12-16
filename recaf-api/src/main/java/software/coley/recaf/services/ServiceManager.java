package software.coley.recaf.services;

import java.util.Map;

/**
 * Manager to access available {@link Service} implementations.
 *
 * @author Matt Coley
 */
public interface ServiceManager {
	/**
	 * @return Map of available services. Key {@link Service#getServiceId()}.
	 */
	Map<String, Service> getAllServices();
}
