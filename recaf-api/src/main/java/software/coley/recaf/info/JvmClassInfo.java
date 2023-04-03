package software.coley.recaf.info;

import jakarta.annotation.Nonnull;
import me.coley.cafedude.classfile.ConstantPoolConstants;
import org.objectweb.asm.ClassReader;
import software.coley.recaf.info.builder.JvmClassInfoBuilder;

import java.util.HashSet;
import java.util.Set;
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
	 * @return Java class file version.
	 */
	int getVersion();

	/**
	 * @return Bytecode of class.
	 */
	@Nonnull
	byte[] getBytecode();

	/**
	 * @return Class reader of {@link #getBytecode()}.
	 */
	@Nonnull
	ClassReader getClassReader();

	/**
	 * @return Set of all classes referenced in the constant pool.
	 */
	@Nonnull
	default Set<String> getReferencedClasses() {
		Set<String> classNames = new HashSet<>();
		ClassReader reader = getClassReader();
		int itemCount = reader.getItemCount();
		char[] buffer = new char[reader.getMaxStringLength()];
		for (int i = 1; i < itemCount; i++) {
			int offset = reader.getItem(i);
			if (offset >= 10) {
				int itemTag = reader.readByte(offset - 1);
				if (itemTag == ConstantPoolConstants.CLASS) {
					String className = reader.readUTF8(offset, buffer);
					classNames.add(className);
				}
			}
		}
		return classNames;
	}

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

	@Nonnull
	@Override
	default JvmClassInfo asJvmClass() {
		return this;
	}

	@Nonnull
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

	@Nonnull
	@Override
	default JvmClassInfoBuilder toBuilder() {
		return new JvmClassInfoBuilder(this);
	}
}
