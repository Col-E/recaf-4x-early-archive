package software.coley.recaf.util;

import com.android.tools.r8.graph.DexProgramClass;
import software.coley.dextranslator.model.ApplicationData;
import software.coley.recaf.info.AndroidClassInfo;
import software.coley.recaf.info.builder.AndroidClassInfoBuilder;
import software.coley.recaf.util.io.ByteSource;
import software.coley.recaf.workspace.model.bundle.AndroidClassBundle;
import software.coley.recaf.workspace.model.bundle.BasicAndroidClassBundle;

import java.io.IOException;

/**
 * Dex file reading and writing.
 *
 * @author Matt Coley
 */
public class DexIOUtil {
	/**
	 * @param source
	 * 		Content source to read from. Must be wrapping a dex file.
	 *
	 * @return Bundle of classes from the dex file.
	 *
	 * @throws IOException
	 * 		When the dex file cannot be read from.
	 */
	public static AndroidClassBundle read(ByteSource source) throws IOException {
		BasicAndroidClassBundle classBundle = new BasicAndroidClassBundle();

		// Read dex file content
		ApplicationData data = ApplicationData.fromDex(source.readAll());

		// Populate bundle
		for (DexProgramClass dexClass : data.getApplication().classes()) {
			AndroidClassInfo classInfo = new AndroidClassInfoBuilder()
					.adaptFrom(dexClass)
					.build();
			classBundle.initialPut(classInfo);
		}

		return classBundle;
	}
}
