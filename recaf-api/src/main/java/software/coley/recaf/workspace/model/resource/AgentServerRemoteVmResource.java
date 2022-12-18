package software.coley.recaf.workspace.model.resource;

import com.sun.tools.attach.VirtualMachine;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.objectweb.asm.ClassReader;
import software.coley.instrument.ApiConstants;
import software.coley.instrument.Client;
import software.coley.instrument.data.ClassData;
import software.coley.instrument.data.ClassLoaderInfo;
import software.coley.instrument.message.MessageConstants;
import software.coley.instrument.message.broadcast.BroadcastClassMessage;
import software.coley.instrument.message.broadcast.BroadcastClassloaderMessage;
import software.coley.instrument.message.request.RequestClassMessage;
import software.coley.instrument.message.request.RequestClassloaderClassesMessage;
import software.coley.instrument.message.request.RequestClassloadersMessage;
import software.coley.instrument.message.request.RequestRedefineMessage;
import software.coley.recaf.analytics.logging.DebuggingLogger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.info.builder.JvmClassInfoBuilder;
import software.coley.recaf.workspace.model.bundle.BasicJvmClassBundle;
import software.coley.recaf.workspace.model.bundle.BundleListener;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Implementation of {@link WorkspaceRemoteVmResource} via {@link Client}.
 *
 * @author Matt Coley
 */
public class AgentServerRemoteVmResource extends BasicWorkspaceResource implements WorkspaceRemoteVmResource {
	private static final DebuggingLogger logger = Logging.get(AgentServerRemoteVmResource.class);
	private final Map<Integer, RemoteJvmClassBundle> remoteBundleMap = new HashMap<>();
	private final Map<Integer, ClassLoaderInfo> remoteLoaders = new HashMap<>();
	private final Set<String> queuedRedefines = new ConcurrentSkipListSet<>();
	private final VirtualMachine virtualMachine;
	private final Client client;
	private boolean closed;

	/**
	 * @param virtualMachine
	 * 		Instance of remote VM.
	 * @param client
	 * 		Client to communicate to the remote VM.
	 */
	public AgentServerRemoteVmResource(VirtualMachine virtualMachine, Client client) {
		super(new WorkspaceResourceBuilder());
		this.virtualMachine = virtualMachine;
		this.client = client;
	}

	@Nonnull
	@Override
	public VirtualMachine getVirtualMachine() {
		return virtualMachine;
	}

