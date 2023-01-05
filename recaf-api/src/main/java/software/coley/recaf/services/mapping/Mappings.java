package software.coley.recaf.services.mapping;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.recaf.services.mapping.format.MappingFileFormat;

/**
 * Outline of intermediate mappings, allowing for clear retrieval regardless of internal storage of mappings.
 * <br>
 * <h2>Relevant noteworthy points</h2>
 * <b>Incomplete mappings</b>: When imported from a {@link MappingFileFormat} not all formats are made equal.
 * Some contain less information than others. See the note in {@link MappingFileFormat} for more information.
 * <br><br>
 * <b>Member references pointing to child sub-types</b>: References to class members can point to child sub-types of
 * the class that defines the member. You may need to check the owner's type hierarchy to see if the field or method
 * is actually defined by a parent class.
 *
 * @author Matt Coley
 */
public interface Mappings {
	/**
	 * @param internalName
	 * 		Original class's internal name.
	 *
	 * @return Mapped name of the class, or {@code null} if no mapping exists.
	 */
	@Nullable
	String getMappedClassName(String internalName);

	/**
	 * @param ownerName
	 * 		Internal name of the class defining the field.<br>
	 * 		<b>NOTE</b>: References to class members can point to child sub-types of the class that defines the member.
	 * 		You may need to check the owner's type hierarchy to see if the field is actually defined in a parent class.
	 * @param fieldName
	 * 		Name of the field.
	 * @param fieldDesc
	 * 		Descriptor of the field.
	 *
	 * @return Mapped name of the field, or {@code null} if no mapping exists.
	 */
	@Nullable
	String getMappedFieldName(String ownerName, String fieldName, String fieldDesc);

	/**
	 * @param ownerName
	 * 		Internal name of the class defining the method.<br>
	 * 		<b>NOTE</b>: References to class members can point to child sub-types of the class that defines the member.
	 * 		You may need to check the owner's type hierarchy to see if the field is actually defined in a parent class.
	 * @param methodName
	 * 		Name of the method.
	 * @param methodDesc
	 * 		Descriptor of the method.
	 *
	 * @return Mapped name of the method, or {@code null} if no mapping exists.
	 */
	@Nullable
	String getMappedMethodName(String ownerName, String methodName, String methodDesc);

	/**
	 * @param className
	 * 		Internal name of the class defining the method the variable resides in.
	 * @param methodName
	 * 		Name of the method.
	 * @param methodDesc
	 * 		Descriptor of the method.
	 * @param name
	 * 		Name of the variable.
	 * @param desc
	 * 		Descriptor of the variable.
	 * @param index
	 * 		Index of the variable.
	 *
	 * @return Mapped name of the variable, or {@code null} if no mapping exists.
	 */
	@Nullable
	String getMappedVariableName(String className, String methodName, String methodDesc,
								 String name, String desc, int index);

	/**
	 * @return Object representation of mappings.
	 *
	 * @see #importIntermediate(IntermediateMappings)
	 */
	@Nonnull
	IntermediateMappings exportIntermediate();

	/**
	 * @param mappings
	 * 		Object representation of mappings.
	 *
	 * @see #exportIntermediate()
	 */
	void importIntermediate(@Nonnull IntermediateMappings mappings);
}