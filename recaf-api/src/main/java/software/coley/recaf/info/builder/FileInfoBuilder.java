package software.coley.recaf.info.builder;

import software.coley.recaf.info.BasicFileInfo;
import software.coley.recaf.info.FileInfo;
import software.coley.recaf.info.properties.BasicPropertyContainer;
import software.coley.recaf.info.properties.PropertyContainer;

/**
 * Common builder info for {@link FileInfo}.
 *
 * @param <B>
 * 		Self type. Exists so implementations don't get stunted in their chaining.
 *
 * @author Matt Coley
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

	@SuppressWarnings("unchecked")
	public static <B extends FileInfoBuilder<?>> B forFile(FileInfo info) {
		FileInfoBuilder<FileInfoBuilder<?>> builder = new FileInfoBuilder<>(info);
		// If in the future if different file-info impls have additional values, do something like this:
		//    if (info.isZipFile()) builder.forZip().withZipProperty1(info.getZipProperty1())
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
		return new BasicFileInfo(name, rawContent, properties);
	}
}
