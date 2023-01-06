package software.coley.recaf.services.mapping.format;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import org.objectweb.asm.commons.Remapper;
import org.slf4j.Logger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.services.mapping.BasicMappingsRemapper;
import software.coley.recaf.services.mapping.IntermediateMappings;
import software.coley.recaf.services.mapping.Mappings;
import software.coley.recaf.services.mapping.data.ClassMapping;
import software.coley.recaf.services.mapping.data.FieldMapping;
import software.coley.recaf.services.mapping.data.MethodMapping;
import software.coley.recaf.util.StringUtil;

/**
 * The MCP SRG format.
 *
 * @author Matt Coley
 */
@Dependent
public class SrgMappings extends AbstractMappingFileFormat {
	public static final String NAME = "SRG";
	private final Logger logger = Logging.get(TinyV1Mappings.class);

	/**
	 * New SRG instance.
	 */
	public SrgMappings() {
		super(NAME, false, false);
	}

	@Override
	public IntermediateMappings parse(@Nonnull String mappingText) {
		IntermediateMappings mappings = new IntermediateMappings();
		String[] lines = StringUtil.splitNewline(mappingText);
		int line = 0;
		for (String lineStr : lines) {
			line++;
			String[] args = lineStr.trim().split(" ");
			String type = args[0];
			try {
				switch (type) {
					case "PK:":
						// Ignore package entries
						break;
					case "CL:":
						String obfClass = args[1];
						String renamedClass = args[2];
						mappings.addClass(obfClass, renamedClass);
						break;
					case "FD:": {
						String obfKey = args[1];
						int splitPos = obfKey.lastIndexOf('/');
						String obfOwner = obfKey.substring(0, splitPos);
						String obfName = obfKey.substring(splitPos + 1);
						String renamedKey = args[2];
						splitPos = renamedKey.lastIndexOf('/');
						String renamedName = renamedKey.substring(splitPos + 1);
						mappings.addField(obfOwner, null, obfName, renamedName);
						break;
					}
					case "MD:": {
						String obfKey = args[1];
						int splitPos = obfKey.lastIndexOf('/');
						String obfOwner = obfKey.substring(0, splitPos);
						String obfName = obfKey.substring(splitPos + 1);
						String obfDesc = args[2];
						String renamedKey = args[3];
						splitPos = renamedKey.lastIndexOf('/');
						String renamedName = renamedKey.substring(splitPos + 1);
						mappings.addMethod(obfOwner, obfDesc, obfName, renamedName);
						break;
					}
					default:
						logger.trace("Unknown SRG mappings line type: \"{}\" @line {}", type, line);
						break;
				}
			} catch (IndexOutOfBoundsException ex) {
				throw new IllegalArgumentException("Failed parsing line " + line, ex);
			}
		}
		return mappings;
	}

	@Override
	public String exportText(Mappings mappings) {
		StringBuilder sb = new StringBuilder();
		Remapper remapper = new BasicMappingsRemapper(mappings);
		IntermediateMappings intermediate = mappings.exportIntermediate();
		for (String oldClassName : intermediate.getClassesWithMappings()) {
			ClassMapping classMapping = intermediate.getClassMapping(oldClassName);
			if (classMapping != null) {
				String newClassName = classMapping.getNewName();
				// CL: BaseClass TargetClass
				sb.append("CL: ").append(oldClassName).append(' ')
						.append(newClassName).append("\n");
			}
			String newClassName = classMapping == null ? oldClassName : classMapping.getNewName();
			for (FieldMapping fieldMapping : intermediate.getClassFieldMappings(oldClassName)) {
				String oldFieldName = fieldMapping.getOldName();
				String newFieldName = fieldMapping.getNewName();
				// FD: BaseClass/baseField TargetClass/targetField
				sb.append("FD: ")
						.append(oldClassName).append('/').append(oldFieldName)
						.append(' ')
						.append(newClassName).append('/').append(newFieldName).append("\n");
			}
			for (MethodMapping methodMapping : intermediate.getClassMethodMappings(oldClassName)) {
				String oldMethodName = methodMapping.getOldName();
				String newMethodName = methodMapping.getNewName();
				String methodDesc = methodMapping.getDesc();
				String mappedDesc = remapper.mapDesc(methodDesc);
				// MD: BaseClass/baseMethod baseDesc TargetClass/targetMethod targetDesc
				sb.append("MD: ")
						.append(oldClassName).append('/').append(oldMethodName)
						.append(' ')
						.append(methodDesc)
						.append(' ')
						.append(newClassName).append('/').append(newMethodName)
						.append(' ')
						.append(mappedDesc).append('\n');
			}
		}
		return sb.toString();
	}
}
