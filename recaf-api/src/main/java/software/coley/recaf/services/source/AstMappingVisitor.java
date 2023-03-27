package software.coley.recaf.services.source;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.*;
import org.openrewrite.marker.Markers;
import software.coley.recaf.services.mapping.Mappings;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static software.coley.recaf.services.source.AstUtils.toInternal;

/**
 * {@link JavaVisitor} to remap type and member references from a {@link Mappings} instance in a {@link J.CompilationUnit}.
 *
 * @author Matt Coley
 */
public class AstMappingVisitor extends JavaIsoVisitor<ExecutionContext> {
	private final Mappings mappings;
	private JavaType.FullyQualified currentType;

	/**
	 * @param mappings
	 * 		Mappings to apply.
	 */
	public AstMappingVisitor(@Nonnull Mappings mappings) {
		this.mappings = mappings;
	}

	// TODO: Support for mapping fields & methods
	//  - Currently this is set up to ONLY handle CLASSES.
	//  - Adding additional mapping will likely cause some of the assumptions in the existing logic to break
	//    so I expect more changes will need to be made to support this.

	@Nonnull
	@Override
	public J.CompilationUnit visitCompilationUnit(@Nonnull J.CompilationUnit cu, @Nonnull ExecutionContext ctx) {
		List<J.ClassDeclaration> classes = cu.getClasses();
		if (classes.isEmpty())
			throw new IllegalStateException("Unit must have at least one class!");
		currentType = classes.get(0).getType();
		return super.visitCompilationUnit(cu, ctx);
	}

	@Nonnull
	@Override
	public J.Package visitPackage(@Nonnull J.Package pkg, @Nonnull ExecutionContext ctx) {
		String internalType = toInternal(currentType);
		String mappedType = mappings.getMappedClassName(internalType);
		if (mappedType != null) {
			int endIndex = mappedType.lastIndexOf('/');
			if (endIndex > 0) {
				String mappedPackage = mappedType.substring(0, endIndex).replace('/', '.');
				pkg = pkg.withTemplate(
						JavaTemplate.builder(this::getCursor, mappedPackage).build(),
						pkg.getCoordinates().replace()
				);
			}
		}
		return pkg;
	}

	@Nonnull
	@Override
	public J.Import visitImport(@Nonnull J.Import impoort, @Nonnull ExecutionContext ctx) {
		String internalType = impoort.getTypeName().replace('.', '/');
		String mappedType = mappings.getMappedClassName(internalType);
		if (mappedType != null) {
			J.FieldAccess qualid = impoort.getQualid();
			String suffix = impoort.isStatic() ? "." + qualid.getName().getSimpleName() : "";
			mappedType = mappedType.replace('/', '.');
			int lastDot = mappedType.lastIndexOf('.');
			String simpleMappedName = mappedType.substring(lastDot + 1);
			String packageName = mappedType.substring(0, lastDot);
			// This is really cringe, but it works.
			Markers markers = new Markers(UUID.randomUUID(), Collections.emptyList());
			Space space = Space.format("");
			J.Identifier packageIdentifier = new J.Identifier(UUID.randomUUID(), space, markers, packageName, null, null);
			J.Identifier nameIdentifier = new J.Identifier(UUID.randomUUID(), space, markers, simpleMappedName + suffix, null, null);
			impoort = impoort.withQualid(qualid.withName(nameIdentifier)
					.withTarget(packageIdentifier)
					.withType(visitType(qualid.getType(), ctx)));
		}
		return impoort;
	}

	@Nonnull
	@Override
	public J.Identifier visitIdentifier(@Nonnull J.Identifier identifier, @Nonnull ExecutionContext ctx) {
		J.Identifier visitedIdentifier = super.visitIdentifier(identifier, ctx);
		JavaType initialType = identifier.getType();
		JavaType visitedType = visitedIdentifier.getType();
		if (visitedType == null || initialType == null)
			return visitedIdentifier;

		// If the parent visit operation resulted in the identifier's type changing we need to update the identifier
		// to reflect the new class type.
		if (initialType != visitedType) {
			// Get the parent context, so we can make sense of what this identifier is used for.
			Cursor cursorParent = getCursor();
			do {
				cursorParent = cursorParent.getParent();
			} while (cursorParent != null &&
					(cursorParent.getValue() instanceof JContainer ||
							cursorParent.getValue() instanceof JLeftPadded ||
							cursorParent.getValue() instanceof JRightPadded));

			// Handle if valid parent found.
			if (cursorParent != null) {
				Object parentValue = cursorParent.getValue();
				if (parentValue instanceof J.Annotation ||
						parentValue instanceof J.VariableDeclarations ||
						parentValue instanceof J.ClassDeclaration ||
						parentValue instanceof J.MethodDeclaration ||
						parentValue instanceof J.MemberReference ||
						parentValue instanceof J.ControlParentheses ||
						parentValue instanceof J.ParameterizedType ||
						parentValue instanceof J.NewClass ||
						parentValue instanceof J.NewArray) {
					// In these cases, the identifier should always be a reference to the class type, and not a general name.
					// For instance:
					//  @NAME
					//  NAME variable = ...
					//  class NAME { ... }
					//  NAME getFoo() { ... }
					//  foo = (NAME) bar;
					//  new NAME
					//  new NAME[]
					JavaType.FullyQualified visitedQualified = (JavaType.FullyQualified) visitedType;
					visitedIdentifier = visitedIdentifier.withSimpleName(visitedQualified.getClassName());
				} else if (parentValue instanceof J.MethodInvocation || parentValue instanceof J.FieldAccess) {
					// This will handle the reference context.
					// For static references, we want to rename the identifier.
					// There's no 'isStatic' call, so we assume if the identifier is the class's simple name we ought to map it.
					if (visitedType instanceof JavaType.FullyQualified visitedQualified) {
						JavaType.FullyQualified initialQualified = (JavaType.FullyQualified) initialType;
						if (visitedIdentifier.getSimpleName().equals(initialQualified.getClassName()))
							visitedIdentifier = visitedIdentifier.withSimpleName(visitedQualified.getClassName());
					} else if (visitedType instanceof JavaType.Array) {
						// In this case, the identifier seems to always be the reference name.
						// We can skip doing anything here.
					} else {
						throw new UnsupportedOperationException("Calling context on reference unknown: " + visitedType.getClass().getName());
					}
				} else {
					System.out.println(parentValue.getClass().getSimpleName());
				}
			}
		}

		return visitedIdentifier;
	}

