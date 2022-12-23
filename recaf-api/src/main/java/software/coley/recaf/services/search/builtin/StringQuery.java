package software.coley.recaf.services.search.builtin;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.slf4j.Logger;
import software.coley.recaf.RecafConstants;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.info.FileInfo;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.info.annotation.BasicAnnotationInfo;
import software.coley.recaf.info.member.FieldMember;
import software.coley.recaf.info.member.MethodMember;
import software.coley.recaf.services.search.FileQuery;
import software.coley.recaf.services.search.FileSearchVisitor;
import software.coley.recaf.services.search.JvmClassQuery;
import software.coley.recaf.services.search.JvmClassSearchVisitor;
import software.coley.recaf.services.search.result.*;
import software.coley.recaf.util.TextMatchMode;

import java.util.function.BiConsumer;

/**
 * String search implementation.
 *
 * @author Matt Coley
 */
public class StringQuery implements JvmClassQuery, FileQuery {
	// TODO: Implement android query when android capabilities are fleshed out enough to have comparable
	//    search capabilities in method code
	private final TextMatchMode matchMode;
	private final String target;

	/**
	 * @param matchMode
	 * 		Text matching mode.
	 * @param target
	 * 		Text to match against.
	 */
	public StringQuery(@Nonnull TextMatchMode matchMode, @Nonnull String target) {
		this.matchMode = matchMode;
		this.target = target;
	}

	private boolean isMatch(Object value) {
		if (value instanceof String)
			return isMatch((String) value);
		return false;
	}

	private boolean isMatch(String text) {
		return matchMode.match(target, text);
	}

	@Nonnull
	@Override
	public JvmClassSearchVisitor visitor(@Nullable JvmClassSearchVisitor delegate) {
		return new JvmVisitor(delegate);
	}

	@Nonnull
	@Override
	public FileSearchVisitor visitor(@Nullable FileSearchVisitor delegate) {
		return new FileVisitor(delegate);
	}

	/**
	 * Points {@link #visitor(FileSearchVisitor)} to file content.
	 */
	private class FileVisitor implements FileSearchVisitor {
		private final FileSearchVisitor delegate;

		private FileVisitor(FileSearchVisitor delegate) {
			this.delegate = delegate;
		}

		@Override
		public void visit(@Nonnull BiConsumer<Location, Object> resultSink,
						  @Nonnull FileLocation currentLocation,
						  @Nonnull FileInfo fileInfo) {
			if (delegate != null) delegate.visit(resultSink, currentLocation, fileInfo);

			// Search text files text content on a line by line basis
			if (fileInfo.isTextFile()) {
				String text = fileInfo.asTextFile().getText();

				// Split by single newline (including goofy carriage returns)
				String[] lines = text.split("\\r?\\n\\r?");
				for (int i = 0; i < lines.length; i++) {
					String lineText = lines[i];
					if (isMatch(lineText))
						resultSink.accept(currentLocation.withTextLineNumber(i + 1), lineText);
				}
			}
		}
	}

	/**
	 * Points {@link #visitor(JvmClassSearchVisitor)} to {@link AsmClassTextVisitor}
	 */
	private class JvmVisitor implements JvmClassSearchVisitor {
		private final JvmClassSearchVisitor delegate;

		private JvmVisitor(@Nullable JvmClassSearchVisitor delegate) {
			this.delegate = delegate;
		}

		@Override
		public void visit(@Nonnull BiConsumer<Location, Object> resultSink,
						  @Nonnull JvmClassLocation currentLocation,
						  @Nonnull JvmClassInfo classInfo) {
			if (delegate != null) delegate.visit(resultSink, currentLocation, classInfo);

			classInfo.getClassReader().accept(new AsmClassTextVisitor(resultSink, currentLocation, classInfo), 0);
		}
	}

	/**
	 * Visits text in classes.
	 */
	private class AsmClassTextVisitor extends ClassVisitor {
		private final Logger logger = Logging.get(AsmClassTextVisitor.class);
		private final BiConsumer<Location, Object> resultSink;
		private final JvmClassLocation currentLocation;
		private final JvmClassInfo classInfo;

		protected AsmClassTextVisitor(@Nonnull BiConsumer<Location, Object> resultSink,
									  @Nonnull JvmClassLocation currentLocation,
									  @Nonnull JvmClassInfo classInfo) {
			super(RecafConstants.getAsmVersion());
			this.resultSink = resultSink;
			this.currentLocation = currentLocation;
			this.classInfo = classInfo;
		}

		@Override
		public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
			FieldVisitor fv = super.visitField(access, name, desc, signature, value);
			if (isMatch(value))
				resultSink.accept(currentLocation, value);
			FieldMember fieldMember = classInfo.getDeclaredField(name, desc);
			if (fieldMember != null) {
				return new AsmFieldTextVisitor(fv, fieldMember, resultSink, currentLocation);
			} else {
				logger.error("Failed to lookup field for query: {}.{} {}", classInfo.getName(), name, desc);
				return fv;
			}
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
			MethodMember methodMember = classInfo.getDeclaredMethod(name, desc);
			if (methodMember != null) {
				return new AsmMethodTextVisitor(mv, methodMember, resultSink, currentLocation);
			} else {
				logger.error("Failed to lookup method for query: {}.{}{}", classInfo.getName(), name, desc);
				return mv;
			}
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			AnnotationVisitor av = super.visitAnnotation(desc, visible);
			return new AnnotationTextVisitor(av, visible, resultSink,
					currentLocation.withAnnotation(new BasicAnnotationInfo(visible, desc)));
		}

