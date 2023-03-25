package software.coley.recaf.services.source;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.openrewrite.Tree;
import org.openrewrite.java.tree.J;
import software.coley.recaf.path.DirectoryPathNode;
import software.coley.recaf.path.PathNode;
import software.coley.recaf.workspace.model.Workspace;

import java.util.List;

/**
 * Helper that can link text at some given offset within a source <i>(given as a {@link J.CompilationUnit})</i>
 * to content in a workspace.
 *
 * @author Matt Coley
 */
public class AstContextHelper {
	private final Workspace workspace;

	/**
	 * @param workspace
	 * 		Workspace to pull data from.
	 */
	public AstContextHelper(@Nonnull Workspace workspace) {
		this.workspace = workspace;
	}

	/**
	 * @param unit
	 * 		Compilation unit to look at.
	 * @param offset
	 * 		Offset in the source <i>(Assuming from the {@link String} the unit originates from)</i> resolve.
	 *
	 * @return Resolved content at the given offset, or {@code null} if no resolution could be made.
	 */
	@Nullable
	public PathNode<?> resolve(@Nonnull J.CompilationUnit unit, int offset) {
		List<Tree> astPath = AstUtils.getAstPathAtOffset(offset, unit);

		// If no AST path was found, we have no clue.
		if (astPath.isEmpty())
			return null;

		// Iterate over path, checking if we can resolve some reference to a type/member/package.
		// First items are the most specific, thus yielding the 'best' results.
		for (Tree ast : astPath) {
			if (ast instanceof J.Package packageAst) {
				String packageName = packageAst.getPackageName().replace('.', '/');
				DirectoryPathNode packagePath = workspace.findPackage(packageName);
				if (packagePath != null)
					return packagePath;
			}
			// TODO: Other cases
		}

		// Unknown.
		return null;
	}
}
