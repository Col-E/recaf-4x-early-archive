package software.coley.recaf.util;

import org.slf4j.Logger;
import software.coley.recaf.analytics.logging.Logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility for creating simple ZIP files.
 *
 * @author Matt Coley
 */
public class ZipCreationUtils {
	private static final Logger logger = Logging.get(ZipCreationUtils.class);

	/**
	 * @param name
	 * 		Entry name.
	 * @param content
	 * 		Entry value.
	 *
	 * @return ZIP bytes, containing the single entry.
	 *
	 * @throws IOException
	 * 		When the content cannot be written.
	 */
	public static byte[] createSingleEntryZip(String name, byte[] content) throws IOException {
		return createZip(zos -> {
			zos.putNextEntry(new ZipEntry(name));
			zos.write(content);
			zos.closeEntry();
		});
	}

	/**
	 * @param entryMap
	 * 		Map of entry name --> contents.
	 *
	 * @return ZIP bytes, containing given entries.
	 *
	 * @throws IOException
	 * 		When the content cannot be written.
	 */
	public static byte[] createZip(Map<String, byte[]> entryMap) throws IOException {
		return createZip(zos -> {
			for (Map.Entry<String, byte[]> entry : entryMap.entrySet()) {
				zos.putNextEntry(new ZipEntry(entry.getKey()));
				zos.write(entry.getValue());
				zos.closeEntry();
			}
		});
	}

	/**
	 * @param consumer
	 * 		Action to do on the ZIP stream.
	 *
	 * @return ZIP bytes.
	 *
	 * @throws IOException
	 * 		When the action fails.
	 */
	public static byte[] createZip(UncheckedConsumer<ZipOutputStream> consumer) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ZipOutputStream zos = new ZipOutputStream(baos)) {
			consumer.accept(zos);
		}
		return baos.toByteArray();
	}

	/**
	 * Resetting the tracked names allows you to write duplicate entries to a ZIP file.
	 *
	 * @param zos
	 * 		ZIP stream to reset name tracking of.
	 */
	private static void resetNames(ZipOutputStream zos) {
		try {
			Field field = ZipOutputStream.class.getDeclaredField("names");
			field.setAccessible(true);
			Collection<?> names = (Collection<?>) field.get(zos);
			names.clear();
		} catch (Exception ex) {
			logger.error("Failed to reset ZIP name tracking: {}", zos, ex);
		}
	}


	/**
	 * @return New ZIP builder.
	 */
	public static ZipBuilder builder() {
		return new ZipBuilder();
	}

	/**
	 * Helper to create zip files.
	 */
	public static class ZipBuilder {
		private final List<Entry> entries = new ArrayList<>();

		/**
		 * @param name
		 * 		Entry name.
		 * @param content
		 * 		Entry contents.
		 *
		 * @return Builder.
		 */
		public ZipBuilder add(String name, byte[] content) {
			entries.add(new Entry(name, content));
			return this;
		}

		/**
		 * @return Generated ZIP.
		 *
		 * @throws IOException
		 * 		When the content cannot be written.
		 */
		public byte[] bytes() throws IOException {
			return createZip(zos -> {
				for (Entry entry : entries) {
					zos.putNextEntry(new ZipEntry(entry.name));
					zos.write(entry.content);
					zos.closeEntry();
					resetNames(zos);
				}
			});
		}

		private static class Entry {
			private final String name;
			private final byte[] content;

			private Entry(String name, byte[] content) {
				this.name = name;
				this.content = content;
			}
		}
	}
}
