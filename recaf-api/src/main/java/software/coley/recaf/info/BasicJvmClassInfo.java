package software.coley.recaf.info;

import software.coley.recaf.info.builder.JvmClassInfoBuilder;

/**
 * Basic JVM class info implementation.
 *
 * @author Matt Coley
 */
public class BasicJvmClassInfo extends BasicClassInfo implements JvmClassInfo {
	private final byte[] bytecode;
	private final int version;

	/**
	 * @param builder
	 * 		Builder to pull info from.
	 */
	public BasicJvmClassInfo(JvmClassInfoBuilder builder) {
		super(builder);
		this.bytecode = builder.getBytecode();
		this.version = builder.getVersion();
	}

	@Override
	public byte[] getBytecode() {
		return bytecode;
	}

	@Override
	public int getVersion() {
		return version;
	}
}
