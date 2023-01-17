package software.coley.recaf.services.attach;

import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.*;

/**
 * Helper to wrap {@link MBeanServerConnection}.
 *
 * @author Matt Coley
 */
public class JmxBeanServerConnection {
	public static final ObjectName CLASS_LOADING = named(ManagementFactory.CLASS_LOADING_MXBEAN_NAME);
	public static final ObjectName COMPILATION = named(ManagementFactory.COMPILATION_MXBEAN_NAME);
	public static final ObjectName OPERATING_SYSTEM = named(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
	public static final ObjectName RUNTIME = named(ManagementFactory.RUNTIME_MXBEAN_NAME);
	public static final ObjectName THREAD = named(ManagementFactory.THREAD_MXBEAN_NAME);
	// The following beans require knowing the bean's domain in advance to supply it as an additional parameter:
	//  - GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE
	//  - MEMORY_MANAGER_MXBEAN_DOMAIN_TYPE
	//  - MEMORY_POOL_MXBEAN_DOMAIN_TYPE
	private final MBeanServerConnection connection;

	/**
	 * @param connection
	 * 		Underlying connection.
	 */
	public JmxBeanServerConnection(MBeanServerConnection connection) {
		this.connection = connection;
	}

	/**
	 * @return Attributes and operations of {@link ClassLoadingMXBean}.
	 *
	 * @throws Exception
	 * 		When the bean could not be fetched.
	 */
	public MBeanInfo getClassloadingBeanInfo() throws Exception {
		return connection.getMBeanInfo(CLASS_LOADING);
	}

	/**
	 * @return Attributes and operations of {@link CompilationMXBean}.
	 *
	 * @throws Exception
	 * 		When the bean could not be fetched.
	 */
	public MBeanInfo getCompilationBeanInfo() throws Exception {
		return connection.getMBeanInfo(COMPILATION);
	}

	/**
	 * @return Attributes and operations of {@link OperatingSystemMXBean}.
	 *
	 * @throws Exception
	 * 		When the bean could not be fetched.
	 */
	public MBeanInfo getOperatingSystemBeanInfo() throws Exception {
		return connection.getMBeanInfo(OPERATING_SYSTEM);
	}

	/**
	 * @return Attributes and operations of {@link RuntimeMXBean}.
	 *
	 * @throws Exception
	 * 		When the bean could not be fetched.
	 */
	public MBeanInfo getRuntimeBeanInfo() throws Exception {
		return connection.getMBeanInfo(RUNTIME);
	}

	/**
	 * @return Attributes and operations of {@link ThreadMXBean}.
	 *
	 * @throws Exception
	 * 		When the bean could not be fetched.
	 */
	public MBeanInfo getThreadBeanInfo() throws Exception {
		return connection.getMBeanInfo(THREAD);
	}

	/**
	 * @return Underlying connection.
	 */
	public MBeanServerConnection getConnection() {
		return connection;
	}

	private static ObjectName named(String name) {
		try {
			return new ObjectName(name);
		} catch (MalformedObjectNameException ex) {
			throw new IllegalStateException(ex);
		}
	}
}
