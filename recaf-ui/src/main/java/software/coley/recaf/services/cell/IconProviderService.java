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
 * Provides support for providing icons for a variety of item types.
 * For instance, the graphics of {@link WorkspaceTreeCell} instances.
 * <br>
 * The icons displayed in the UI can be swapped out by supplying your own
 * {@link IconProviderFactory} instances to the overrides:
 * <ul>
 *     <li>{@link #setClassIconProviderOverride(ClassIconProviderFactory)}</li>
 *     <li>{@link #setFileIconProviderOverride(FileIconProviderFactory)}</li>
 *     <li>{@link #setPackageIconProviderOverride(PackageIconProviderFactory)}</li>
 *     <li>{@link #setDirectoryIconProviderOverride(DirectoryIconProviderFactory)}</li>
 *     <li>{@link #setBundleIconProviderOverride(BundleIconProviderFactory)}</li>
 *     <li>{@link #setResourceIconProviderOverride(ResourceIconProviderFactory)}</li>
 * </ul>
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class IconProviderService implements Service {
	public static final String SERVICE_ID = "cell-icons";
	private final IconProviderServiceConfig config;
	// Defaults
	private final ClassIconProviderFactory classIconDefault;
	private final FileIconProviderFactory fileIconDefault;
	private final PackageIconProviderFactory packageIconDefault;
	private final DirectoryIconProviderFactory directoryIconDefault;
	private final BundleIconProviderFactory bundleIconDefault;
	private final ResourceIconProviderFactory resourceIconDefault;
	// Overrides
	private ClassIconProviderFactory classIconOverride;
	private FileIconProviderFactory fileIconOverride;
	private PackageIconProviderFactory packageIconOverride;
	private DirectoryIconProviderFactory directoryIconOverride;
	private BundleIconProviderFactory bundleIconOverride;
	private ResourceIconProviderFactory resourceIconOverride;

	@Inject
	public IconProviderService(@Nonnull IconProviderServiceConfig config,
							   @Nonnull ClassIconProviderFactory classIconDefault,
							   @Nonnull FileIconProviderFactory fileIconDefault,
							   @Nonnull PackageIconProviderFactory packageIconDefault,
							   @Nonnull DirectoryIconProviderFactory directoryIconDefault,
							   @Nonnull BundleIconProviderFactory bundleIconDefault,
							   @Nonnull ResourceIconProviderFactory resourceIconDefault) {
		this.config = config;

		// Default factories
		this.classIconDefault = classIconDefault;
		this.fileIconDefault = fileIconDefault;
		this.packageIconDefault = packageIconDefault;
		this.directoryIconDefault = directoryIconDefault;
		this.bundleIconDefault = bundleIconDefault;
		this.resourceIconDefault = resourceIconDefault;
	}

	/**
	 * Delegates to {@link ClassIconProviderFactory}.
	 *
	 * @param workspace
	 * 		Containing workspace.
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param info
	 * 		The class to create an icon for.
	 *
	 * @return Icon provider for the class.
	 */
	@Nonnull
	public IconProvider getJvmClassInfoIconProvider(@Nonnull Workspace workspace,
													@Nonnull WorkspaceResource resource,
													@Nonnull JvmClassBundle bundle,
													@Nonnull JvmClassInfo info) {
		ClassIconProviderFactory factory = classIconOverride != null ? classIconOverride : classIconDefault;
		return factory.getJvmClassInfoIconProvider(workspace, resource, bundle, info);
	}

	/**
	 * Delegates to {@link ClassIconProviderFactory}.
	 *
	 * @param workspace
	 * 		Containing workspace.
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param info
	 * 		The class to create an icon for.
	 *
	 * @return Icon provider for the class.
	 */
	@Nonnull
	public IconProvider getAndroidClassInfoIconProvider(@Nonnull Workspace workspace,
														@Nonnull WorkspaceResource resource,
														@Nonnull AndroidClassBundle bundle,
														@Nonnull AndroidClassInfo info) {
		ClassIconProviderFactory factory = classIconOverride != null ? classIconOverride : classIconDefault;
		return factory.getAndroidClassInfoIconProvider(workspace, resource, bundle, info);
	}

	/**
	 * Delegates to {@link FileIconProviderFactory}.
	 *
	 * @param workspace
	 * 		Containing workspace.
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		Containing bundle.
	 * @param info
	 * 		The file to create an icon for.
	 *
	 * @return Icon provider for the file.
	 */
	@Nonnull
	public IconProvider getFileInfoIconProvider(@Nonnull Workspace workspace,
												@Nonnull WorkspaceResource resource,
												@Nonnull FileBundle bundle,
												@Nonnull FileInfo info) {
		FileIconProviderFactory factory = fileIconOverride != null ? fileIconOverride : fileIconDefault;
		return factory.getFileInfoIconProvider(workspace, resource, bundle, info);
	}

	/**
	 * Delegates to {@link PackageIconProviderFactory}.
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
	 * @return Icon provider for the package.
	 */
	@Nonnull
	public IconProvider getPackageIconProvider(@Nonnull Workspace workspace,
											   @Nonnull WorkspaceResource resource,
											   @Nonnull ClassBundle<? extends ClassInfo> bundle,
											   @Nonnull String packageName) {
		PackageIconProviderFactory factory = packageIconOverride != null ? packageIconOverride : packageIconDefault;
		return factory.getPackageIconProvider(workspace, resource, bundle, packageName);
	}

	/**
	 * Delegates to {@link DirectoryIconProviderFactory}.
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
	 * @return Icon provider for the directory.
	 */
	@Nonnull
	public IconProvider getDirectoryIconProvider(@Nonnull Workspace workspace,
												 @Nonnull WorkspaceResource resource,
												 @Nonnull FileBundle bundle,
												 @Nonnull String directoryName) {
		DirectoryIconProviderFactory factory = directoryIconOverride != null ? directoryIconOverride : directoryIconDefault;
		return factory.getDirectoryIconProvider(workspace, resource, bundle, directoryName);
	}

	/**
	 * Delegates to {@link BundleIconProviderFactory}.
	 *
	 * @param workspace
	 * 		Containing workspace.
	 * @param resource
	 * 		Containing resource.
	 * @param bundle
	 * 		The bundle to create an icon for.
	 *
	 * @return Icon provider for the bundle.
	 */
	@Nonnull
	public IconProvider getBundleIconProvider(@Nonnull Workspace workspace,
											  @Nonnull WorkspaceResource resource,
											  @Nonnull Bundle<? extends Info> bundle) {
		BundleIconProviderFactory factory = bundleIconOverride != null ? bundleIconOverride : bundleIconDefault;
		return factory.getBundleIconProvider(workspace, resource, bundle);
	}

	/**
	 * Delegates to {@link ResourceIconProviderFactory}.
	 *
	 * @param workspace
	 * 		Containing workspace.
	 * @param resource
	 * 		The resource to create an icon for.
	 *
	 * @return Icon provider for the resource.
	 */
	@Nonnull
	public IconProvider getResourceIconProvider(@Nonnull Workspace workspace,
												@Nonnull WorkspaceResource resource) {
		ResourceIconProviderFactory factory = resourceIconOverride != null ? resourceIconOverride : resourceIconDefault;
		return factory.getResourceIconProvider(workspace, resource);
	}

	/**
	 * @return Default icon provider for classes.
	 */
	@Nonnull
	public ClassIconProviderFactory getClassIconDefault() {
		return classIconDefault;
	}

	/**
	 * @return Default icon provider for files.
	 */
	@Nonnull
	public FileIconProviderFactory getFileIconDefault() {
		return fileIconDefault;
	}

	/**
	 * @return Default icon provider for packages.
	 */
	@Nonnull
	public PackageIconProviderFactory getPackageIconDefault() {
		return packageIconDefault;
	}

	/**
	 * @return Default icon provider for directories.
	 */
	@Nonnull
	public DirectoryIconProviderFactory getDirectoryIconDefault() {
		return directoryIconDefault;
	}

	/**
	 * @return Default icon provider for bundles.
	 */
	@Nonnull
	public BundleIconProviderFactory getBundleIconDefault() {
		return bundleIconDefault;
	}

	/**
	 * @return Default icon provider for resources.
	 */
	@Nonnull
	public ResourceIconProviderFactory getResourceIconDefault() {
		return resourceIconDefault;
	}

	/**
	 * @return Override factory for supplying class icon providers.
	 */
	@Nullable
	public ClassIconProviderFactory getClassIconProviderOverride() {
		return classIconOverride;
	}

	/**
	 * @param classIconOverride
	 * 		Override factory for supplying class icon providers.
	 */
	public void setClassIconProviderOverride(@Nullable ClassIconProviderFactory classIconOverride) {
		this.classIconOverride = classIconOverride;
	}

	/**
	 * @return Override factory for supplying file icon providers.
	 */
	@Nullable
	public FileIconProviderFactory getFileIconProviderOverride() {
		return fileIconOverride;
	}

	/**
	 * @param fileIconOverride
	 * 		Override factory for supplying file icon providers.
	 */
	public void setFileIconProviderOverride(@Nullable FileIconProviderFactory fileIconOverride) {
		this.fileIconOverride = fileIconOverride;
	}

	/**
	 * @return Override factory for supplying package icon providers.
	 */
	@Nullable
	public PackageIconProviderFactory getPackageIconProviderOverride() {
		return packageIconOverride;
	}

	/**
	 * @param packageIconOverride
	 * 		Override factory for supplying package icon providers.
	 */
	public void setPackageIconProviderOverride(@Nullable PackageIconProviderFactory packageIconOverride) {
		this.packageIconOverride = packageIconOverride;
	}

	/**
	 * @return Override factory for supplying directory icon providers.
	 */
	@Nullable
	public DirectoryIconProviderFactory getDirectoryIconProviderOverride() {
		return directoryIconOverride;
	}

	/**
	 * @param directoryIconOverride
	 * 		Override factory for supplying directory icon providers.
	 */
	public void setDirectoryIconProviderOverride(@Nullable DirectoryIconProviderFactory directoryIconOverride) {
		this.directoryIconOverride = directoryIconOverride;
	}

	/**
	 * @return Override factory for supplying bundle icon providers.
	 */
	@Nullable
	public BundleIconProviderFactory getBundleIconProviderOverride() {
		return bundleIconOverride;
	}

	/**
	 * @param bundleIconOverride
	 * 		Override factory for supplying bundle icon providers.
	 */
	public void setBundleIconProviderOverride(@Nullable BundleIconProviderFactory bundleIconOverride) {
		this.bundleIconOverride = bundleIconOverride;
	}

	/**
	 * @return Override factory for supplying resource icon providers.
	 */
	@Nullable
	public ResourceIconProviderFactory getResourceIconProviderOverride() {
		return resourceIconOverride;
	}

	/**
	 * @param resourceIconOverride
	 * 		Override factory for supplying resource icon providers.
	 */
	public void setResourceIconProviderOverride(@Nullable ResourceIconProviderFactory resourceIconOverride) {
		this.resourceIconOverride = resourceIconOverride;
	}

	@Nonnull
	@Override
	public String getServiceId() {
		return SERVICE_ID;
	}

	@Nonnull
	@Override
	public IconProviderServiceConfig getServiceConfig() {
		return config;
	}
}
