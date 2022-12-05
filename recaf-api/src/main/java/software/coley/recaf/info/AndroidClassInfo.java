package software.coley.recaf.info;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Outline of an Android class.
 *
 * @author Matt Coley
 */
public interface AndroidClassInfo extends ClassInfo {
	@Override
	default void acceptIfJvmClass(Consumer<JvmClassInfo> action) {
		// no-op
	}

	@Override
	default void acceptIfAndroidClass(Consumer<AndroidClassInfo> action) {
		action.accept(this);
	}

	@Override
	default boolean testIfJvmClass(Predicate<JvmClassInfo> predicate) {
		return false;
	}

	@Override
	default boolean testIfAndroidClass(Predicate<AndroidClassInfo> predicate) {
		return predicate.test(this);
	}

	@Override
	default JvmClassInfo asJvmClass() {
		throw new IllegalStateException("Android class cannot be cast to JVM class");
	}

	@Override
	default AndroidClassInfo asAndroidClass() {
		return this;
	}

	@Override
	default boolean isJvmClass() {
		return false;
	}

	@Override
	default boolean isAndroidClass() {
		return true;
	}
}
