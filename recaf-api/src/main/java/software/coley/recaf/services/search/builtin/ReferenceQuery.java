package software.coley.recaf.services.search.builtin;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.slf4j.Logger;
import software.coley.recaf.RecafConstants;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.info.annotation.BasicAnnotationInfo;
import software.coley.recaf.info.member.MethodMember;
import software.coley.recaf.services.search.JvmClassQuery;
import software.coley.recaf.services.search.JvmClassSearchVisitor;
import software.coley.recaf.services.search.result.*;
import software.coley.recaf.util.StringUtil;
import software.coley.recaf.util.TextMatchMode;
import software.coley.recaf.util.Types;

import java.util.function.BiConsumer;

/**
 * Reference search implementation.
 *
 * @author Matt Coley
 */
public class ReferenceQuery implements JvmClassQuery {
	private final TextMatchMode matchMode;
	private final String targetOwner;
	private final String targetName;
	private final String targetDesc;
	private final boolean classRefOnly;

	/**
	 * Class reference query.
	 *
	 * @param matchMode
	 * 		Match mode for text comparison.
	 * @param targetOwner
	 * 		Name to search for.
	 */
	public ReferenceQuery(@Nonnull TextMatchMode matchMode,
						  @Nullable String targetOwner) {
		this.matchMode = matchMode;
		this.targetOwner = targetOwner;
		this.targetName = null;
		this.targetDesc = null;
		classRefOnly = true;
	}

	/**
	 * Member reference query.
	 * <br>
	 * Do note that each target value is nullable/optional.
	 * Including only the owner and {@code null} for the name/desc will yield references to all members in the class.
	 * Including only the desc will yield references to all members of that desc in all classes.
	 *
	 * @param matchMode
	 * 		Match mode for text comparison.
	 * @param targetOwner
	 * 		Declaring class of target reference.
	 * @param targetName
	 * 		Member name of target reference.
	 * @param targetDesc
	 * 		Member descriptor of target reference.
	 */
	public ReferenceQuery(@Nonnull TextMatchMode matchMode,
						  @Nullable String targetOwner, @Nullable String targetName, @Nullable String targetDesc) {
		this.matchMode = matchMode;
		this.targetOwner = targetOwner;
		this.targetName = targetName;
		this.targetDesc = targetDesc;
		classRefOnly = false;
	}

	private boolean isClassRefMatch(String className) {
		if (!classRefOnly) return false;
		if (className == null && targetName != null) return false;
		return StringUtil.isAnyNullOrEmpty(targetOwner, className) || matchMode.match(targetOwner, className);
	}

	private boolean isMemberRefMatch(String owner, String name, String desc) {
		if (classRefOnly) return false;

		// The parameters are null if we only are searching against a type.
		// In these cases since we're comparing to a type, then any name/desc comparison should be ignored.
		if (name == null && targetName != null) return false;
		if (desc == null && targetDesc != null) return false;

		// Check if match modes succeed.
		// If our query arguments are null, that field can skip comparison, and we move on to the next.
		// If all of our non-null query arguments match the given parameters, we have a match.
		if (StringUtil.isAnyNullOrEmpty(targetOwner, owner) || matchMode.match(targetOwner, owner))
			if (StringUtil.isAnyNullOrEmpty(targetName, name) || matchMode.match(targetName, name))
				return StringUtil.isAnyNullOrEmpty(targetDesc, desc) || matchMode.match(targetDesc, desc);

		return false;
	}

	private static String getInternalName(String classDesc) {
		return Type.getType(classDesc).getInternalName();
	}

	private static ClassReferenceResult.ClassReference cref(String name) {
		return new ClassReferenceResult.ClassReference(name);
	}

	private static MemberReferenceResult.MemberReference mref(String owner, String name, String desc) {
		return new MemberReferenceResult.MemberReference(owner, name, desc);
	}

	@Nonnull
	@Override
	public JvmClassSearchVisitor visitor(@Nullable JvmClassSearchVisitor delegate) {
		return (resultSink, currentLocation, classInfo) -> {
			if (delegate != null)
				delegate.visit(resultSink, currentLocation, classInfo);
			classInfo.getClassReader().accept(new AsmReferenceClassVisitor(resultSink, currentLocation, classInfo), 0);
		};
	}

	/**
	 * Visits references in classes.
	 */
	private class AsmReferenceClassVisitor extends ClassVisitor {
		private final Logger logger = Logging.get(AsmReferenceClassVisitor.class);
		private final BiConsumer<Location, Object> resultSink;
		private final JvmClassLocation currentLocation;
		private final JvmClassInfo classInfo;

