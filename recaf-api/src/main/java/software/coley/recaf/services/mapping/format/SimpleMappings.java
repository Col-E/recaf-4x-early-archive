package software.coley.recaf.services.mapping.format;

import jakarta.enterprise.context.Dependent;
import software.coley.recaf.services.mapping.IntermediateMappings;
import software.coley.recaf.services.mapping.MappingsAdapter;
import software.coley.recaf.services.mapping.data.ClassMapping;
import software.coley.recaf.services.mapping.data.FieldMapping;
import software.coley.recaf.services.mapping.data.MethodMapping;
import software.coley.recaf.util.StringUtil;

import java.util.Map;

import static software.coley.recaf.util.EscapeUtil.escapeAll;
import static software.coley.recaf.util.EscapeUtil.unescapeAll;

/**
 * Simple mappings file implementation where the old/new names are split by a space.
 * The input format of the mappings is based on the format outlined by
 * {@link org.objectweb.asm.commons.SimpleRemapper#SimpleRemapper(Map)}.
 * <br>
 * Differences include:
 * <ul>
 *     <li>Support for {@code #comment} lines</li>
 *     <li>Support for unicode escape sequences ({@code \\uXXXX})</li>
 *     <li>Support for fields specified by their name <i>and descriptor</i></li>
 * </ul>
 *
 * @author Matt Coley
 * @author Wolfie / win32kbase
 */
@Dependent
public class SimpleMappings extends MappingsAdapter implements MappingFileFormat {
	public static final String NAME = "Simple";

	/**
	 * New simple instance.
	 */
	public SimpleMappings() {
		super(NAME, true, true);
	}

	@Override
	public boolean supportsExportText() {
		return true;
	}

	@Override
	public void parse(String mappingText) {
		String[] lines = StringUtil.splitNewline(mappingText);
		// # Comment
		// BaseClass TargetClass
		// BaseClass.baseField targetField
		// BaseClass.baseField baseDesc targetField
		// BaseClass.baseMethod(BaseMethodDesc) targetMethod
		for (String line : lines) {
			// Skip comments and empty lines
			if (line.trim().startsWith("#") || line.trim().isEmpty())
				continue;
			String[] args = line.split(" ");
			String oldBaseName = unescapeAll(args[0]);
			if (args.length >= 3) {
				// Descriptor qualified field format
				String desc = unescapeAll(args[1]);
				String targetName = unescapeAll(args[2]);
				int dot = oldBaseName.lastIndexOf('.');
				String oldClassName = oldBaseName.substring(0, dot);
				String oldFieldName = oldBaseName.substring(dot + 1);
				addField(oldClassName, oldFieldName, desc, targetName);
			} else {
				String newName = unescapeAll(args[1]);
				int dot = oldBaseName.lastIndexOf('.');
				if (dot > 0) {
					// Indicates a member
					String oldClassName = oldBaseName.substring(0, dot);
					String oldIdentifier = oldBaseName.substring(dot + 1);
					int methodDescStart = oldIdentifier.lastIndexOf("(");
					if (methodDescStart > 0) {
						// Method descriptor part of ID, split it up
						String methodName = oldIdentifier.substring(0, methodDescStart);
						String methodDesc = oldIdentifier.substring(methodDescStart);
						addMethod(oldClassName, methodName, methodDesc, newName);
					} else {
						// Likely a field without linked descriptor
						addField(oldClassName, oldIdentifier, newName);
					}
				} else {
					addClass(oldBaseName, newName);
				}
			}
		}
	}

	@Override
	public String exportText() {
		StringBuilder sb = new StringBuilder();
		IntermediateMappings intermediate = exportIntermediate();
		for (String oldClassName : intermediate.getClassesWithMappings()) {
			ClassMapping classMapping = intermediate.getClassMapping(oldClassName);
			String escapedOldClassName = escapeAll(oldClassName);
			if (classMapping != null) {
				String newClassName = classMapping.getNewName();
				// BaseClass TargetClass
				sb.append(escapedOldClassName).append(' ').append(newClassName).append("\n");
			}
			for (FieldMapping fieldMapping : intermediate.getClassFieldMappings(oldClassName)) {
				String oldFieldName = escapeAll(fieldMapping.getOldName());
				String newFieldName = escapeAll(fieldMapping.getNewName());
				String fieldDesc = escapeAll(fieldMapping.getDesc());
				if (fieldDesc != null) {
					// BaseClass.baseField baseDesc targetField
					sb.append(escapedOldClassName).append('.').append(oldFieldName)
							.append(' ').append(fieldDesc)
							.append(' ').append(newFieldName).append("\n");
				} else {
					// BaseClass.baseField targetField
					sb.append(escapedOldClassName).append('.').append(oldFieldName)
							.append(' ').append(newFieldName).append("\n");
				}
			}
			for (MethodMapping methodMapping : intermediate.getClassMethodMappings(oldClassName)) {
				String oldMethodName = escapeAll(methodMapping.getOldName());
				String newMethodName = escapeAll(methodMapping.getNewName());
				String methodDesc = escapeAll(methodMapping.getDesc());
				// BaseClass.baseMethod(BaseMethodDesc) targetMethod
				sb.append(escapedOldClassName).append('.').append(oldMethodName)
						.append(methodDesc)
						.append(' ').append(newMethodName).append("\n");
			}
		}
		return sb.toString();
	}
}
