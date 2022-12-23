package software.coley.recaf.info;

import software.coley.recaf.info.builder.FileInfoBuilder;

import java.nio.charset.StandardCharsets;

/**
 * Basic implementation of text file info.
 *
 * @author Matt Coley
 */
public class BasicTextFileInfo extends BasicFileInfo implements TextFileInfo {
	private final String text;

	/**
	 * @param builder
	 * 		Builder to pull information from.
	 */
	public BasicTextFileInfo(FileInfoBuilder<?> builder) {
		super(builder);
		text = new String(getRawContent(), StandardCharsets.UTF_8);
	}

	@Override
	public String getText() {
		return text;
	}
}
