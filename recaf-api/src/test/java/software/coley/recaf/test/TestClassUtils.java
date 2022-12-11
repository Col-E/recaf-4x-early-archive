package software.coley.recaf.test;

import org.objectweb.asm.ClassReader;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.info.builder.JvmClassInfoBuilder;

import java.io.IOException;

/**
 * Various test utils for {@link Class} and {@link software.coley.recaf.info.ClassInfo} usage.
 */
public class TestClassUtils {
	/**
	 * @param c
	 * 		Class ref.
	 *
	 * @return Info of class.
	 *
	 * @throws IOException
	 * 		When class cannot be found at runtime.
	 */
	public static JvmClassInfo fromRuntimeClass(Class<?> c) throws IOException {
		return new JvmClassInfoBuilder(new ClassReader(c.getName())).build();
	}
}
