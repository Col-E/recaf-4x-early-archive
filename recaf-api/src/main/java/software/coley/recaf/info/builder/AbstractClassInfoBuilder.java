package software.coley.recaf.info.builder;

import software.coley.recaf.info.Accessed;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.InnerClassInfo;
import software.coley.recaf.info.annotation.AnnotationInfo;
import software.coley.recaf.info.annotation.TypeAnnotationInfo;
import software.coley.recaf.info.member.FieldMember;
import software.coley.recaf.info.member.MethodMember;
import software.coley.recaf.info.properties.BasicPropertyContainer;
import software.coley.recaf.info.properties.PropertyContainer;

import java.util.Collections;
import java.util.List;

/**
 * Common builder info for {@link ClassInfo}.
 *
 * @param <B>
 * 		Self type. Exists so implementations don't get stunted in their chaining.
 *
 * @author Matt Coley
 * @see JvmClassInfoBuilder For {@link software.coley.recaf.info.JvmClassInfo}
 * @see AndroidClassInfoBuilder For {@link software.coley.recaf.info.AndroidClassInfo}
 */
public abstract class AbstractClassInfoBuilder<B extends AbstractClassInfoBuilder<?>> {
	private String name;
	private String superName = "java/lang/Object";
	private List<String> interfaces = Collections.emptyList();
	private final AccessImpl access = new AccessImpl();
	private String signature;
	private String sourceFileName;
	private List<AnnotationInfo> annotations = Collections.emptyList();
	private List<TypeAnnotationInfo> typeAnnotations = Collections.emptyList();
	private String outerClassName;
	private String outerMethodName;
	private String outerMethodDescriptor;
	private List<InnerClassInfo> innerClasses = Collections.emptyList();
	private List<FieldMember> fields = Collections.emptyList();
	private List<MethodMember> methods = Collections.emptyList();
	private PropertyContainer propertyContainer = new BasicPropertyContainer();

	protected AbstractClassInfoBuilder() {
		// default
	}

	protected AbstractClassInfoBuilder(ClassInfo classInfo) {
		// copy state
		withName(classInfo.getName());
		withSuperName(classInfo.getSuperName());
		withInterfaces(classInfo.getInterfaces());
		withAccess(classInfo.getAccess());
		withSignature(classInfo.getSignature());
		withSourceFileName(classInfo.getSourceFileName());
		withAnnotations(classInfo.getAnnotations());
		withTypeAnnotations(classInfo.getTypeAnnotations());
		withOuterClassName(classInfo.getOuterClassName());
		withOuterMethodName(classInfo.getOuterMethodName());
		withOuterMethodDescriptor(classInfo.getOuterMethodDescriptor());
		withInnerClasses(classInfo.getInnerClasses());
		withFields(classInfo.getFields());
		withMethods(classInfo.getMethods());
		withPropertyContainer(new BasicPropertyContainer(classInfo.getProperties()));
	}

	@SuppressWarnings("unchecked")
	public B withName(String name) {
		this.name = name;
		return (B) this;
	}

	@SuppressWarnings("unchecked")
	public B withSuperName(String superName) {
		this.superName = superName;
		return (B) this;
	}

	@SuppressWarnings("unchecked")
	public B withInterfaces(List<String> interfaces) {
		this.interfaces = interfaces;
		return (B) this;
	}

	@SuppressWarnings("unchecked")
	public B withAccess(int access) {
		this.access.value = access;
		return (B) this;
	}

	@SuppressWarnings("unchecked")
	public B withSignature(String signature) {
		this.signature = signature;
		return (B) this;
	}

	@SuppressWarnings("unchecked")
	public B withSourceFileName(String sourceFileName) {
		this.sourceFileName = sourceFileName;
		return (B) this;
	}

	@SuppressWarnings("unchecked")
	public B withAnnotations(List<AnnotationInfo> annotations) {
		this.annotations = annotations;
		return (B) this;
	}

	@SuppressWarnings("unchecked")
	public B withTypeAnnotations(List<TypeAnnotationInfo> typeAnnotations) {
		this.typeAnnotations = typeAnnotations;
		return (B) this;
	}

	@SuppressWarnings("unchecked")
	public B withOuterClassName(String outerClassName) {
		this.outerClassName = outerClassName;
		return (B) this;
	}

	@SuppressWarnings("unchecked")
	public B withOuterMethodName(String outerMethodName) {
		this.outerMethodName = outerMethodName;
		return (B) this;
	}

	@SuppressWarnings("unchecked")
	public B withOuterMethodDescriptor(String outerMethodDescriptor) {
		this.outerMethodDescriptor = outerMethodDescriptor;
		return (B) this;
	}

	@SuppressWarnings("unchecked")
	public B withInnerClasses(List<InnerClassInfo> innerClasses) {
		this.innerClasses = innerClasses;
		return (B) this;
	}

	@SuppressWarnings("unchecked")
	public B withFields(List<FieldMember> fields) {
		this.fields = fields;
		return (B) this;
	}

	@SuppressWarnings("unchecked")
	public B withMethods(List<MethodMember> methods) {
		this.methods = methods;
		return (B) this;
	}

	@SuppressWarnings("unchecked")
	public B withPropertyContainer(PropertyContainer propertyContainer) {
		this.propertyContainer = propertyContainer;
		return (B) this;
	}

	public String getName() {
		return name;
	}

	public String getSuperName() {
		return superName;
	}

	public List<String> getInterfaces() {
		return interfaces;
	}

	public int getAccess() {
		return access.value;
	}

	public String getSignature() {
		return signature;
	}

	public String getSourceFileName() {
		return sourceFileName;
	}

	public List<AnnotationInfo> getAnnotations() {
		return annotations;
	}

	public List<TypeAnnotationInfo> getTypeAnnotations() {
		return typeAnnotations;
	}

	public String getOuterClassName() {
		return outerClassName;
	}

	public String getOuterMethodName() {
		return outerMethodName;
	}

	public String getOuterMethodDescriptor() {
		return outerMethodDescriptor;
	}

	public List<InnerClassInfo> getInnerClasses() {
		return innerClasses;
	}

	public List<FieldMember> getFields() {
		return fields;
	}

	public List<MethodMember> getMethods() {
		return methods;
	}

	public PropertyContainer getPropertyContainer() {
		return propertyContainer;
	}

	public abstract ClassInfo build();

	protected void verify() {
		if (name == null)
			throw new IllegalArgumentException("Name required");
		if (superName == null && !access.hasAnnotationModifier())
			throw new IllegalArgumentException("Super-name required");
	}

	static class AccessImpl implements Accessed {
		private int value;

		@Override
		public int getAccess() {
			return value;
		}
	}
}
