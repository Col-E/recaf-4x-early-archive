package software.coley.recaf.services.search.result;

import jakarta.annotation.Nonnull;

/**
 * Result of a class reference match.
 *
 * @author Matt Coley
 */
public class MemberReferenceResult extends Result<MemberReferenceResult.MemberReference> {
	private final MemberReference ref;

	/**
	 * @param location
	 * 		Result location.
	 * @param owner
	 * 		Name of class declaring the member.
	 * @param name
	 * 		Member name.
	 * @param desc
	 * 		Member descriptor.
	 */
	public MemberReferenceResult(@Nonnull Location location,
								 @Nonnull String owner, @Nonnull String name, @Nonnull String desc) {
		this(location, new MemberReference(owner, name, desc));
	}

	/**
	 * @param location
	 * 		Result location.
	 * @param ref
	 * 		Member reference.
	 */
	public MemberReferenceResult(@Nonnull Location location, @Nonnull MemberReference ref) {
		super(location);
		this.ref = ref;
	}

	@Nonnull
	@Override
	protected MemberReference getValue() {
		return ref;
	}

	public static class MemberReference {
		private final String owner;
		private final String name;
		private final String descr;

		public MemberReference(@Nonnull String owner, @Nonnull String name, @Nonnull String descr) {
			this.owner = owner;
			this.name = name;
			this.descr = descr;
		}

		/**
		 * @return {@code true} when this is a reference to a field member.
		 */
		public boolean isFieldReference() {
			return !isMethodReference();
		}

		/**
		 * @return {@code true} when this is a reference to a method member.
		 */
		public boolean isMethodReference() {
			return descr.charAt(0) == '(';
		}

		/**
		 * @return Name of class declaring the member.
		 */
		@Nonnull
		public String getOwner() {
			return owner;
		}

		/**
		 * @return Member name.
		 */
		@Nonnull
		public String getName() {
			return name;
		}

		/**
		 * @return Member descriptor.
		 */
		@Nonnull
		public String getDescr() {
			return descr;
		}

		@Override
		public String toString() {
			return "MemberReference{" +
					"owner='" + owner + '\'' +
					", name='" + name + '\'' +
					", descr='" + descr + '\'' +
					'}';
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			MemberReference that = (MemberReference) o;

			if (!owner.equals(that.owner)) return false;
			if (!name.equals(that.name)) return false;
			return descr.equals(that.descr);
		}

		@Override
		public int hashCode() {
			int result = owner.hashCode();
			result = 31 * result + name.hashCode();
			result = 31 * result + descr.hashCode();
			return result;
		}
	}
}
