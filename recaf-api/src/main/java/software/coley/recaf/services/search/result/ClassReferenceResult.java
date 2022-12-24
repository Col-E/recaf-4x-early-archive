package software.coley.recaf.services.search.result;

import jakarta.annotation.Nonnull;

/**
 * Result of a class reference match.
 *
 * @author Matt Coley
 */
public class ClassReferenceResult extends Result<ClassReferenceResult.ClassReference> {
	private final ClassReference ref;

	/**
	 * @param location
	 * 		Result location.
	 * @param name
	 * 		Class name.
	 */
	public ClassReferenceResult(@Nonnull Location location, @Nonnull String name) {
		this(location, new ClassReference(name));
	}

	/**
	 * @param location
	 * 		Result location.
	 * @param ref
	 * 		Class reference.
	 */
	public ClassReferenceResult(@Nonnull Location location, @Nonnull ClassReference ref) {
		super(location);
		this.ref = ref;
	}

	@Nonnull
	@Override
	protected ClassReference getValue() {
		return ref;
	}

	public static class ClassReference {
		private final String name;

		public ClassReference(@Nonnull String name) {
			this.name = name;
		}

		/**
		 * @return Class name.
		 */
		@Nonnull
		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return "ClassReference{" +
					"name='" + name + '\'' +
					'}';
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			ClassReference that = (ClassReference) o;

			return name.equals(that.name);
		}

		@Override
		public int hashCode() {
			return name.hashCode();
		}
	}
}
