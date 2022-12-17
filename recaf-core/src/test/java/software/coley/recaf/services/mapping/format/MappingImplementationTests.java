package software.coley.recaf.services.mapping.format;

import org.junit.jupiter.api.Test;
import software.coley.recaf.services.mapping.IntermediateMappings;
import software.coley.recaf.services.mapping.Mappings;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests various {@link MappingFileFormat} implementation's ability to parse input texts.
 */
public class MappingImplementationTests {
	private static final String NAME_SAMPLE = "Sample";
	private static final String NAME_RENAMED = "Renamed";

	@Test
	void testTinyV1() {
		String mappingsText = "v1\tintermediary\tnamed\n" +
				"CLASS\ttest/Greetings\trename/Hello\n" +
				"FIELD\ttest/Greetings\tLjava/lang/String;\toldField\tnewField\n" +
				"METHOD\ttest/Greetings\t()V\tsay\tspeak";
		Mappings mappings = new TinyV1Mappings();
		mappings.parse(mappingsText);
		assertInheritMap(mappings);
	}

	@Test
	void testSimple() {
		String mappingsText = "test/Greetings rename/Hello\n" +
				"test/Greetings.oldField Ljava/lang/String; newField\n" +
				"test/Greetings.say()V speak";
		Mappings mappings = new SimpleMappings();
		mappings.parse(mappingsText);
		assertInheritMap(mappings);
	}

	@Test
	void testProguard() {
		String mappingsText = "# Backwards format because proguard mappings are intended to be undone, not applied\n" +
				"rename.Hello -> test.Greetings:\n" +
				"    java.lang.String newField -> oldField\n" +
				"    void speak() -> say";
		Mappings mappings = new ProguardMappings();
		mappings.parse(mappingsText);
		assertInheritMap(mappings);
	}

	@Test
	void testEnigma() {
		String mappingsText = "CLASS test/Greetings rename/Hello\n" +
				"\tFIELD oldField newField Ljava/lang/String;\n" +
				"\tMETHOD say speak ()V";
		Mappings mappings = new EnigmaMappings();
		mappings.parse(mappingsText);
		assertInheritMap(mappings);
	}

	@Test
	void testJadx() {
		String mappingsText = "c test.Greetings = Hello\n" +
				"f test.Greetings.oldField:Ljava/lang/String; = newField\n" +
				"m test.Greetings.say()V = speak";
		Mappings mappings = new JadxMappings();
		mappings.parse(mappingsText);

		// Cannot use same 'assertInheritMap(...)' because Jadx format doesn't allow package renaming
		assertEquals("test/Hello", mappings.getMappedClassName("test/Greetings"));
		assertEquals("newField", mappings.getMappedFieldName("test/Greetings", "oldField", "Ljava/lang/String;"));
		assertEquals("speak", mappings.getMappedMethodName("test/Greetings", "say", "()V"));
	}

	// TODO: Test cases for other formats once supported
	//  - TinyV2
	//  - TSRG

	/**
	 * @param mappings
	 * 		Mappings to check.
	 */
	private void assertInheritMap(Mappings mappings) {
		assertEquals("rename/Hello", mappings.getMappedClassName("test/Greetings"));
		assertEquals("newField", mappings.getMappedFieldName("test/Greetings", "oldField", "Ljava/lang/String;"));
		assertEquals("speak", mappings.getMappedMethodName("test/Greetings", "say", "()V"));
	}

	/**
	 * Dummy mappings that only renames the name "Sample".
	 */
	private static class SampleMappings implements Mappings {
		@Override
		public String getMappedClassName(String internalName) {
			if (internalName.equals(NAME_SAMPLE)) {
				return NAME_RENAMED;
			}
			return null;
		}

		@Override
		public String getMappedFieldName(String ownerName, String fieldName, String fieldDesc) {
			return null;
		}

		@Override
		public String getMappedMethodName(String ownerName, String methodName, String methodDesc) {
			return null;
		}

		@Override
		public String getMappedVariableName(String className, String methodName, String methodDesc,
											String name, String desc, int index) {
			return null;
		}

		@Override
		public String implementationName() {
			return null;
		}

		@Override
		public void parse(String mappingsText) {
		}

		@Override
		public String exportText() {
			return null;
		}

		@Override
		public IntermediateMappings exportIntermediate() {
			return null;
		}

		@Override
		public void importIntermediate(IntermediateMappings mappings) {
			// no-op
		}
	}
}