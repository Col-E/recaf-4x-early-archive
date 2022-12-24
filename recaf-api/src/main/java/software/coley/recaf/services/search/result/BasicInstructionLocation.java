package software.coley.recaf.services.search.result;

import jakarta.annotation.Nonnull;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Textifier;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Basic implementation of {@link InstructionLocation}.
 *
 * @author Matt Coley
 */
public class BasicInstructionLocation extends AbstractLocation implements InstructionLocation {
	private final AbstractInsnNode instruction;
	private final MemberDeclarationLocation parent;

	public BasicInstructionLocation(@Nonnull AbstractInsnNode instruction,
									@Nonnull MemberDeclarationLocation parent) {
		super(parent.getContainingWorkspace(), parent.getContainingResource());
		this.instruction = instruction;
		this.parent = parent;
	}

	@Override
	public AbstractInsnNode getInstruction() {
		return instruction;
	}

	@Nonnull
	@Override
	public String comparableString() {
		// The instruction hashcode is added to disambiguate between instructions at different
		// offsets, since ASM instructions hash is not based on content value and is based on identity.
		return getParent().comparableString() + " insn " + toString(instruction)
				+ "[" + instruction.hashCode() + "]";
	}

	@Nonnull
	@Override
	public Location getParent() {
		return parent;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		BasicInstructionLocation that = (BasicInstructionLocation) o;

		if (!instruction.equals(that.instruction)) return false;
		return parent.equals(that.parent);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + instruction.hashCode();
		result = 31 * result + parent.hashCode();
		return result;
	}

	private static String toString(AbstractInsnNode instruction) {
		int opcode = instruction.getOpcode();
		Textifier textifier = new Textifier();
		switch (instruction.getType()) {
			case AbstractInsnNode.INSN:
				textifier.visitInsn(opcode);
				break;
			case AbstractInsnNode.INT_INSN:
				IntInsnNode intInsnNode = (IntInsnNode) instruction;
				textifier.visitIntInsn(opcode, intInsnNode.operand);
				break;
			case AbstractInsnNode.VAR_INSN:
				VarInsnNode varInsnNode = (VarInsnNode) instruction;
				textifier.visitVarInsn(opcode, varInsnNode.var);
				break;
			case AbstractInsnNode.TYPE_INSN:
				TypeInsnNode typeInsnNode = (TypeInsnNode) instruction;
				textifier.visitTypeInsn(opcode, typeInsnNode.desc);
				break;
			case AbstractInsnNode.FIELD_INSN:
				FieldInsnNode fieldInsnNode = (FieldInsnNode) instruction;
				textifier.visitFieldInsn(opcode, fieldInsnNode.owner, fieldInsnNode.name, fieldInsnNode.desc);
				break;
			case AbstractInsnNode.METHOD_INSN:
				MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
				textifier.visitMethodInsn(opcode, methodInsnNode.owner, methodInsnNode.name,
						methodInsnNode.desc, methodInsnNode.itf);
				break;
			case AbstractInsnNode.INVOKE_DYNAMIC_INSN:
				InvokeDynamicInsnNode invokeDynamicInsnNode = (InvokeDynamicInsnNode) instruction;
				textifier.visitInvokeDynamicInsn(invokeDynamicInsnNode.name, invokeDynamicInsnNode.desc,
						invokeDynamicInsnNode.bsm, invokeDynamicInsnNode.bsmArgs);
				break;
			case AbstractInsnNode.JUMP_INSN:
				JumpInsnNode jumpInsnNode = (JumpInsnNode) instruction;
				textifier.visitJumpInsn(opcode, jumpInsnNode.label.getLabel());
				break;
			case AbstractInsnNode.LABEL:
				LabelNode labelNode = (LabelNode) instruction;
				textifier.visitLabel(labelNode.getLabel());
				break;
			case AbstractInsnNode.LDC_INSN:
				LdcInsnNode ldcInsnNode = (LdcInsnNode) instruction;
				textifier.visitLdcInsn(ldcInsnNode.cst);
				break;
			case AbstractInsnNode.IINC_INSN:
				IincInsnNode iincInsnNode = (IincInsnNode) instruction;
				textifier.visitIincInsn(iincInsnNode.var, iincInsnNode.incr);
				break;
			case AbstractInsnNode.TABLESWITCH_INSN:
				TableSwitchInsnNode tableSwitchInsnNode = (TableSwitchInsnNode) instruction;
				textifier.visitTableSwitchInsn(tableSwitchInsnNode.min, tableSwitchInsnNode.max,
						tableSwitchInsnNode.dflt.getLabel(),
						(Label[]) tableSwitchInsnNode.labels.stream().map(LabelNode::getLabel).toArray());
				break;
			case AbstractInsnNode.LOOKUPSWITCH_INSN:
				LookupSwitchInsnNode lookupSwitchInsnNode = (LookupSwitchInsnNode) instruction;
				textifier.visitLookupSwitchInsn(lookupSwitchInsnNode.dflt.getLabel(),
						lookupSwitchInsnNode.keys.stream().mapToInt(i -> i).toArray(),
						(Label[]) lookupSwitchInsnNode.labels.stream().map(LabelNode::getLabel).toArray());
				break;
			case AbstractInsnNode.MULTIANEWARRAY_INSN:
				MultiANewArrayInsnNode multiANewArrayInsnNode = (MultiANewArrayInsnNode) instruction;
				textifier.visitMultiANewArrayInsn(multiANewArrayInsnNode.desc, multiANewArrayInsnNode.dims);
				break;
			default:
				throw new UnsupportedOperationException("Unsupported instruction: " + instruction);
		}
		StringWriter writer = new StringWriter();
		textifier.print(new PrintWriter(writer));
		return writer.toString().trim();
	}
}
