package software.coley.recaf.test.dummy;

/**
 * Dummy class to test existence of fields with varing degrees of access.
 */
@SuppressWarnings("all")
public class AccessibleFields {
	public static final int CONSTANT_FIELD = 16;
	private final int privateFinalField = 8;
	protected final int protectedField = 4;
	public final int publicField = 2;
	final int packageField = 1;
}
