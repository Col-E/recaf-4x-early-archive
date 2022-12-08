package software.coley.recaf.info;

import java.util.Arrays;

/**
 * Basic implementation of file info.
 *
 * @author Matt Coley
 */
public class BasicFileInfo implements FileInfo {
	private final String name;
	private final byte[] rawContent;

	/**
	 * @param name
	 * 		File name/path.
	 * @param rawContent
	 * 		Raw contents of file.
	 */
	public BasicFileInfo(String name, byte[] rawContent) {
		this.name = name;
		this.rawContent = rawContent;
	}

	@Override
	public byte[] getRawContent() {
		return rawContent;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BasicFileInfo other = (BasicFileInfo) o;

		if (!name.equals(other.name)) return false;
		return Arrays.equals(rawContent, other.rawContent);
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + Arrays.hashCode(rawContent);
		return result;
	}
}
