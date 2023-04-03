package software.coley.recaf.services.source;

import jakarta.annotation.Nonnull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.marker.JavaSourceSet;
import org.openrewrite.java.tree.J;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

/**
 * Delegating implementation of {@link JavaParser}.
 *
 * @author Matt Coley
 */
public class DelegatingJavaParser implements JavaParser {
	private final JavaParser delegate;

	/**
	 * @param delegate
	 * 		Backing parser value.
	 */
	public DelegatingJavaParser(@Nonnull JavaParser delegate) {
		this.delegate = delegate;
	}

	@Nonnull
	@Override
	public List<J.CompilationUnit> parseInputs(@Nonnull Iterable<Input> sources,
											   @Nullable Path relativeTo,
											   @Nonnull ExecutionContext ctx) {
		// The default source-set type generation logic is not well optimized.
		// We also do not gain significant benefits from it, so we can skip it entirely.
		ctx.putMessage(SKIP_SOURCE_SET_TYPE_GENERATION, true);
		return delegate.parseInputs(sources, relativeTo, ctx);
	}

	@Nonnull
	@Override
	public JavaParser reset() {
		return delegate.reset();
	}

	@Nonnull
	@Override
	public JavaParser reset(@Nonnull Collection<URI> uris) {
		return delegate.reset(uris);
	}

	@Override
	public void setClasspath(@Nonnull Collection<Path> classpath) {
		delegate.setClasspath(classpath);
	}

	@Override
	public void setSourceSet(@Nonnull String sourceSet) {
		delegate.setSourceSet(sourceSet);
	}

	@Nonnull
	@Override
	public JavaSourceSet getSourceSet(@Nonnull ExecutionContext ctx) {
		return delegate.getSourceSet(ctx);
	}
}
