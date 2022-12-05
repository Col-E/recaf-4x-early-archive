package software.coley.recaf.info.member;

import java.util.List;
import java.util.Objects;

/**
 * Basic implementation of a method member.
 *
 * @author Matt Coley
 */
public class BasicMethodMember extends BasicMember implements MethodMember {
	private final List<String> exceptions;

	/**
	 * @param name
	 * 		Method name.
	 * @param desc
	 * 		Method descriptor.
	 * @param signature
	 * 		Method generic signature. May be {@code null}.
	 * @param access
	 * 		Method access modifiers.
	 * @param exceptions
	 * 		Method's thrown excpetions.
	 */
	public BasicMethodMember(String name, String desc, String signature, int access, List<String> exceptions) {
		super(name, desc, signature, access);
		this.exceptions = exceptions;
	}

	@Override
	public List<String> getExceptions() {
		return exceptions;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || !o.getClass().isAssignableFrom(MethodMember.class)) return false;

		MethodMember method = (MethodMember) o;

		if (!getName().equals(method.getName())) return false;
		if (!Objects.equals(getSignature(), method.getSignature())) return false;
		if (!getExceptions().equals(method.getExceptions())) return false;
		return getDescriptor().equals(method.getDescriptor());
	}

	@Override
	public int hashCode() {
		int result = getName().hashCode();
		result = 31 * result + getDescriptor().hashCode();
		result = 31 * result + getExceptions().hashCode();
		if (getSignature() != null) result = 31 * result + getSignature().hashCode();
		return result;
	}
}
