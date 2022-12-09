package software.coley.recaf.info;

import jakarta.annotation.Nonnull;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Outline of a JVM class.
 *
 * @author Matt Coley
 */
public interface JvmClassInfo extends ClassInfo {
	/**
	 * Multi-release JARs prefix class names with this, plus the target version.
	 * For example: Multiple versions of {@code foo/Bar.class}
	 * <ul>
	 *     <li>{@code foo/Bar.class}</li>
	 *     <li>{@code META-INF/versions/9/foo/Bar.class}</li>
	 *     <li>{@code META-INF/versions/11/foo/Bar.class}</li>
	 * </ul>
	 * The first item is used for Java 8.<br>
	 * The second item for Java 9 and 10.<br>
	 * The third item for Java 11+.
	 */
	String MULTI_RELEASE_PREFIX = "META-INF/versions/";

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
}
