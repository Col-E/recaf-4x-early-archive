package software.coley.recaf.services.search.result;

import jakarta.annotation.Nonnull;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Basic implementation of {@link InstructionLocation}.
 *
 * @author Matt Coley
 */
public class BasicInstructionValue extends AbstractLocation implements InstructionLocation {
	private final AbstractInsnNode instruction;
	private final MemberDeclarationLocation parent;

	public BasicInstructionValue(@Nonnull AbstractInsnNode instruction, @Nonnull MemberDeclarationLocation parent) {
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
		return getParent().comparableString() + ":" + instruction;
	}

	@Nonnull
	@Override
	public Location getParent() {
		return parent;
	}
}
