package software.coley.recaf.services.callgraph;

import dev.xdark.jlinker.MemberInfo;
import dev.xdark.jlinker.Resolution;
import dev.xdark.jlinker.ResolutionError;
import dev.xdark.jlinker.Result;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import software.coley.recaf.RecafConstants;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.cdi.WorkspaceScoped;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.info.member.MethodMember;
import software.coley.recaf.services.Service;
import software.coley.recaf.util.MultiMap;
import software.coley.recaf.workspace.WorkspaceModificationListener;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;
import software.coley.recaf.workspace.model.resource.ResourceJvmClassListener;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author Amejonah
 * @author Matt Coley
 */
@WorkspaceScoped
public class CallGraph implements Service, WorkspaceModificationListener, ResourceJvmClassListener {
	public static final String SERVICE_ID = "graph-calls";
	private static final Logger logger = Logging.get(CallGraph.class);
	private final CachedLinkResolver resolver = new CachedLinkResolver();
	private final Map<JvmClassInfo, LinkedClass> classToLinkerType = Collections.synchronizedMap(new IdentityHashMap<>());
	private final Map<JvmClassInfo, ClassMethodsContainer> classToMethodsContainer = Collections.synchronizedMap(new IdentityHashMap<>());
	private final MultiMap<String, MethodRef, Set<MethodRef>> unresolvedCalls = MultiMap.from(
			Collections.synchronizedMap(new HashMap<>()),
			() -> Collections.synchronizedSet(new HashSet<>()));
	private final CallGraphConfig config;
	private final ClassLookup lookup;

	/**
	 * @param config
	 * 		Graphing config options.
	 * @param workspace
	 * 		Workspace to pull data from.
	 */
	@Inject
	public CallGraph(@Nonnull CallGraphConfig config, @Nonnull Workspace workspace) {
		this.config = config;
		lookup = new ClassLookup(workspace);
		workspace.addWorkspaceModificationListener(this);
		workspace.getPrimaryResource().addResourceJvmClassListener(this);
		initialize(workspace);
	}

	/**
	 * @param classInfo
	 * 		Class to wrap.
	 *
	 * @return Wrapper for easy {@link MethodVertex} management for the class.
	 */
	@Nonnull
	public ClassMethodsContainer getClassMethodsContainer(@Nonnull JvmClassInfo classInfo) {
		return classToMethodsContainer.computeIfAbsent(classInfo, c -> new ClassMethodsContainer(classInfo));
	}

	/**
	 * @param classInfo
	 * 		Class to wrap.
	 *
	 * @return JLinker wrapper for class.
	 */
	@Nonnull
	private LinkedClass linked(@Nonnull JvmClassInfo classInfo) {
		return classToLinkerType.computeIfAbsent(classInfo, c -> new LinkedClass(lookup, c));
	}

	/**
	 * @param workspace
	 * 		Workspace to {@link #visit(JvmClassInfo)} all classes of.
	 */
	private void initialize(@Nonnull Workspace workspace) {
		for (WorkspaceResource resource : workspace.getAllResources(false)) {
			Stream.concat(resource.jvmClassBundleStream(),
					resource.getVersionedJvmClassBundles().values().stream()).forEach(bundle -> {
				for (JvmClassInfo jvmClass : bundle.values()) {
					visit(jvmClass);
				}
			});
		}
	}

