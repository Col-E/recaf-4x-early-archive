package software.coley.recaf.info.builder;

import software.coley.recaf.info.*;

public class WarFileInfoBuilder extends ZipFileInfoBuilder {
	public WarFileInfoBuilder() {
		// empty
	}

	public WarFileInfoBuilder(WarFileInfo warInfo) {
		super(warInfo);
	}

	public WarFileInfoBuilder(FileInfoBuilder<?> other) {
		super(other);
	}

	@Override
	public BasicWarFileInfo build() {
		return new BasicWarFileInfo(this);
	}
}
