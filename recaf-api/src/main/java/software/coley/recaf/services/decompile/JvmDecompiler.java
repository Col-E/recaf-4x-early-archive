package software.coley.recaf.services.decompile;

import jakarta.annotation.Nonnull;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.info.properties.builtin.CachedDecompileProperty;
import software.coley.recaf.workspace.model.Workspace;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Outline for JVM decompile capabilities.
 *
 * @author Matt Coley
 */
public abstract class JvmDecompiler extends AbstractDecompiler implements Decompiler {
	private final Set<JvmInputFilter> inputFilters = new HashSet<>();

	/**
	 * @param name
	 * 		Decompiler name.
	 * @param version
	 * 		Decompiler version.
	 */
	public JvmDecompiler(@Nonnull String name, @Nonnull String version) {
		super(name, version);
	}

	/**
	 * @param filter
	 * 		Filter to add.
	 */
	public void addJvmInputFilter(JvmInputFilter filter) {
		inputFilters.add(filter);
	}

	/**
	 * @param workspace
	 * 		Workspace to pull data from.
	 * @param classInfo
	 * 		Class to decompile.
	 *
	 * @return Future of decompilation result.
	 */
	public final CompletableFuture<DecompileResult> decompile(Workspace workspace, JvmClassInfo classInfo) {
		// Check for cached result, returning it if found.
		DecompileResult cachedResult = CachedDecompileProperty.get(classInfo, this);
		if (cachedResult != null)
			return CompletableFuture.completedFuture(cachedResult);

		// Get bytecode and run through filters.
		byte[] bytecode = classInfo.getBytecode();
		for (JvmInputFilter filter : inputFilters)
			bytecode = filter.filter(bytecode);

		// Pass to implementation.
		return decompile(workspace, classInfo.getName(), bytecode).thenApply(r -> {
			// Cache result so later runs do not need to redo work.
			CachedDecompileProperty.set(classInfo, this, r);
			return r;
		});
	}

	/**
	 * @param workspace
	 * 		Workspace to pull data from.
	 * @param name
	 * 		Class name.
	 * @param bytecode
	 * 		Class bytecode.
	 *
	 * @return Future of decompilation result.
	 */
	protected abstract CompletableFuture<DecompileResult> decompile(Workspace workspace, String name, byte[] bytecode);

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		JvmDecompiler that = (JvmDecompiler) o;

		return inputFilters.equals(that.inputFilters);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + inputFilters.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "JvmDecompiler: " + getName() + " - " + getVersion();
	}
}
