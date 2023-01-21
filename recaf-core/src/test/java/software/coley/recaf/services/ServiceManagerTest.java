package software.coley.recaf.services;

import org.junit.jupiter.api.Test;
import software.coley.recaf.TestBase;
import software.coley.recaf.services.config.ConfigManager;
import software.coley.recaf.services.plugin.PluginManager;
import software.coley.recaf.services.attach.AttachManager;
import software.coley.recaf.services.callgraph.CallGraph;
import software.coley.recaf.services.compile.JavacCompiler;
import software.coley.recaf.services.decompile.DecompilerManager;
import software.coley.recaf.services.inheritance.InheritanceGraph;
import software.coley.recaf.services.mapping.aggregate.AggregateMappingManager;
import software.coley.recaf.services.mapping.format.MappingFormatManager;
import software.coley.recaf.services.mapping.gen.MappingGenerator;
import software.coley.recaf.services.search.SearchService;
import software.coley.recaf.workspace.io.ResourceImporter;

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
		assertTrue(allServices.containsKey(ResourceImporter.SERVICE_ID), "Missing resource import service");
		assertTrue(allServices.containsKey(DecompilerManager.SERVICE_ID), "Missing decompile manager service");
		assertTrue(allServices.containsKey(InheritanceGraph.SERVICE_ID), "Missing inheritance graph service");
		assertTrue(allServices.containsKey(JavacCompiler.SERVICE_ID), "Missing java compiler service");
		assertTrue(allServices.containsKey(MappingFormatManager.SERVICE_ID), "Missing mapping file format service");
		assertTrue(allServices.containsKey(MappingGenerator.SERVICE_ID), "Missing mapping generator service");
		assertTrue(allServices.containsKey(AggregateMappingManager.SERVICE_ID), "Missing mapping aggregator service");
		assertTrue(allServices.containsKey(AttachManager.SERVICE_ID), "Missing attach service");
		assertTrue(allServices.containsKey(SearchService.SERVICE_ID), "Missing search service");
		assertTrue(allServices.containsKey(PluginManager.SERVICE_ID), "Missing plugin management service");
		assertTrue(allServices.containsKey(CallGraph.SERVICE_ID), "Missing call graph service");
		assertTrue(allServices.containsKey(ConfigManager.SERVICE_ID), "Missing config service");
	}
}