	/**
	 * Populate {@link MethodVertex} for all methods in {@link JvmClassInfo#getMethods()}.
	 *
	 * @param jvmClass
	 * 		Class to visit.
	 */
	private void visit(@Nonnull JvmClassInfo jvmClass) {
		ClassMethodsContainer classMethodsContainer = getClassMethodsContainer(jvmClass);
		jvmClass.getClassReader().accept(new ClassVisitor(RecafConstants.getAsmVersion()) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
				MutableMethodVertex methodVertex = (MutableMethodVertex) classMethodsContainer.getVertex(name, descriptor);
				if (methodVertex == null) {
					logger.error("Method {}{} was visited, but not present in info for declaring class {}",
							name, descriptor, jvmClass.getName());
					return null;
				}

				return new MethodVisitor(RecafConstants.getAsmVersion()) {
					@Override
					public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
						onMethodCalled(methodVertex, opcode, owner, name, descriptor, isInterface);
					}

					@Override
					public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
						if (!"java/lang/invoke/LambdaMetafactory".equals(bootstrapMethodHandle.getOwner())
								|| !"metafactory".equals(bootstrapMethodHandle.getName())
								|| !"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;".equals(bootstrapMethodHandle.getDesc())) {
							super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
							return;
						}
						Object handleObj = bootstrapMethodArguments.length == 3 ? bootstrapMethodArguments[1] : null;
						if (!(handleObj instanceof Handle)) {
							super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
							return;
						}
						Handle handle = (Handle) handleObj;
						switch (handle.getTag()) {
							case Opcodes.H_INVOKESPECIAL:
							case Opcodes.H_INVOKEVIRTUAL:
							case Opcodes.H_INVOKESTATIC:
							case Opcodes.H_INVOKEINTERFACE:
								visitMethodInsn(handle.getTag(), handle.getOwner(), handle.getName(), handle.getDesc(), handle.isInterface());
						}
					}
				};
			}
		}, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
	}

	/**
	 * Called from the {@link ClassReader} in {@link #visit(JvmClassInfo)}.
	 * Links the given vertex to the remote {@link MethodVertex} of the resolved method call,
	 * if resolution is a success. When not successful, the call is recorded as an
	 * {@link #unresolvedCalls unresolved reference}.
	 *
	 * @param methodVertex
	 * 		The method that is doing the call.
	 * @param opcode
	 * 		Call opcode.
	 * @param owner
	 * 		Call owner.
	 * @param name
	 * 		Method call name.
	 * @param descriptor
	 * 		Method call descriptor.
	 * @param isInterface
	 * 		Method interface flag.
	 */
	private void onMethodCalled(MutableMethodVertex methodVertex, int opcode, String owner, String name, String descriptor, boolean isInterface) {
		JvmClassInfo ownerClass = lookup.apply(owner);

		// Skip if we cannot resolve owner
		if (ownerClass == null) {
			unresolvedCalls.put(owner, new MethodRef(owner, name, descriptor));
			return;
		}

		// Resolve the method
		Result<Resolution<JvmClassInfo, MethodMember>> resolutionResult;
		LinkedClass linkedOwnerClass = linked(ownerClass);
		switch (opcode) {
			case Opcodes.INVOKEVIRTUAL:
				resolutionResult = resolver.resolveVirtualMethod(linkedOwnerClass, name, descriptor);
				break;
			case Opcodes.INVOKESPECIAL:
				MemberInfo<MethodMember> method = linkedOwnerClass.getMethod(name, descriptor);
				if (method != null)
					resolutionResult = Result.ok(new Resolution<>(linkedOwnerClass, method, false));
				else
					resolutionResult = Result.error(ResolutionError.NO_SUCH_METHOD);
				break;
			case Opcodes.INVOKESTATIC:
				resolutionResult = resolver.resolveStaticMethod(linkedOwnerClass, name, descriptor);
				break;
			case Opcodes.INVOKEINTERFACE:
				resolutionResult = resolver.resolveInterfaceMethod(linkedOwnerClass, name, descriptor);
				break;
			default:
				throw new IllegalArgumentException("Invalid method opcode: " + opcode);
		}

		// Handle result
		if (resolutionResult.isSuccess()) {
			// Extract vertex from resolution
			Resolution<JvmClassInfo, MethodMember> resolution = resolutionResult.value();
			ClassMethodsContainer resolvedClass = getClassMethodsContainer(resolution.owner().innerValue());
			MutableMethodVertex resolvedMethodCallVertex = (MutableMethodVertex) resolvedClass.getVertex(resolution.member().innerValue());

			// Link the vertices
			methodVertex.getCalls().add(resolvedMethodCallVertex);
			resolvedMethodCallVertex.getCallers().add(methodVertex);
		} else {
			unresolvedCalls.put(owner, new MethodRef(owner, name, descriptor));
		}
	}

	@Override
	public void onAddLibrary(Workspace workspace, WorkspaceResource library) {
		// TODO: Update unresolved
	}

	@Override
	public void onRemoveLibrary(Workspace workspace, WorkspaceResource library) {
		// TODO: Reset
	}

	@Override
	public void onNewClass(WorkspaceResource resource, JvmClassBundle bundle, JvmClassInfo cls) {
		// TODO: Update unresolved
	}

	@Override
	public void onUpdateClass(WorkspaceResource resource, JvmClassBundle bundle, JvmClassInfo oldCls, JvmClassInfo newCls) {
		// TODO: Update affected vertices
	}

	@Override
	public void onRemoveClass(WorkspaceResource resource, JvmClassBundle bundle, JvmClassInfo cls) {
		// TODO: Reset affected
	}

	@Nonnull
	@Override
	public String getServiceId() {
		return SERVICE_ID;
	}

	@Nonnull
	@Override
	public CallGraphConfig getServiceConfig() {
		return config;
	}

	/**
	 * Mutable impl of {@link MethodVertex}.
	 */
	static class MutableMethodVertex implements MethodVertex {
		private final Set<MethodVertex> callers = Collections.synchronizedSet(new HashSet<>());
		private final Set<MethodVertex> calls = Collections.synchronizedSet(new HashSet<>());
		private final MethodRef method;
		private final MethodMember resolvedMethod;

		MutableMethodVertex(MethodRef method, MethodMember resolvedMethod) {
			this.method = method;
			this.resolvedMethod = resolvedMethod;
		}

		@Nonnull
		@Override
		public MethodRef getMethod() {
			return method;
		}

		@Nullable
		@Override
		public MethodMember getResolvedMethod() {
			return resolvedMethod;
		}

		@Nonnull
		@Override
		public Collection<MethodVertex> getCallers() {
			return callers;
		}

		@Nonnull
		@Override
		public Collection<MethodVertex> getCalls() {
			return calls;
		}

		@Override
		public String toString() {
			return method.toString();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			MutableMethodVertex vertex = (MutableMethodVertex) o;
			return method.equals(vertex.method);
		}

		@Override
		public int hashCode() {
			return method.hashCode();
		}
	}
}
