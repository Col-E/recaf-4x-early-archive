package software.coley.recaf.services.search.result;

import jakarta.annotation.Nonnull;
import software.coley.recaf.info.member.ClassMember;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

/**
 * Basic implementation of {@link MemberDeclarationLocation}.
 *
 * @author Matt Coley
 */
public class BasicMemberDeclarationLocation extends AbstractLocation implements MemberDeclarationLocation {
	private final ClassMember member;
	private final Location parent;

	/**
	 * @param workspace
	 * 		Target workspace.
	 * @param resource
	 * 		Target resource.
	 * @param parent
	 * 		Parent location context.
	 * @param member
	 * 		Target member containing the result.
	 */
	public BasicMemberDeclarationLocation(@Nonnull Workspace workspace, @Nonnull WorkspaceResource resource,
										  @Nonnull Location parent, @Nonnull ClassMember member) {
		super(workspace, resource);
		this.member = member;
		this.parent = parent;
	}

	@Override
	public ClassMember getDeclaredMember() {
		return member;
	}

	@Nonnull
	@Override
	public Location getParent() {
		return parent;
	}

	@Nonnull
	@Override
	public String comparableString() {
		return getParent().comparableString() + " " + member.getName() + " " + member.getDescriptor();
	}
}
