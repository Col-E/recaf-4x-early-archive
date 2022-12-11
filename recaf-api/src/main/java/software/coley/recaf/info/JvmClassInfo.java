package software.coley.recaf.info;

import jakarta.annotation.Nonnull;
import software.coley.recaf.info.builder.AbstractClassInfoBuilder;
import software.coley.recaf.info.builder.JvmClassInfoBuilder;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Outline of a JVM class.
 *
 * @author Matt Coley
 */
public interface JvmClassInfo extends ClassInfo {
	/**
	 * Denotes the base version offset.
	 * <ul>
	 *     <li>For version 1 of you would use {@code BASE_VERSION + 1}.</li>
	 *     <li>For version 2 of you would use {@code BASE_VERSION + 2}.</li>
	 *     <li>...</li>
	 *     <li>For version N of you would use {@code BASE_VERSION + N}.</li>
	 * </ul>
	 */
	int BASE_VERSION = 44;

	/**
	 * @return Bytecode of class.
	 */
	@Nonnull
	byte[] getBytecode();

	/**
	 * @return Java class file version.
	 */
	int getVersion();

	@Override
	default void acceptIfJvmClass(Consumer<JvmClassInfo> action) {
		action.accept(this);
	}

	@Override
	default void acceptIfAndroidClass(Consumer<AndroidClassInfo> action) {
		// no-op
	}

	@Override
	default boolean testIfJvmClass(Predicate<JvmClassInfo> predicate) {
		return predicate.test(this);
	}

	@Override
	default boolean testIfAndroidClass(Predicate<AndroidClassInfo> predicate) {
		return false;
	}

	@Override
	default JvmClassInfo asJvmClass() {
		return this;
	}

	@Override
	default AndroidClassInfo asAndroidClass() {
		throw new IllegalStateException("JVM class cannot be cast to Android class");
	}

	@Override
	default boolean isJvmClass() {
		return true;
	}

	@Override
	default boolean isAndroidClass() {
		return false;
	}

	@Override
	default JvmClassInfoBuilder toBuilder() {
		return new JvmClassInfoBuilder(this);
	}
}
