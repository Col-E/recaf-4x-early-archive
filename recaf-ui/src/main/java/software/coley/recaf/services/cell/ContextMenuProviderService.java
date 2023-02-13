package software.coley.recaf.services.cell;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.coley.recaf.info.*;
import software.coley.recaf.services.Service;
import software.coley.recaf.ui.control.tree.WorkspaceTreeCell;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.*;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

/**
 * Provides support for providing context menus for a variety of item types.
 * For instance, the menus of {@link WorkspaceTreeCell} instances.
 * <br>
 * The menus displayed in the UI can be swapped out by supplying your own
 * {@link ContextMenuProviderFactory} instances to the overrides:
 * <ul>
 *     <li>{@link #setClassContextMenuProviderOverride(ClassContextMenuProviderFactory)}</li>
 *     <li>{@link #setFileContextMenuProviderOverride(FileContextMenuProviderFactory)}</li>
 *     <li>{@link #setPackageContextMenuProviderOverride(PackageContextMenuProviderFactory)}</li>
 *     <li>{@link #setDirectoryContextMenuProviderOverride(DirectoryContextMenuProviderFactory)}</li>
 *     <li>{@link #setBundleContextMenuProviderOverride(BundleContextMenuProviderFactory)}</li>
 *     <li>{@link #setResourceContextMenuProviderOverride(ResourceContextMenuProviderFactory)}</li>
 * </ul>
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class ContextMenuProviderService implements Service {
	public static final String SERVICE_ID = "cell-menus";
	private final ContextMenuProviderServiceConfig config;
	// Defaults
	private final ClassContextMenuProviderFactory classContextMenuDefault;
	private final FileContextMenuProviderFactory fileContextMenuDefault;
	private final PackageContextMenuProviderFactory packageContextMenuDefault;
	private final DirectoryContextMenuProviderFactory directoryContextMenuDefault;
	private final BundleContextMenuProviderFactory bundleContextMenuDefault;
	private final ResourceContextMenuProviderFactory resourceContextMenuDefault;
	// Overrides
	private ClassContextMenuProviderFactory classContextMenuOverride;
	private FileContextMenuProviderFactory fileContextMenuOverride;
	private PackageContextMenuProviderFactory packageContextMenuOverride;
	private DirectoryContextMenuProviderFactory directoryContextMenuOverride;
	private BundleContextMenuProviderFactory bundleContextMenuOverride;
	private ResourceContextMenuProviderFactory resourceContextMenuOverride;

	@Inject
	public ContextMenuProviderService(@Nonnull ContextMenuProviderServiceConfig config,
									  @Nonnull ClassContextMenuProviderFactory classContextMenuDefault,
									  @Nonnull FileContextMenuProviderFactory fileContextMenuDefault,
									  @Nonnull PackageContextMenuProviderFactory packageContextMenuDefault,
									  @Nonnull DirectoryContextMenuProviderFactory directoryContextMenuDefault,
									  @Nonnull BundleContextMenuProviderFactory bundleContextMenuDefault,
									  @Nonnull ResourceContextMenuProviderFactory resourceContextMenuDefault) {
		this.config = config;

		// Default factories
		this.classContextMenuDefault = classContextMenuDefault;
		this.fileContextMenuDefault = fileContextMenuDefault;
		this.packageContextMenuDefault = packageContextMenuDefault;
		this.directoryContextMenuDefault = directoryContextMenuDefault;
		this.bundleContextMenuDefault = bundleContextMenuDefault;
		this.resourceContextMenuDefault = resourceContextMenuDefault;

		// TODO: Factories for (here and in other services)
		//  - inner classes of ClassInfo
		//  - fields of ClassInfo
		//  - methods of ClassInfo
	}

	/**
	 * Delegates to {@link ClassContextMenuProviderFactory}.
	 *
	 * @param workspace
	 * 		Containing workspace.
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param info
	 * 		The class to create a menu for.
	 *
	 * @return Menu provider for the class.
	 */
	@Nonnull
	public ContextMenuProvider getJvmClassInfoContextMenuProvider(@Nonnull Workspace workspace,
																  @Nonnull WorkspaceResource resource,
																  @Nonnull JvmClassBundle bundle,
																  @Nonnull JvmClassInfo info) {
		ClassContextMenuProviderFactory factory = classContextMenuOverride != null ? classContextMenuOverride : classContextMenuDefault;
		return factory.getJvmClassInfoContextMenuProvider(workspace, resource, bundle, info);
	}

	/**
	 * Delegates to {@link ClassContextMenuProviderFactory}.
	 *
	 * @param workspace
	 * 		Containing workspace.
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param info
	 * 		The class to create a menu for.
	 *
	 * @return Menu provider for the class.
	 */
	@Nonnull
	public ContextMenuProvider getAndroidClassInfoContextMenuProvider(@Nonnull Workspace workspace,
																	  @Nonnull WorkspaceResource resource,
																	  @Nonnull AndroidClassBundle bundle,
																	  @Nonnull AndroidClassInfo info) {
		ClassContextMenuProviderFactory factory = classContextMenuOverride != null ? classContextMenuOverride : classContextMenuDefault;
		return factory.getAndroidClassInfoContextMenuProvider(workspace, resource, bundle, info);
	}

	/**
	 * Delegates to {@link FileContextMenuProviderFactory}.
	 *
	 * @param workspace
	 * 		Containing workspace.
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param info
	 * 		The file to create a menu for.
	 *
	 * @return Menu provider for the file.
	 */
	@Nonnull
	public ContextMenuProvider getFileInfoContextMenuProvider(@Nonnull Workspace workspace,
															  @Nonnull WorkspaceResource resource,
															  @Nonnull FileBundle bundle,
															  @Nonnull FileInfo info) {
		FileContextMenuProviderFactory factory = fileContextMenuOverride != null ? fileContextMenuOverride : fileContextMenuDefault;
		return factory.getFileInfoContextMenuProvider(workspace, resource, bundle, info);
	}

	/**
	 * Delegates to {@link PackageContextMenuProviderFactory}.
	 *
	 * @param workspace
	 * 		Containing workspace.
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param packageName
	 * 		The full package name, separated by {@code /}.
	 *
	 * @return Menu provider for the package.
	 */
	@Nonnull
	public ContextMenuProvider getPackageContextMenuProvider(@Nonnull Workspace workspace,
															 @Nonnull WorkspaceResource resource,
															 @Nonnull ClassBundle<? extends ClassInfo> bundle,
															 @Nonnull String packageName) {
		PackageContextMenuProviderFactory factory = packageContextMenuOverride != null ? packageContextMenuOverride : packageContextMenuDefault;
		return factory.getPackageContextMenuProvider(workspace, resource, bundle, packageName);
	}

	/**
	 * Delegates to {@link DirectoryContextMenuProviderFactory}.
	 *
	 * @param workspace
	 * 		Containing workspace.
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param directoryName
	 * 		The full path of the directory.
	 *
	 * @return Menu provider for the directory.
	 */
	@Nonnull
	public ContextMenuProvider getDirectoryContextMenuProvider(@Nonnull Workspace workspace,
															   @Nonnull WorkspaceResource resource,
															   @Nonnull FileBundle bundle,
															   @Nonnull String directoryName) {
		DirectoryContextMenuProviderFactory factory = directoryContextMenuOverride != null ? directoryContextMenuOverride : directoryContextMenuDefault;
		return factory.getDirectoryContextMenuProvider(workspace, resource, bundle, directoryName);
	}

	/**
	 * Delegates to {@link BundleContextMenuProviderFactory}.
	 *
	 * @param workspace
	 * 		Containing workspace.
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		The bundle to create a menu for.
	 *
	 * @return Menu provider for the bundle.
	 */
	@Nonnull
	public ContextMenuProvider getBundleContextMenuProvider(@Nonnull Workspace workspace,
															@Nonnull WorkspaceResource resource,
															@Nonnull Bundle<? extends Info> bundle) {
		BundleContextMenuProviderFactory factory = bundleContextMenuOverride != null ? bundleContextMenuOverride : bundleContextMenuDefault;
		return factory.getBundleContextMenuProvider(workspace, resource, bundle);
	}

	/**
	 * Delegates to {@link ResourceContextMenuProviderFactory}.
	 *
	 * @param workspace
	 * 		Containing workspace.
	 * @param resource
	 * 		The resource to create a menu for.
	 *
	 * @return Menu provider for the resource.
	 */
	@Nonnull
	public ContextMenuProvider getResourceContextMenuProvider(@Nonnull Workspace workspace,
															  @Nonnull WorkspaceResource resource) {
		ResourceContextMenuProviderFactory factory = resourceContextMenuOverride != null ? resourceContextMenuOverride : resourceContextMenuDefault;
		return factory.getResourceContextMenuProvider(workspace, resource);
	}

	/**
	 * @return Default menu provider for classes.
	 */
	@Nonnull
	public ClassContextMenuProviderFactory getClassContextMenuDefault() {
		return classContextMenuDefault;
	}

	/**
	 * @return Default menu provider for files.
	 */
	@Nonnull
	public FileContextMenuProviderFactory getFileContextMenuDefault() {
		return fileContextMenuDefault;
	}

	/**
	 * @return Default menu provider for packages.
	 */
	@Nonnull
	public PackageContextMenuProviderFactory getPackageContextMenuDefault() {
		return packageContextMenuDefault;
	}

	/**
	 * @return Default menu provider for directories.
	 */
	@Nonnull
	public DirectoryContextMenuProviderFactory getDirectoryContextMenuDefault() {
		return directoryContextMenuDefault;
	}

	/**
	 * @return Default menu provider for bundles.
	 */
	@Nonnull
	public BundleContextMenuProviderFactory getBundleContextMenuDefault() {
		return bundleContextMenuDefault;
	}

	/**
	 * @return Default menu provider for resources.
	 */
	@Nonnull
	public ResourceContextMenuProviderFactory getResourceContextMenuDefault() {
		return resourceContextMenuDefault;
	}

	/**
	 * @return Override factory for supplying class menu providers.
	 */
	@Nullable
	public ClassContextMenuProviderFactory getClassContextMenuProviderOverride() {
		return classContextMenuOverride;
	}

	/**
	 * @param classContextMenuOverride
	 * 		Override factory for supplying class menu providers.
	 */
	public void setClassContextMenuProviderOverride(@Nullable ClassContextMenuProviderFactory classContextMenuOverride) {
		this.classContextMenuOverride = classContextMenuOverride;
	}

	/**
	 * @return Override factory for supplying file menu providers.
	 */
	@Nullable
	public FileContextMenuProviderFactory getFileContextMenuProviderOverride() {
		return fileContextMenuOverride;
	}

	/**
	 * @param fileContextMenuOverride
	 * 		Override factory for supplying file menu providers.
	 */
	public void setFileContextMenuProviderOverride(@Nullable FileContextMenuProviderFactory fileContextMenuOverride) {
		this.fileContextMenuOverride = fileContextMenuOverride;
	}

	/**
	 * @return Override factory for supplying package menu providers.
	 */
	@Nullable
	public PackageContextMenuProviderFactory getPackageContextMenuProviderOverride() {
		return packageContextMenuOverride;
	}

	/**
	 * @param packageContextMenuOverride
	 * 		Override factory for supplying package menu providers.
	 */
	public void setPackageContextMenuProviderOverride(@Nullable PackageContextMenuProviderFactory packageContextMenuOverride) {
		this.packageContextMenuOverride = packageContextMenuOverride;
	}

	/**
	 * @return Override factory for supplying directory menu providers.
	 */
	@Nullable
	public DirectoryContextMenuProviderFactory getDirectoryContextMenuProviderOverride() {
		return directoryContextMenuOverride;
	}

	/**
	 * @param directoryContextMenuOverride
	 * 		Override factory for supplying directory menu providers.
	 */
	public void setDirectoryContextMenuProviderOverride(@Nullable DirectoryContextMenuProviderFactory directoryContextMenuOverride) {
		this.directoryContextMenuOverride = directoryContextMenuOverride;
	}

	/**
	 * @return Override factory for supplying bundle menu providers.
	 */
	@Nullable
	public BundleContextMenuProviderFactory getBundleContextMenuProviderOverride() {
		return bundleContextMenuOverride;
	}

	/**
	 * @param bundleContextMenuOverride
	 * 		Override factory for supplying bundle menu providers.
	 */
	public void setBundleContextMenuProviderOverride(@Nullable BundleContextMenuProviderFactory bundleContextMenuOverride) {
		this.bundleContextMenuOverride = bundleContextMenuOverride;
	}

	/**
	 * @return Override factory for supplying resource menu providers.
	 */
	@Nullable
	public ResourceContextMenuProviderFactory getResourceContextMenuProviderOverride() {
		return resourceContextMenuOverride;
	}

	/**
	 * @param resourceContextMenuOverride
	 * 		Override factory for supplying resource menu providers.
	 */
	public void setResourceContextMenuProviderOverride(@Nullable ResourceContextMenuProviderFactory resourceContextMenuOverride) {
		this.resourceContextMenuOverride = resourceContextMenuOverride;
	}

	@Nonnull
	@Override
	public String getServiceId() {
		return SERVICE_ID;
	}

	@Nonnull
	@Override
	public ContextMenuProviderServiceConfig getServiceConfig() {
		return config;
	}
}
