package software.coley.recaf.info.member;

import jakarta.annotation.Nonnull;

import java.util.List;
import java.util.Objects;

/**
 * Basic implementation of a method member.
 *
 * @author Matt Coley
 */
public class BasicMethodMember extends BasicMember implements MethodMember {
	private final List<String> thrownTypes;
	private final List<LocalVariable> variables;

	/**
	 * @param name
	 * 		Method name.
	 * @param desc
	 * 		Method descriptor.
	 * @param signature
	 * 		Method generic signature. May be {@code null}.
	 * @param access
	 * 		Method access modifiers.
	 * @param thrownTypes
	 * 		Method's thrown exceptions.
	 * @param variables
	 * 		Method's local variables.
	 */
	public BasicMethodMember(String name, String desc, String signature, int access,
							 @Nonnull List<String> thrownTypes, @Nonnull List<LocalVariable> variables) {
		super(name, desc, signature, access);
		this.thrownTypes = thrownTypes;
		this.variables = variables;
	}

	/**
	 * @param variable
	 * 		Variable to add.
	 */
	public void addLocalVariable(LocalVariable variable) {
		variables.add(variable);
	}

	@Nonnull
	@Override
	public List<String> getThrownTypes() {
		return thrownTypes;
	}

	@Nonnull
	@Override
	public List<LocalVariable> getLocalVariables() {
		return variables;
	}

	@Override
	public String toString() {
		return "Method: " + getName() + getDescriptor();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || !MethodMember.class.isAssignableFrom(o.getClass())) return false;

		MethodMember method = (MethodMember) o;

		if (!getName().equals(method.getName())) return false;
		if (!Objects.equals(getSignature(), method.getSignature())) return false;
		if (!getThrownTypes().equals(method.getThrownTypes())) return false;
		if (!getLocalVariables().equals(method.getLocalVariables())) return false;
		return getDescriptor().equals(method.getDescriptor());
	}

	@Override
	public int hashCode() {
		int result = getName().hashCode();
		result = 31 * result + getDescriptor().hashCode();
		result = 31 * result + getThrownTypes().hashCode();
		result = 31 * result + getLocalVariables().hashCode();
		if (getSignature() != null) result = 31 * result + getSignature().hashCode();
		return result;
	}
}
