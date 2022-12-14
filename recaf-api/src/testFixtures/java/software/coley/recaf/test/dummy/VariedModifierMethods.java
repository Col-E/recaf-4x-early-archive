package software.coley.recaf.test.dummy;

/**
 * Dummy class to test existence of methods with varing modifiers.
 */
@SuppressWarnings("all")
public abstract class VariedModifierMethods {
	static void staticMethod(){}
	final void finalMethod(){}
	synchronized void synchronizedMethod(){}
	native void nativeMethod();
	abstract void abstractMethod();
	strictfp void strictfpMethod(){}
	void varargsMethod(String...varargs){}
}
