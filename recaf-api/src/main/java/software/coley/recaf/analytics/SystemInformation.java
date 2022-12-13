package software.coley.recaf.analytics;

public class SystemInformation {
	// TODO: Flesh out to include useful info for us when users open bug reports
	//  - os name/arch
	//  - java version/vendor/arch
	
	public static String getJvmInfo() {
		return System.getProperty("java.version");
	}
}
