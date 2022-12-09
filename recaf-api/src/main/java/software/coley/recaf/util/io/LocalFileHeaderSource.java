package software.coley.recaf.util.io;

import software.coley.llzip.ZipCompressions;
import software.coley.llzip.part.LocalFileHeader;
import software.coley.llzip.util.ByteData;
import software.coley.llzip.util.ByteDataUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * Byte source from {@link LocalFileHeader}.
 *
 * @author xDark
 */
public final class LocalFileHeaderSource implements ByteSource {
	private final LocalFileHeader fileHeader;
	private ByteData decompressed;

	public LocalFileHeaderSource(LocalFileHeader fileHeader) {
		this.fileHeader = fileHeader;
	}

	@Override
	public byte[] readAll() throws IOException {
		return ByteDataUtil.toByteArray(decompress());
	}

	@Override
	public byte[] peek(int count) throws IOException {
		ByteData data = decompress();
		long length = data.length();
		if (length < count)
			count = (int) length;
		byte[] bytes = new byte[count];
		data.get(0L, bytes, 0, count);
		return bytes;
	}

	@Override
	public InputStream openStream() throws IOException {
		// Delegate to byte source
		return ByteSources.forZip(decompress()).openStream();
	}

	private ByteData decompress() throws IOException {
		ByteData decompressed = this.decompressed;
		if (decompressed == null) {
			return this.decompressed = ZipCompressions.decompress(fileHeader);
		}
		return decompressed;
	}
}