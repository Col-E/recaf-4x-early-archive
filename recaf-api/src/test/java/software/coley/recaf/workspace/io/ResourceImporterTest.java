package software.coley.recaf.workspace.io;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.coley.recaf.info.FileInfo;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.test.TestClassUtils;
import software.coley.recaf.test.dummy.HelloWorld;
import software.coley.recaf.util.AccessPatcher;
import software.coley.recaf.util.ZipCreationUtils;
import software.coley.recaf.util.io.ByteSource;
import software.coley.recaf.util.io.ByteSources;
import software.coley.recaf.workspace.model.resource.WorkspaceFileResource;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ResourceImporter}
 */
class ResourceImporterTest {
	static ResourceImporter importer;

	@BeforeAll
	static void setup() {
		AccessPatcher.patch();
		importer = new BasicResourceImporter(new BasicInfoImporter(new BasicClassPatcher()));
	}

	@Test
	void testImportSingleClass() throws IOException {
		JvmClassInfo helloWorldInfo = TestClassUtils.fromRuntimeClass(HelloWorld.class);
		ByteSource classSource = ByteSources.wrap(helloWorldInfo.getBytecode());
		WorkspaceResource resource = importer.importResource(classSource);

		// Should be aware of the class file as its source
		assertInstanceOf(WorkspaceFileResource.class, resource,
				"Resource didn't keep single-class as its associated input");

		// Should have just ONE class in the JVM bundle
		assertEquals(1, resource.getJvmClassBundle().size());
		assertEquals(0, resource.getVersionedJvmClassBundles().size());
		assertEquals(0, resource.getAndroidClassBundles().size());
		assertEquals(0, resource.getFileBundle().size());
		assertEquals(0, resource.getEmbeddedResources().size());
		assertNull(resource.getContainingResource());
		assertFalse(resource.isEmbeddedResource());
		assertFalse(resource.isInternal());

		// Validate JVM class bundle content
		JvmClassInfo classInfo = resource.getJvmClassBundle().iterator().next();
		assertEquals(helloWorldInfo, classInfo, "Missing data compared to baseline input class");
	}

	@Test
	void testImportSingleFile() throws IOException {
		byte[] helloBytes = "Hello".getBytes(StandardCharsets.UTF_8);
		ByteSource classSource = ByteSources.wrap(helloBytes);
		WorkspaceResource resource = importer.importResource(classSource);

		// Should be aware of the class file as its source
		assertInstanceOf(WorkspaceFileResource.class, resource,
				"Resource didn't keep single-file as its associated input");

		// Should have just ONE file in the file bundle
		assertEquals(0, resource.getJvmClassBundle().size());
		assertEquals(0, resource.getVersionedJvmClassBundles().size());
		assertEquals(0, resource.getAndroidClassBundles().size());
		assertEquals(1, resource.getFileBundle().size());
		assertEquals(0, resource.getEmbeddedResources().size());
		assertNull(resource.getContainingResource());
		assertFalse(resource.isEmbeddedResource());
		assertFalse(resource.isInternal());

		// Validate file bundle content
		FileInfo fileInfo = resource.getFileBundle().iterator().next();
		assertArrayEquals(helloBytes, fileInfo.getRawContent(), "Missing data compared to baseline input bytes");
	}

	@Test
	void testImportZipInsideZip() throws IOException {
		// create a ZIP holding another ZIP
		String insideZipName = "inner.zip";
		String innerDataName = "data";
		byte[] innerData = {1, 2, 3};
		byte[] insideZipBytes = ZipCreationUtils.createSingleEntryZip(innerDataName, innerData);
		byte[] outsideZipBytes = ZipCreationUtils.createSingleEntryZip(insideZipName, insideZipBytes);
		ByteSource classSource = ByteSources.wrap(outsideZipBytes);
		WorkspaceResource resource = importer.importResource(classSource);

		// Should be aware of the ZIP file as its source
		assertInstanceOf(WorkspaceFileResource.class, resource,
				"Resource didn't keep single-file as its associated input");

		// Should create an embedded resource
		assertEquals(1, resource.getEmbeddedResources().size(), "Should have 1 embedded resource");
		WorkspaceFileResource insideZipResource = resource.getEmbeddedResources().get(insideZipName);
		assertNotNull(insideZipResource, "Incorrect embedded resource path, expected: " + insideZipName +
				" got " + insideZipResource.getFileInfo().getName());

		// Should have expected data
		FileInfo innerDotZip = insideZipResource.getFileBundle().get(innerDataName);
		assertNotNull(innerDotZip);
		assertEquals(innerDataName, innerDotZip.getName());
		assertArrayEquals(innerData, innerDotZip.getRawContent());
	}

	@Test
	void testImportsFromDifferentSourcesAreTheSame() throws IOException {
		// Create zip:
		//  - hello.txt
		//  - foo.zip (containing foo)
		//  - bla/bla/bla/HelloWorld.class
		Map<String, byte[]> map = new HashMap<>();
		map.put("hello.txt", "Hello world".getBytes(StandardCharsets.UTF_8));
		map.put(HelloWorld.class.getName().replace(".", "/") + ".class",
				TestClassUtils.fromRuntimeClass(HelloWorld.class).getBytecode());
		map.put("data.zip", ZipCreationUtils.createSingleEntryZip("foo", new byte[]{1, 2, 3}));
		byte[] zipBytes = ZipCreationUtils.createZip(map);

		// Write to disk temporarily for test duration
		File tempFile = File.createTempFile("recaf", "test.zip");
		Files.write(tempFile.toPath(), zipBytes);
		tempFile.deleteOnExit();

		// Create workspace resources from each kind of input, all sourced from the same content.
		// They should all be equal.
		WorkspaceResource fromByteSource = importer.importResource(ByteSources.wrap(zipBytes));
		WorkspaceFileResource fromFile = importer.importResource(tempFile);
		WorkspaceFileResource fromPath = importer.importResource(tempFile.toPath());
		WorkspaceFileResource fromUri = importer.importResource(tempFile.toURI());
		WorkspaceFileResource fromUrl = importer.importResource(tempFile.toURI().toURL());
		assertEquals(fromByteSource, fromFile);
		assertEquals(fromByteSource, fromPath);
		assertEquals(fromByteSource, fromUri);
		assertEquals(fromByteSource, fromUrl);
	}
}