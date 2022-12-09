package software.coley.recaf.info.builder;

import software.coley.recaf.info.*;

public class JarFileInfoBuilder extends ZipFileInfoBuilder {
	public JarFileInfoBuilder() {
		// empty
	}

	public JarFileInfoBuilder(JarFileInfo jarInfo) {
		super(jarInfo);
	}

	public JarFileInfoBuilder(FileInfoBuilder<?> other) {
		super(other);
	}

	@Override
	public BasicFileInfo build() {
		return new BasicJarFileInfo(this);
	}
}
