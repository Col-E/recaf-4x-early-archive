package software.coley.recaf.services.phantom;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import software.coley.recaf.TestBase;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.info.builder.JvmClassInfoBuilder;
import software.coley.recaf.info.member.MethodMember;
import software.coley.recaf.workspace.model.EmptyWorkspace;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PhantomGenerator}.
 */
public class PhantomGeneratorTest extends TestBase implements Opcodes {
	private static PhantomGenerator generator;

	@BeforeAll
	static void setup() {
		generator = recaf.get(PhantomGenerator.class);
	}

	@Test
	void test() {
		// Make a dummy ctor to point to ClassDoesNotExist.<init> and a method in InterfaceDoesNotExist.
		ClassNode node = new ClassNode();
		node.visit(V11, ACC_PUBLIC | ACC_ABSTRACT, "Example", null, "ClassDoesNotExist", new String[]{"InterfaceDoesNotExist"});
		MethodNode constructor = new MethodNode(ACC_PUBLIC, "<init>", "()V", null, null);
		constructor.visitVarInsn(ALOAD, 0);
		constructor.visitMethodInsn(INVOKESPECIAL, "ClassDoesNotExist", "<init>", "()V", false);
		constructor.visitVarInsn(ALOAD, 0);
		constructor.visitMethodInsn(INVOKEINTERFACE, "InterfaceDoesNotExist", "doSomething", "()V", false);
		constructor.visitInsn(RETURN);
		constructor.visitMaxs(1, 1);
		node.methods.add(constructor);

		// Create the class
		ClassWriter writer = new ClassWriter(0);
		node.accept(writer);
		byte[] dummy = writer.toByteArray();
		JvmClassInfo dummyInfo = new JvmClassInfoBuilder(new ClassReader(dummy)).build();
		List<JvmClassInfo> dummyWrapped = Collections.singletonList(dummyInfo);

		// Generate phantoms
		try {
			WorkspaceResource phantoms = generator.createPhantomsForClasses(EmptyWorkspace.get(), dummyWrapped);
			JvmClassBundle phantomBundle = phantoms.getJvmClassBundle();
			JvmClassInfo cdnePhantom = phantomBundle.get("ClassDoesNotExist");
			JvmClassInfo idnePhantom = phantomBundle.get("InterfaceDoesNotExist");
			assertNotNull(cdnePhantom, "Missing phantom: ClassDoesNotExist");
			assertNotNull(idnePhantom, "Missing phantom: InterfaceDoesNotExist");
			assertFalse(cdnePhantom.hasInterfaceModifier());
			assertTrue(idnePhantom.hasInterfaceModifier());
			MethodMember cdneCtor = cdnePhantom.getDeclaredMethod("<init>", "()V");
			MethodMember idneDoSomething = idnePhantom.getDeclaredMethod("doSomething", "()V");
			assertNotNull(cdneCtor, "Missing phantom: ClassDoesNotExist.<init>");
			assertNotNull(idneDoSomething, "Missing phantom: InterfaceDoesNotExist.doSomething");
		} catch (PhantomGenerationFailure ex) {
			fail(ex);
		}
	}
}