		@Override
		public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
			AnnotationVisitor av = super.visitTypeAnnotation(typeRef, typePath, desc, visible);
			return new AnnotationTextVisitor(av, visible, resultSink,
					currentLocation.withAnnotation(new BasicAnnotationInfo(visible, desc)));
		}
	}

	/**
	 * Visits text in fields.
	 */
	private class AsmFieldTextVisitor extends FieldVisitor {
		private final BiConsumer<Location, Object> resultSink;
		private final MemberDeclarationLocation currentLocation;

		public AsmFieldTextVisitor(@Nullable FieldVisitor delegate,
								   @Nonnull FieldMember fieldMember,
								   @Nonnull BiConsumer<Location, Object> resultSink,
								   @Nonnull JvmClassLocation classLocation) {
			super(RecafConstants.getAsmVersion(), delegate);
			this.resultSink = resultSink;
			this.currentLocation = classLocation.withMember(fieldMember);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			AnnotationVisitor av = super.visitAnnotation(desc, visible);
			return new AnnotationTextVisitor(av, visible, resultSink,
					currentLocation.withAnnotation(new BasicAnnotationInfo(visible, desc)));
		}

		@Override
		public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
			AnnotationVisitor av = super.visitTypeAnnotation(typeRef, typePath, desc, visible);
			return new AnnotationTextVisitor(av, visible, resultSink,
					currentLocation.withAnnotation(new BasicAnnotationInfo(visible, desc)));
		}
	}

	/**
	 * Visits text in methods.
	 */
	private class AsmMethodTextVisitor extends MethodVisitor {
		private final BiConsumer<Location, Object> resultSink;
		private final MemberDeclarationLocation currentLocation;

		public AsmMethodTextVisitor(@Nullable MethodVisitor delegate,
									@Nonnull MethodMember methodMember,
									@Nonnull BiConsumer<Location, Object> resultSink,
									@Nonnull JvmClassLocation classLocation) {
			super(RecafConstants.getAsmVersion(), delegate);
			this.resultSink = resultSink;
			this.currentLocation = classLocation.withMember(methodMember);
		}

		@Override
		public void visitInvokeDynamicInsn(String name, String desc, Handle bsmHandle,
										   Object... bsmArgs) {
			super.visitInvokeDynamicInsn(name, desc, bsmHandle, bsmArgs);
			for (Object bsmArg : bsmArgs) {
				if (isMatch(bsmArg)) {
					InvokeDynamicInsnNode indy = new InvokeDynamicInsnNode(name, desc, bsmHandle, bsmArgs);
					resultSink.accept(currentLocation.withInstruction(indy), bsmArg);
				}
			}
		}

		@Override
		public void visitLdcInsn(Object value) {
			super.visitLdcInsn(value);
			if (isMatch(value))
				resultSink.accept(currentLocation.withInstruction(new LdcInsnNode(value)), value);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			AnnotationVisitor av = super.visitAnnotation(desc, visible);
			return new AnnotationTextVisitor(av, visible, resultSink,
					currentLocation.withAnnotation(new BasicAnnotationInfo(visible, desc)));
		}

		@Override
		public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
			AnnotationVisitor av = super.visitTypeAnnotation(typeRef, typePath, desc, visible);
			return new AnnotationTextVisitor(av, visible, resultSink,
					currentLocation.withAnnotation(new BasicAnnotationInfo(visible, desc)));
		}

		@Override
		public AnnotationVisitor visitAnnotationDefault() {
			AnnotationVisitor av = super.visitAnnotationDefault();
			return new AnnotationTextVisitor(av, true, resultSink, currentLocation);
		}

		@Override
		public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
			AnnotationVisitor av = super.visitParameterAnnotation(parameter, desc, visible);
			return new AnnotationTextVisitor(av, visible, resultSink,
					currentLocation.withAnnotation(new BasicAnnotationInfo(visible, desc)));
		}

		@Override
		public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
			AnnotationVisitor av = super.visitInsnAnnotation(typeRef, typePath, desc, visible);
			return new AnnotationTextVisitor(av, visible, resultSink,
					currentLocation.withAnnotation(new BasicAnnotationInfo(visible, desc)));
		}

		@Override
		public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc,
														 boolean visible) {
			AnnotationVisitor av = super.visitTryCatchAnnotation(typeRef, typePath, desc, visible);
			return new AnnotationTextVisitor(av, visible, resultSink,
					currentLocation.withAnnotation(new BasicAnnotationInfo(visible, desc)));
		}

		@Override
		public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath,
															  Label[] start, Label[] end, int[] index,
															  String desc, boolean visible) {
			AnnotationVisitor av = super.visitLocalVariableAnnotation(typeRef, typePath, start, end,
					index, desc, visible);
			return new AnnotationTextVisitor(av, visible, resultSink,
					currentLocation.withAnnotation(new BasicAnnotationInfo(visible, desc)));
		}
	}

	/**
	 * Visits text in annotations.
	 */
	private class AnnotationTextVisitor extends AnnotationVisitor {
		private final BiConsumer<Location, Object> resultSink;
		private final AnnotatableLocation currentAnnoLocation;
		private final boolean visible;

		public AnnotationTextVisitor(@Nullable AnnotationVisitor delegate,
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
			return new AnnotationTextVisitor(av, visible, resultSink,
					currentAnnoLocation.withAnnotation(new BasicAnnotationInfo(visible, descriptor)));
		}

		@Override
		public AnnotationVisitor visitArray(String name) {
			AnnotationVisitor av = super.visitArray(name);
			return new AnnotationTextVisitor(av, visible, resultSink, currentAnnoLocation);
		}

		@Override
		public void visit(String name, Object value) {
			super.visit(name, value);
			if (isMatch(value))
				resultSink.accept(currentAnnoLocation, value);
		}
	}
}
