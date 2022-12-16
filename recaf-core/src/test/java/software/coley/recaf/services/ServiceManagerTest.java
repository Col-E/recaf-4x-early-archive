package software.coley.recaf.services;

import org.junit.jupiter.api.Test;
import software.coley.recaf.TestBase;
import software.coley.recaf.services.decompile.DecompilerManager;
import software.coley.recaf.services.inheritance.InheritanceGraph;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ServiceManager}
 */
class ServiceManagerTest extends TestBase {
	@Test
	void test() {
		ServiceManager serviceManager = recaf.getAndCreate(ServiceManager.class);
		assertNotNull(serviceManager, "Failed to get instance of service manager, which should be application-scoped");

		// Assert services exist
		Map<String, Service> allServices = serviceManager.getAllServices();
		assertFalse(allServices.isEmpty(), "No services found");
		assertTrue(allServices.containsKey(DecompilerManager.SERVICE_ID), "Missing decompile manager service");
		assertTrue(allServices.containsKey(InheritanceGraph.SERVICE_ID), "Missing inheritance graph service");
	}
}
