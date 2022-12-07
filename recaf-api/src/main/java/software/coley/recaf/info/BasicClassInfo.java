package software.coley.recaf.info;

import software.coley.recaf.info.annotation.AnnotationInfo;
import software.coley.recaf.info.annotation.TypeAnnotationInfo;
import software.coley.recaf.info.builder.AbstractClassInfoBuilder;
import software.coley.recaf.info.member.FieldMember;
import software.coley.recaf.info.member.MethodMember;
import software.coley.recaf.info.properties.Property;
import software.coley.recaf.info.properties.PropertyContainer;

import java.util.List;
import java.util.Map;

/**
 * Basic implementation of class info.
 *
 * @author Matt Coley
 * @see BasicJvmClassInfo
 * @see BasicAndroidClassInfo
 */
public abstract class BasicClassInfo implements ClassInfo {
	private final PropertyContainer properties;
	private final String name;
	private final String superName;
	private final List<String> interfaces;
	private final int access;
	private final String signature;
	private final String sourceFileName;
	private final List<AnnotationInfo> annotations;
	private final List<TypeAnnotationInfo> typeAnnotations;
	private final String outerClassName;
	private final String outerMethodName;
	private final String outerMethodDescriptor;
	private final List<InnerClassInfo> innerClasses;
	private final List<FieldMember> fields;
	private final List<MethodMember> methods;

	protected BasicClassInfo(AbstractClassInfoBuilder<?> builder) {
		this(builder.getName(),
				builder.getSuperName(),
				builder.getInterfaces(),
				builder.getAccess(),
				builder.getSignature(),
				builder.getSourceFileName(),
				builder.getAnnotations(),
				builder.getTypeAnnotations(),
				builder.getOuterClassName(),
				builder.getOuterMethodName(),
				builder.getOuterMethodDescriptor(),
				builder.getInnerClasses(),
				builder.getFields(),
				builder.getMethods(),
				builder.getPropertyContainer());
	}

	protected BasicClassInfo(String name, String superName, List<String> interfaces, int access,
							 String signature, String sourceFileName,
							 List<AnnotationInfo> annotations, List<TypeAnnotationInfo> typeAnnotations,
							 String outerClassName, String outerMethodName, String outerMethodDescriptor,
							 List<InnerClassInfo> innerClasses, List<FieldMember> fields, List<MethodMember> methods,
							 PropertyContainer properties) {
		this.name = name;
		this.superName = superName;
		this.interfaces = interfaces;
		this.access = access;
		this.signature = signature;
		this.sourceFileName = sourceFileName;
		this.annotations = annotations;
		this.typeAnnotations = typeAnnotations;
		this.outerClassName = outerClassName;
		this.outerMethodName = outerMethodName;
		this.outerMethodDescriptor = outerMethodDescriptor;
		this.innerClasses = innerClasses;
		this.fields = fields;
		this.methods = methods;
		this.properties = properties;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getSuperName() {
		return superName;
	}

	@Override
	public List<String> getInterfaces() {
		return interfaces;
	}

	@Override
	public int getAccess() {
		return access;
	}

	@Override
	public String getSignature() {
		return signature;
	}

	@Override
	public String getSourceFileName() {
		return sourceFileName;
	}

	@Override
	public List<AnnotationInfo> getAnnotations() {
		return annotations;
	}

	@Override
	public List<TypeAnnotationInfo> getTypeAnnotations() {
		return typeAnnotations;
	}

	@Override
	public String getOuterClassName() {
		return outerClassName;
	}

	@Override
	public String getOuterMethodName() {
		return outerMethodName;
	}

	@Override
	public String getOuterMethodDescriptor() {
		return outerMethodDescriptor;
	}

	@Override
	public List<String> getOuterClassBreadcrumbs() {
		return null;
	}

	@Override
	public List<InnerClassInfo> getInnerClasses() {
		return innerClasses;
	}

	@Override
	public List<FieldMember> getFields() {
		return fields;
	}

	@Override
	public List<MethodMember> getMethods() {
		return methods;
	}

	@Override
	public <V> void setProperty(Property<V> property) {
		properties.setProperty(property);
	}

	@Override
	public void removeProperty(String key) {
		properties.removeProperty(key);
	}

	@Override
	public Map<String, Property<?>> getProperties() {
		return properties.getProperties();
	}
}
