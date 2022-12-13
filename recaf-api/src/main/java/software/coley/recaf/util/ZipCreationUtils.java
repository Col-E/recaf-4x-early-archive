package software.coley.recaf.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility for creating simple ZIP files.
 *
 * @author Matt Coley
 */
public class ZipCreationUtils {
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
}
