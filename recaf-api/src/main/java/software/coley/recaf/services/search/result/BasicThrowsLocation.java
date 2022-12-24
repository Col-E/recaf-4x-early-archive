package software.coley.recaf.services.search.result;

import jakarta.annotation.Nonnull;

/**
 * Basic implementation of {@link ThrowsLocation}.
 *
 * @author Matt Coley
 */
public class BasicThrowsLocation extends AbstractLocation implements ThrowsLocation {
	private final MemberDeclarationLocation parent;
	private final String thrownException;

	protected BasicThrowsLocation(@Nonnull String thrownException,
								  @Nonnull MemberDeclarationLocation parent) {
		super(parent.getContainingWorkspace(), parent.getContainingResource());
		this.thrownException = thrownException;
		this.parent = parent;
	}

	@Nonnull
	@Override
	public String comparableString() {
		return parent.comparableString() + " throws " + thrownException;
	}

	@Nonnull
	@Override
	public Location getParent() {
		return parent;
	}

	@Override
	public String getThrownException() {
		return thrownException;
	}
}
