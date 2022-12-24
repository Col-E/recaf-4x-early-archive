package software.coley.recaf.services.search.result;

import jakarta.annotation.Nonnull;
import org.objectweb.asm.tree.LocalVariableNode;

/**
 * Basic implementation of {@link LocalVariableLocation}.
 *
 * @author Matt Coley
 */
public class BasicLocalVariableLocation extends AbstractLocation implements LocalVariableLocation {
	private final MemberDeclarationLocation parent;
	private final LocalVariableNode variable;

	protected BasicLocalVariableLocation(@Nonnull LocalVariableNode variable,
										 @Nonnull MemberDeclarationLocation parent) {
		super(parent.getContainingWorkspace(), parent.getContainingResource());
		this.variable = variable;
		this.parent = parent;
	}

	@Nonnull
	@Override
	public String comparableString() {
		return parent.comparableString() + " var " + variable.desc + " " + variable.name;
	}

	@Nonnull
	@Override
	public Location getParent() {
		return parent;
	}

	@Override
	public LocalVariableNode getVariable() {
		return variable;
	}
}
