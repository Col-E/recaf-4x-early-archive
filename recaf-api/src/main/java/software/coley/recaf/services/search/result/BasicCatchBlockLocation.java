package software.coley.recaf.services.search.result;

import jakarta.annotation.Nonnull;
import org.objectweb.asm.tree.TryCatchBlockNode;

/**
 * Basic implementation of {@link LocalVariableLocation}.
 *
 * @author Matt Coley
 */
public class BasicCatchBlockLocation extends AbstractLocation implements CatchBlockLocation {
	private final MemberDeclarationLocation parent;
	private final TryCatchBlockNode catchBlock;

	protected BasicCatchBlockLocation(@Nonnull TryCatchBlockNode catchBlock,
									  @Nonnull MemberDeclarationLocation parent) {
		super(parent.getContainingWorkspace(), parent.getContainingResource());
		this.catchBlock = catchBlock;
		this.parent = parent;
	}

	@Nonnull
	@Override
	public String comparableString() {
		return parent.comparableString() + " catch " + catchBlock.type;
	}

	@Nonnull
	@Override
	public Location getParent() {
		return parent;
	}

	@Override
	public TryCatchBlockNode getCatchBlock() {
		return catchBlock;
	}
}