		public AsmReferenceClassVisitor(@Nonnull BiConsumer<Location, Object> resultSink,
										@Nonnull JvmClassLocation currentLocation,
										@Nonnull JvmClassInfo classInfo) {
			super(RecafConstants.getAsmVersion());
			this.resultSink = resultSink;
			this.currentLocation = currentLocation;
			this.classInfo = classInfo;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
			MethodMember methodMember = classInfo.getDeclaredMethod(name, desc);
			if (methodMember != null) {
				// Check exceptions
				if (exceptions != null) {
					MemberDeclarationLocation location = currentLocation.withMember(methodMember);
					for (String exception : exceptions)
						resultSink.accept(location.withThrownException(exception),
								cref(exception));
				}

				// Visit method
				return new AsmReferenceMethodVisitor(mv, methodMember, resultSink, currentLocation);
			} else {
				logger.error("Failed to lookup method for query: {}.{}{}", classInfo.getName(), name, desc);
				return mv;
			}
		}
	}

	/**
	 * Visits references in methods.
	 */
	private class AsmReferenceMethodVisitor extends MethodVisitor {
		private final BiConsumer<Location, Object> resultSink;
		private final MemberDeclarationLocation currentLocation;

		public AsmReferenceMethodVisitor(@Nullable MethodVisitor delegate,
										 @Nonnull MethodMember methodMember,
										 @Nonnull BiConsumer<Location, Object> resultSink,
										 @Nonnull JvmClassLocation classLocation) {
			super(RecafConstants.getAsmVersion(), delegate);
			this.resultSink = resultSink;
			this.currentLocation = classLocation.withMember(methodMember);
		}

		@Override
		public void visitTypeInsn(int opcode, String type) {
			if (isClassRefMatch(type)) {
				TypeInsnNode insn = new TypeInsnNode(opcode, type);
				resultSink.accept(currentLocation.withInstruction(insn), cref(type));
			}
			super.visitTypeInsn(opcode, type);
		}

		@Override
		public void visitFieldInsn(int opcode, String owner, String name, String desc) {
			FieldInsnNode insn = new FieldInsnNode(opcode, owner, name, desc);

			// Check method ref
			if (isMemberRefMatch(owner, name, desc))
				resultSink.accept(currentLocation.withInstruction(insn), mref(owner, name, desc));

			// Check types used in ref
			String fieldType = getInternalName(desc);
			if (isClassRefMatch(fieldType))
				resultSink.accept(currentLocation.withInstruction(insn), cref(fieldType));

			super.visitFieldInsn(opcode, owner, name, desc);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface) {
			MethodInsnNode insn = new MethodInsnNode(opcode, owner, name, desc, isInterface);

			// Check method ref
			if (isMemberRefMatch(owner, name, desc))
				resultSink.accept(currentLocation.withInstruction(insn), mref(owner, name, desc));

			// Check types used in ref
			Type methodType = Type.getMethodType(desc);
			String methodRetType = methodType.getReturnType().getInternalName();
			if (isClassRefMatch(methodRetType))
				resultSink.accept(currentLocation.withInstruction(insn), cref(methodRetType));
			for (Type argumentType : methodType.getArgumentTypes()) {
				if (isClassRefMatch(argumentType.getInternalName()))
					resultSink.accept(currentLocation.withInstruction(insn), cref(argumentType.getInternalName()));
			}

			super.visitMethodInsn(opcode, owner, name, desc, isInterface);
		}

		@Override
		public void visitInvokeDynamicInsn(String name, String desc, Handle bsmHandle, Object... bsmArgs) {
			super.visitInvokeDynamicInsn(name, desc, bsmHandle, bsmArgs);
		}

		@Override
		public void visitLdcInsn(Object value) {
			LdcInsnNode insn = new LdcInsnNode(value);

			if (value instanceof Handle) {
				Handle handle = (Handle) value;

				// Check handle ref
				if (isMemberRefMatch(handle.getOwner(), handle.getName(), handle.getDesc())) {
					resultSink.accept(currentLocation.withInstruction(insn),
							mref(handle.getOwner(), handle.getName(), handle.getDesc()));
				}

				// Check types used in ref
				Type methodType = Type.getMethodType(handle.getDesc());
				String methodRetType = methodType.getReturnType().getInternalName();
				if (isClassRefMatch(methodRetType))
					resultSink.accept(currentLocation.withInstruction(insn), cref(methodRetType));
				for (Type argumentType : methodType.getArgumentTypes()) {
					if (isClassRefMatch(argumentType.getInternalName()))
						resultSink.accept(currentLocation.withInstruction(insn), cref(argumentType.getInternalName()));
				}
			} else if (value instanceof Type) {
				String type = ((Type) value).getInternalName();
				if (isClassRefMatch(type)) {
					resultSink.accept(currentLocation.withInstruction(insn), cref(type));
				}
			}
			super.visitLdcInsn(value);
		}

		@Override
		public void visitMultiANewArrayInsn(String desc, int numDimensions) {
			if (Types.isValidDesc(desc)) {
				String type = getInternalName(desc);
				if (isClassRefMatch(type)) {
					MultiANewArrayInsnNode insn = new MultiANewArrayInsnNode(desc, numDimensions);
					resultSink.accept(currentLocation.withInstruction(insn), cref(type));
				}
			}
			super.visitMultiANewArrayInsn(desc, numDimensions);
		}

