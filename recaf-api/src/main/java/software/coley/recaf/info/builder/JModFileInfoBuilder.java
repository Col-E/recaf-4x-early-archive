package software.coley.recaf.info.builder;

import software.coley.recaf.info.*;

public class JModFileInfoBuilder extends ZipFileInfoBuilder {
	public JModFileInfoBuilder() {
		// empty
	}

	public JModFileInfoBuilder(JModFileInfo jmodInfo) {
		super(jmodInfo);
	}

	public JModFileInfoBuilder(FileInfoBuilder<?> other) {
		super(other);
	}

	@Override
	public BasicJModFileInfo build() {
		return new BasicJModFileInfo(this);
	}
}
