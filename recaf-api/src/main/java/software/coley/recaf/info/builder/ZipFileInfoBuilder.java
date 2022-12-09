package software.coley.recaf.info.builder;

import software.coley.recaf.info.BasicZipFileInfo;
import software.coley.recaf.info.ZipFileInfo;

public class ZipFileInfoBuilder extends FileInfoBuilder<ZipFileInfoBuilder> {
	public ZipFileInfoBuilder() {
		// empty
	}

	public ZipFileInfoBuilder(ZipFileInfo zipInfo) {
		super(zipInfo);
	}

	public ZipFileInfoBuilder(FileInfoBuilder<?> other) {
		super(other);
	}

	@Override
	public BasicZipFileInfo build() {
		return new BasicZipFileInfo(this);
	}
}
