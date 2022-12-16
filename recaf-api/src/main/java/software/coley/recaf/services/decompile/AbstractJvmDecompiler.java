package software.coley.recaf.services.decompile;

import jakarta.annotation.Nonnull;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.info.properties.builtin.CachedDecompileProperty;
import software.coley.recaf.workspace.model.Workspace;

import java.util.HashSet;
import java.util.Set;

/**
 * Basic setup for {@link JvmDecompiler}.
 *
 * @author Matt Coley
 */
public abstract class AbstractJvmDecompiler extends AbstractDecompiler implements JvmDecompiler {
	private final Set<JvmInputFilter> inputFilters = new HashSet<>();

	/**
	 * @param name
	 * 		Decompiler name.
	 * @param version
	 * 		Decompiler version.
	 */
	public AbstractJvmDecompiler(@Nonnull String name, @Nonnull String version) {
		super(name, version);
	}

	@Override
	public void addJvmInputFilter(JvmInputFilter filter) {
		inputFilters.add(filter);
	}

	@Override
	public final DecompileResult decompile(Workspace workspace, JvmClassInfo classInfo) {
		// Check for cached result, returning it if found.
		DecompileResult cachedResult = CachedDecompileProperty.get(classInfo, this);
		if (cachedResult != null)
			return cachedResult;

		// Get bytecode and run through filters.
		byte[] bytecode = classInfo.getBytecode();
		for (JvmInputFilter filter : inputFilters)
			bytecode = filter.filter(bytecode);

		// Pass to implementation.
		DecompileResult result = decompile(workspace, classInfo.getName(), bytecode);

		// Cache result
		CachedDecompileProperty.set(classInfo, this, result);
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		AbstractJvmDecompiler other = (AbstractJvmDecompiler) o;

		return inputFilters.equals(other.inputFilters);
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