		@Override
		public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
			if (isClassRefMatch(type)) {
				TryCatchBlockNode catchBlock
						= new TryCatchBlockNode(new LabelNode(start), new LabelNode(end), new LabelNode(handler), type);
				resultSink.accept(currentLocation.withCatchBlock(catchBlock), cref(type));
			}
			super.visitTryCatchBlock(start, end, handler, type);
		}

		@Override
		public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
			if (Types.isValidDesc(desc) && !Types.isPrimitive(desc)) {
				String type = getInternalName(desc);
				if (isClassRefMatch(type)) {
					LocalVariableNode variable = new LocalVariableNode(name, desc, signature,
							new LabelNode(start), new LabelNode(end), index);
					resultSink.accept(currentLocation.withLocalVariable(variable), cref(type));
				}
			}
			super.visitLocalVariable(name, desc, signature, start, end, index);

		}

		@Override
		public AnnotationVisitor visitAnnotationDefault() {
			return new AnnotationReferenceVisitor(super.visitAnnotationDefault(), true, resultSink, currentLocation);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			// Match annotation
			String type = getInternalName(desc);
			if (isClassRefMatch(type))
				resultSink.accept(currentLocation, cref(type));

			AnnotationVisitor av = super.visitAnnotation(desc, visible);
			return new AnnotationReferenceVisitor(av, visible, resultSink, currentLocation);

		}

		@Override
		public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
			// Match annotation
			String type = getInternalName(desc);
			if (isClassRefMatch(type))
				resultSink.accept(currentLocation, cref(type));

			AnnotationVisitor av = super.visitTypeAnnotation(typeRef, typePath, desc, visible);
			return new AnnotationReferenceVisitor(av, visible, resultSink, currentLocation);
		}

		@Override
		public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
			// Match annotation
			String type = getInternalName(desc);
			if (isClassRefMatch(type))
				resultSink.accept(currentLocation, cref(type));

			AnnotationVisitor av = super.visitParameterAnnotation(parameter, desc, visible);
			return new AnnotationReferenceVisitor(av, visible, resultSink, currentLocation);
		}

		@Override
		public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
			// Match annotation
			String type = getInternalName(desc);
			if (isClassRefMatch(type))
				resultSink.accept(currentLocation, cref(type));

			AnnotationVisitor av = super.visitInsnAnnotation(typeRef, typePath, desc, visible);
			return new AnnotationReferenceVisitor(av, visible, resultSink, currentLocation);
		}

		@Override
		public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
			// Match annotation
			String type = getInternalName(desc);
			if (isClassRefMatch(type))
				resultSink.accept(currentLocation, cref(type));

			AnnotationVisitor av = super.visitTryCatchAnnotation(typeRef, typePath, desc, visible);
			return new AnnotationReferenceVisitor(av, visible, resultSink, currentLocation);
		}

		@Override
		public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible) {
			// Match annotation
			String type = getInternalName(desc);
			if (isClassRefMatch(type))
				resultSink.accept(currentLocation, cref(type));

			AnnotationVisitor av = super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
			return new AnnotationReferenceVisitor(av, visible, resultSink, currentLocation);
		}
	}

	/**
	 * Visits references in annotations.
	 */
	private class AnnotationReferenceVisitor extends AnnotationVisitor {
		private final BiConsumer<Location, Object> resultSink;
		private final AnnotatableLocation currentAnnoLocation;
		private final boolean visible;

		public AnnotationReferenceVisitor(@Nullable AnnotationVisitor delegate,
										  boolean visible,
										  @Nonnull BiConsumer<Location, Object> resultSink,
										  @Nonnull AnnotatableLocation currentAnnoLocation) {
			super(RecafConstants.getAsmVersion(), delegate);
			this.visible = visible;
			this.resultSink = resultSink;
			this.currentAnnoLocation = currentAnnoLocation;
		}

		@Override
		public AnnotationVisitor visitAnnotation(String name, String descriptor) {
			AnnotationVisitor av = super.visitAnnotation(name, descriptor);

			// Match sub-annotation
			String type = getInternalName(descriptor);
			if (isClassRefMatch(type))
				resultSink.accept(currentAnnoLocation, cref(type));

			// Visit sub-annotation
			return new AnnotationReferenceVisitor(av, visible, resultSink,
					currentAnnoLocation.withAnnotation(new BasicAnnotationInfo(visible, descriptor)));
		}

		@Override
		public AnnotationVisitor visitArray(String name) {
			AnnotationVisitor av = super.visitArray(name);
			return new AnnotationReferenceVisitor(av, visible, resultSink, currentAnnoLocation);
		}

		@Override
		public void visitEnum(String name, String descriptor, String value) {
			super.visitEnum(name, descriptor, value);

			// Match enum reference
			String owner = getInternalName(descriptor);
			if (isMemberRefMatch(owner, descriptor, value))
				resultSink.accept(currentAnnoLocation, mref(owner, value, descriptor));
		}
	}
}
