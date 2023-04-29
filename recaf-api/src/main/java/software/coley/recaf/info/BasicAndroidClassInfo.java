package software.coley.recaf.info;

import com.android.tools.r8.graph.DexProgramClass;
import jakarta.annotation.Nonnull;
import org.objectweb.asm.ClassReader;
import software.coley.dextranslator.ir.ConversionException;
import software.coley.dextranslator.model.ApplicationData;
import software.coley.recaf.info.builder.AndroidClassInfoBuilder;
import software.coley.recaf.info.builder.JvmClassInfoBuilder;

import java.util.Collections;

/**
 * Basic Android class info implementation.
 *
 * @author Matt Coley
 */
public class BasicAndroidClassInfo extends BasicClassInfo implements AndroidClassInfo {
	private final DexProgramClass dexClass;
	private JvmClassInfo converted;

	/**
	 * @param builder
	 * 		Builder to pull info from.
	 */
	public BasicAndroidClassInfo(@Nonnull AndroidClassInfoBuilder builder) {
		super(builder);
		dexClass = builder.getDexClass();
	}

	/**
	 * @return Translation into JVM class.
	 */
	@Nonnull
	@Override
	public JvmClassInfo asJvmClass() {
		if (converted == null) {
			try {
				String name = getName();
				byte[] convertedBytecode = ApplicationData.fromProgramClasses(Collections.singleton(dexClass))
						.exportToJvmClass(name);
				if (convertedBytecode == null)
					throw new IllegalStateException("Failed to convert Dalvik model of " + name + " to JVM bytecode, " +
							"conversion results did not include type name.");
				ClassReader reader = new ClassReader(convertedBytecode);
				converted = new JvmClassInfoBuilder(reader).build();
			} catch (ConversionException ex) {
				throw new IllegalStateException(ex);
			}
		}
		return converted;
	}

	/**
	 * @return Origin dex class node.
	 */
	@Nonnull
	public DexProgramClass getDexClass() {
		return dexClass;
	}

	@Override
	public String toString() {
		return "Android class: " + getName();
	}
}
