package software.coley.recaf.info.builder;

import software.coley.recaf.info.*;
import software.coley.recaf.info.properties.BasicPropertyContainer;
import software.coley.recaf.info.properties.PropertyContainer;

/**
 * Common builder info for {@link FileInfo}.
 *
 * @param <B>
 * 		Self type. Exists so implementations don't get stunted in their chaining.
 *
 * @author Matt Coley
 * @see ZipFileInfoBuilder
 * @see DexFileInfoBuilder
 * @see ModulesFileInfoBuilder
 */
public class FileInfoBuilder<B extends FileInfoBuilder<?>> {
	private PropertyContainer properties = new BasicPropertyContainer();
	private String name;
	private byte[] rawContent;

	public FileInfoBuilder() {
		// default
	}

	protected FileInfoBuilder(FileInfo fileInfo) {
		// copy state
		withName(fileInfo.getName());
		withRawContent(fileInfo.getRawContent());
		withProperties(new BasicPropertyContainer(fileInfo.getProperties()));
	}

	protected FileInfoBuilder(FileInfoBuilder<?> other) {
		withName(other.getName());
		withRawContent(other.getRawContent());
		withProperties(other.getProperties());
	}

	@SuppressWarnings("unchecked")
	public static <B extends FileInfoBuilder<?>> B forFile(FileInfo info) {
		FileInfoBuilder<?> builder;
		if (info.isZipFile()) {
			// Handle different container types
			if (info instanceof JarFileInfo) {
				builder = new JarFileInfoBuilder((JarFileInfo) info);
			} else if (info instanceof JModFileInfo) {
				builder = new JModFileInfoBuilder((JModFileInfo) info);
			} else if (info instanceof WarFileInfo) {
				builder = new WarFileInfoBuilder((WarFileInfo) info);
			} else {
				builder = new ZipFileInfoBuilder(info.asZipFile());
			}
		} else if (info instanceof DexFileInfo) {
			builder = new DexFileInfoBuilder((DexFileInfo) info);
		} else if (info instanceof ModulesFileInfo) {
			builder = new ModulesFileInfoBuilder((ModulesFileInfo) info);
		} else {
			builder = new FileInfoBuilder<>(info);
		}
		return (B) builder;
	}

	@SuppressWarnings("unchecked")
	public B withProperties(PropertyContainer properties) {
		this.properties = properties;
		return (B) this;
	}

	@SuppressWarnings("unchecked")
	public B withName(String name) {
		this.name = name;
		return (B) this;
	}

	@SuppressWarnings("unchecked")
	public B withRawContent(byte[] rawContent) {
		this.rawContent = rawContent;
		return (B) this;
	}

	public PropertyContainer getProperties() {
		return properties;
	}

	public String getName() {
		return name;
	}

	public byte[] getRawContent() {
		return rawContent;
	}

	public BasicFileInfo build() {
		return new BasicFileInfo(this);
	}
}
