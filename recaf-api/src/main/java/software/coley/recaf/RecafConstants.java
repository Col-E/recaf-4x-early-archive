package software.coley.recaf;

import org.objectweb.asm.Opcodes;

public final class RecafConstants {
	private RecafConstants() {}

	public static int getAsmVersion() {
		return Opcodes.ASM9;
	}
}