	@Nonnull
	@Override
	public J.MethodDeclaration visitMethodDeclaration(@Nonnull J.MethodDeclaration method, @Nonnull ExecutionContext ctx) {
		J.MethodDeclaration visitedMethod = super.visitMethodDeclaration(method, ctx);

		// Edge case for constructors
		if (visitedMethod.getSimpleName().equals(currentType.getClassName())) {
			J.Identifier name = method.getName();
			JavaType visitedNameType = visitType(currentType, ctx);
			JavaType.Method type = method.getMethodType();
			JavaType.Method visitedMethodType = (JavaType.Method) visitType(type, ctx);
			if (visitedNameType instanceof JavaType.FullyQualified qualified) {
				visitedMethod = visitedMethod
						.withName(name.withSimpleName(qualified.getClassName()))
						.withMethodType(visitedMethodType);
			}
		}

		return visitedMethod;
	}

	@Nullable
	@Override
	public JavaType visitType(@Nullable JavaType javaType, @Nonnull ExecutionContext ctx) {
		if (javaType instanceof JavaType.FullyQualified qualifiedType) {
			// Update class references
			String internalTypeName = qualifiedType.getFullyQualifiedName().replace('.', '/');
			String mappedClassName = mappings.getMappedClassName(internalTypeName);
			if (mappedClassName != null)
				javaType = qualifiedType.withFullyQualifiedName(mappedClassName.replace('/', '.'));
		} else if (javaType instanceof JavaType.Array array) {
			// Update array references
			JavaType visitedElementType = visitType(array.getElemType(), ctx);
			if (visitedElementType != null && visitedElementType != array.getElemType())
				javaType = array.withElemType(visitedElementType);
		} else if (javaType instanceof JavaType.Method method) {
			// Update method types
			JavaType visitedReturnType = visitType(method.getReturnType(), ctx);
			if (visitedReturnType != null && visitedReturnType != method.getReturnType())
				method = method.withReturnType(visitedReturnType);

			// Update arguments
			boolean dirty = false;
			List<JavaType> parameterTypes = method.getParameterTypes();
			for (int i = 0; i < parameterTypes.size(); i++) {
				JavaType parameterType = parameterTypes.get(i);
				JavaType visited = visitType(parameterType, ctx);
				if (visited != parameterType) {
					parameterTypes.set(i, visited);
					dirty = true;
				}
			}
			if (dirty)
				method = method.withParameterTypes(parameterTypes);

			// Update thrown types
			dirty = false;
			List<JavaType.FullyQualified> thrownExceptions = method.getThrownExceptions();
			for (int i = 0; i < thrownExceptions.size(); i++) {
				JavaType.FullyQualified thrownType = thrownExceptions.get(i);
				JavaType.FullyQualified visited = (JavaType.FullyQualified) visitType(thrownType, ctx);
				if (visited != thrownType) {
					thrownExceptions.set(i, visited);
					dirty = true;
				}
			}
			if (dirty)
				method = method.withParameterTypes(parameterTypes);

			// Update annotations
			dirty = false;
			List<JavaType.FullyQualified> annotations = method.getAnnotations();
			for (int i = 0; i < annotations.size(); i++) {
				JavaType.FullyQualified annotationType = annotations.get(i);
				JavaType.FullyQualified visited = (JavaType.FullyQualified) visitType(annotationType, ctx);
				if (visited != annotationType) {
					annotations.set(i, visited);
					dirty = true;
				}
			}
			if (dirty)
				method = method.withParameterTypes(parameterTypes);

			// Update reference
			javaType = method;
		} else if (javaType instanceof JavaType.Variable variableType) {
			// Update variable type
			JavaType type = variableType.getType();
			JavaType visited = visitType(type, ctx);
			if (visited != null && visited != type)
				variableType = variableType.withType(visited);

			// Update variable names
			/*
			// TODO: Support mapping member references (will probably end up having side-effects in other overrides)
			String internalType = toInternal(type);
			String name = variableType.getName();
			JavaType owner = variableType.getOwner();
			if (owner != null) {
				String mappedName = mappings.getMappedFieldName(toInternal(owner), name, internalType);
				if (mappedName != null)
					variableType = variableType.withName(mappedName);
			}
			 */

			javaType = variableType;
		}
		return super.visitType(javaType, ctx);
	}
}