	@Nonnull
	@Override
	public Map<Integer, ClassLoaderInfo> getRemoteLoaders() {
		return remoteLoaders;
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public Map<Integer, JvmClassBundle> getJvmClassloaderBundles() {
		return (Map<Integer, JvmClassBundle>) (Object) remoteBundleMap;
	}

	@Override
	public void close() {
		super.close();
		closed = true;
		try {
			virtualMachine.detach();
		} catch (IOException ex) {
			logger.warn("Failed to properly detach from VM");
		}
	}

	/**
	 * Setup client connection handling.
	 *
	 * @return {@code true} on success.
	 * {@code false} when any error occurred.
	 */
	public boolean initialize() {
		try {
			client.setBroadcastListener((messageType, message) -> {
				switch (messageType) {
					case MessageConstants.ID_BROADCAST_LOADER:
						// New loader reported
						BroadcastClassloaderMessage loaderMessage = (BroadcastClassloaderMessage) message;
						ClassLoaderInfo loaderInfo = loaderMessage.getClassLoader();
						remoteLoaders.put(loaderInfo.getId(), loaderInfo);
						break;
					case MessageConstants.ID_BROADCAST_CLASS:
						// New class, or update to existing class reported
						BroadcastClassMessage classMessage = (BroadcastClassMessage) message;
						ClassData data = classMessage.getData();
						handleReceiveClassData(data, null);
						break;
					default:
						// unknown broadcast packet
						break;
				}
			});

			// Try to connect
			if (!client.connect())
				throw new IOException("Client connection failed");

			// Request known classloaders
			client.sendAsync(new RequestClassloadersMessage(), loaderReply -> {
				for (ClassLoaderInfo loader : loaderReply.getClassLoaders()) {
					if (loader.isBootstrap())
						continue;
					int loaderId = loader.getId();
					remoteLoaders.put(loaderId, loader);

					// Get/create bundle for loader
					ClassLoaderInfo loaderInfo = remoteLoaders.get(loaderId);
					RemoteJvmClassBundle bundle = remoteBundleMap
							.computeIfAbsent(loaderId, id -> new RemoteJvmClassBundle(loaderInfo));

					// Request all classes from classloader
					client.sendAsync(new RequestClassloaderClassesMessage(loaderId), classesReply -> {
						for (String className : classesReply.getClasses()) {
							if (closed)
								return;

							// If class does not exist in bundle, then request it from remote server
							if (bundle.get(className) == null) {
								client.sendBlocking(new RequestClassMessage(loaderId, className), reply -> {
									if (reply.hasData()) {
										ClassData data = reply.getData();
										handleReceiveClassData(data, bundle);
									}
								});
							}
						}
					});
				}
			});
			return true;
		} catch (Throwable t) {
			logger.error("Could not setup connection to agent server, client connect gave 'false'", t);
			return false;
		}
	}

	/**
	 * @param data
	 * 		Class data to handle adding to the resource.
	 * @param bundle
	 * 		Bundle to check within.
	 * 		May be {@code null} to be lazily fetched in this method.
	 */
	private void handleReceiveClassData(@Nonnull ClassData data, @Nullable RemoteJvmClassBundle bundle) {
		// If it belongs to the bootstrap classloader, it's a core JVM class.
		if (data.getClassLoaderId() == ApiConstants.BOOTSTRAP_CLASSLOADER_ID)
			return;

		// If this class broadcast isn't for one of our redefine requests, it's a new class.
		if (!queuedRedefines.remove(data.getName())) {
			// Get the bundle for the remote classloader if not specified by parameter
			if (bundle == null) {
				int loaderId = data.getClassLoaderId();
				ClassLoaderInfo loaderInfo = remoteLoaders.get(loaderId);
				bundle = remoteBundleMap
						.computeIfAbsent(loaderId, id -> new RemoteJvmClassBundle(loaderInfo));
			}

			// Add the class
			JvmClassInfo classInfo = new JvmClassInfoBuilder(new ClassReader(data.getCode())).build();
			bundle.initialPut(classInfo);
		}
	}

	/**
	 * JVM bundle extension adding a listener to handle syncing local changes with the remote server.
	 */
	private class RemoteJvmClassBundle extends BasicJvmClassBundle {
		private RemoteJvmClassBundle(ClassLoaderInfo loaderInfo) {
			this.addBundleListener(new BundleListener<>() {
				@Override
				public void onNewItem(String key, JvmClassInfo value) {
					// Should occur when we get data from the client.
					// No action needed.
				}

				@Override
				public void onUpdateItem(String key, JvmClassInfo oldValue, JvmClassInfo newValue) {
					// Should occur when the user makes changes to a class from recaf.
					// We need to send this definition to the remote server.

					// Record that we expect acknowledgement from the remote server for our redefine request.
					queuedRedefines.add(key);

					// Request class update
					byte[] definition = newValue.getBytecode();
					client.sendAsync(new RequestRedefineMessage(loaderInfo.getId(), key, definition), reply -> {
						if (reply.isSuccess()) {
							logger.debug("Redefine '{}' success", key);
						} else {
							logger.debug("Redefine '{}' failed: {}", key, reply.getMessage());
						}
					});
				}

				@Override
				public void onRemoveItem(String key, JvmClassInfo value) {
					// Should not occur
					throw new IllegalStateException("Remove operations should not occur for remote VM resource!");
				}
			});
		}
	}
}
