package software.coley.recaf.services.decompile;

import jakarta.annotation.Nonnull;

/**
 * Base for {@link Decompiler}.
 *
 * @author Matt Coley
 */
public class AbstractDecompiler implements Decompiler {
	private final String name;
	private final String version;

	/**
	 * @param name
	 * 		Decompiler name.
	 * @param version
	 * 		Decompiler version.
	 */
	public AbstractDecompiler(@Nonnull String name, @Nonnull String version) {
		this.name = name;
		this.version = version;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		AbstractDecompiler other = (AbstractDecompiler) o;

		if (!name.equals(other.name)) return false;
		return version.equals(other.version);
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + version.hashCode();
		return result;
	}
}
