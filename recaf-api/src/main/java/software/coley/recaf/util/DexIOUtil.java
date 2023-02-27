package software.coley.recaf.util;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedField;
import org.jf.dexlib2.dexbacked.DexBackedMethod;
import software.coley.recaf.info.AndroidClassInfo;
import software.coley.recaf.info.builder.AndroidClassInfoBuilder;
import software.coley.recaf.info.member.*;
import software.coley.recaf.util.io.ByteSource;
import software.coley.recaf.workspace.model.bundle.AndroidClassBundle;
import software.coley.recaf.workspace.model.bundle.BasicAndroidClassBundle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dex file reading and writing.
 *
 * @author Matt Coley
 */
public class DexIOUtil {
	/**
	 * @param source
	 * 		Content source to read from. Must be wrapping a dex file.
	 *
	 * @return Bundle of classes from the dex file.
	 *
	 * @throws IOException
	 * 		When the dex file cannot be read from.
	 */
	public static AndroidClassBundle read(ByteSource source) throws IOException {
		BasicAndroidClassBundle classBundle = new BasicAndroidClassBundle();
		Opcodes opcodes = Opcodes.getDefault();
		DexBackedDexFile file = DexBackedDexFile.fromInputStream(opcodes, source.openStream());

		// Record all classes in the file
		for (DexBackedClassDef dexClass : file.getClasses()) {
			AndroidClassInfoBuilder builder = new AndroidClassInfoBuilder()
					.withName(dexClass.getType())
					.withSuperName(dexClass.getSuperclass())
					.withInterfaces(dexClass.getInterfaces())
					.withAccess(dexClass.getAccessFlags())
					.withSourceFileName(dexClass.getSourceFile());
			// TODO: Parse metadata annotations for:
			//   - signature
			//   - outer-class-name
			//   - outer-method-name/descriptor
			//   - inner-classes
			//   - other annotations

			// Record fields
			List<FieldMember> fieldMembers = new ArrayList<>();
			for (DexBackedField field : dexClass.getFields()) {
				String signature = null;
				Object defaultValue = null;
				fieldMembers.add(new BasicFieldMember(
						field.getName(),
						field.getType(),
						signature,
						field.getAccessFlags(),
						defaultValue
				));
			}
			builder.withFields(fieldMembers);

			// Record methods
			List<MethodMember> methodMembers = new ArrayList<>();
			for (DexBackedMethod method : dexClass.getMethods()) {
				StringBuilder sb = new StringBuilder("(");
				for (CharSequence type : method.getParameterTypes()) {
					sb.append(type);
				}
				sb.append(")").append(method.getReturnType());
				String type = sb.toString();
				String signature = null;
				List<String> exceptions = Collections.emptyList();
				List<LocalVariable> variables = Collections.emptyList();
				methodMembers.add(new BasicMethodMember(
						method.getName(),
						type,
						signature,
						method.getAccessFlags(),
						exceptions,
						variables
				));
			}
			builder.withMethods(methodMembers);

			// Create class and add to bundle
			AndroidClassInfo classInfo = builder.build();
			classBundle.initialPut(classInfo);
		}
		return classBundle;
	}
}
