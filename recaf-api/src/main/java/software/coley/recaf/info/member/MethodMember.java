package software.coley.recaf.info.member;

import software.coley.recaf.info.ClassInfo;

import java.util.List;

/**
 * Field component of a {@link ClassInfo}.
 *
 * @author Matt Coley
 */
public interface MethodMember extends ClassMember {
	/**
	 * @return List of thrown exceptions.
	 */
	List<String> getExceptions();

	@Override
	default boolean isField() {
		return false;
	}

	@Override
	default boolean isMethod() {
		return true;
	}
}
