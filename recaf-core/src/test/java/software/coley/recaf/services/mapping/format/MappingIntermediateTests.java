package software.coley.recaf.services.mapping.format;

import org.junit.jupiter.api.Test;
import software.coley.recaf.TestBase;
import software.coley.recaf.services.mapping.Mappings;
import software.coley.recaf.services.mapping.MappingsAdapter;
import software.coley.recaf.services.mapping.format.MappingFormatManager;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link MappingFileFormat} implementation support for importing from intermediate mappings.
 */
public class MappingIntermediateTests extends TestBase {
	@Test
	void testMapFromIntermediate() {
		// Setup base mappings
		MappingsAdapter adapter = new MappingsAdapter("TEST", true, true);
		String oldClassName = "Foo";
		String newClassName = "Bar";
		String oldMethodName = "say";
		String newMethodName = "speak";
		String oldFieldName = "syntax";
		String newFieldName = "pattern";
		String methodDesc = "(Ljava/lang/String;)V";
		String fieldDesc = "Ljava/lang/String;";
		adapter.addClass(oldClassName, newClassName);
		adapter.addField(oldClassName, oldFieldName, fieldDesc, newFieldName);
		adapter.addMethod(oldClassName, oldMethodName, methodDesc, newMethodName);

		// Assert registered mapping types can import from the intermediate
		MappingFormatManager formatManager = recaf.get(MappingFormatManager.class);
		assertTrue(formatManager.getMappingFileFormats().size() > 1);
		for (String formatName : formatManager.getMappingFileFormats()) {
			Mappings mappings = formatManager.createFormatInstance(formatName);
			System.out.println("Intermediate -> " + mappings.implementationName());
			assertTrue(mappings.supportsExportIntermediate());

			// Import from intermediate
			mappings.importIntermediate(adapter.exportIntermediate());

			assertEquals(newClassName, mappings.getMappedClassName(oldClassName));
			assertEquals(newFieldName, mappings.getMappedFieldName(oldClassName, oldFieldName, fieldDesc));
			assertEquals(newMethodName, mappings.getMappedMethodName(oldClassName, oldMethodName, methodDesc));

			// Export and print
			if (mappings.supportsExportText())
				System.out.println(mappings.exportText());
			else
				System.out.println("Mappings does not support text export: " + mappings.implementationName() + "\n");
		}
	}
}
