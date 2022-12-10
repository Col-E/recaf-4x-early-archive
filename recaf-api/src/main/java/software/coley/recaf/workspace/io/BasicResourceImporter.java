package software.coley.recaf.workspace.io;

import com.sun.tools.attach.VirtualMachineDescriptor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import software.coley.llzip.ZipArchive;
import software.coley.llzip.ZipIO;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.info.*;
import software.coley.recaf.info.builder.FileInfoBuilder;
import software.coley.recaf.info.properties.builtin.PathOriginalNameProperty;
import software.coley.recaf.info.properties.builtin.PathPrefixProperty;
import software.coley.recaf.info.properties.builtin.PathSuffixProperty;
import software.coley.recaf.info.properties.builtin.ZipCompressionProperty;
import software.coley.recaf.util.DexIOUtil;
import software.coley.recaf.util.IOUtil;
import software.coley.recaf.util.ModulesIOUtil;
import software.coley.recaf.util.io.ByteSource;
import software.coley.recaf.util.io.ByteSources;
import software.coley.recaf.util.io.LocalFileHeaderSource;
import software.coley.recaf.workspace.model.bundle.AndroidClassBundle;
import software.coley.recaf.workspace.model.bundle.BasicFileBundle;
import software.coley.recaf.workspace.model.bundle.BasicJvmClassBundle;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceFileResource;
import software.coley.recaf.workspace.model.resource.WorkspaceRemoteVmResource;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;
import software.coley.recaf.workspace.model.resource.WorkspaceResourceBuilder;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Basic implementation of the resource importer.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class BasicResourceImporter implements ResourceImporter {
	private static final Logger logger = Logging.get(BasicResourceImporter.class);
	private final InfoImporter infoImporter;

	@Inject
	public BasicResourceImporter(InfoImporter infoImporter) {
		this.infoImporter = infoImporter;
	}

	/**
	 * General read handling for any resource kind. Delegates to others when needed.
	 *
	 * @param builder
	 * 		Builder to work with.
	 * @param pathName
	 * 		Name of input file / content.
	 * @param source
	 * 		Access to content / data.
	 *
	 * @return Read resource.
	 */
	private WorkspaceResource handle(WorkspaceResourceBuilder builder,
									 String pathName, ByteSource source) throws IOException {
		// Read input as raw info in order to determine file-type.
		Info readInfo = infoImporter.readInfo(pathName, source);

		// Check if it is a single class.
		if (readInfo.isClass()) {
			// If it is a class, we know it MUST be a single JVM class since Android classes do not exist
			// in single file form. They only come bundled in DEX files.
			JvmClassInfo readAsJvmClass = readInfo.asClass().asJvmClass();
			BasicJvmClassBundle bundle = new BasicJvmClassBundle();
			bundle.initialPut(readAsJvmClass);

			// To satisfy our file-info requirement for the file resource we can create a wrapper file-info
			// using the JVM class's bytecode.
			FileInfo fileInfo = new FileInfoBuilder<>()
					.withName(readAsJvmClass.getName() + ".class")
					.withRawContent(readAsJvmClass.getBytecode())
					.build();
			return builder.withFileInfo(fileInfo)
					.withJvmClassBundle(bundle)
					.build();
		}

		// Must be some non-class type of file.
		FileInfo readInfoAsFile = readInfo.asFile();
		builder.withFileInfo(readInfoAsFile);

		// Check for general ZIP container format (ZIP/JAR/WAR/APK/JMod)
		if (readInfoAsFile.isZipFile()) {
			ZipFileInfo readInfoAsZip = readInfoAsFile.asZipFile();
			return handleZip(builder, readInfoAsZip, source);
		}

		// Must be some edge case type: Modules, or an unknown file type
		if (readInfoAsFile instanceof ModulesFileInfo) {
			return handleModules(builder, (ModulesFileInfo) readInfoAsFile);
		}

		// Unknown file type
		BasicFileBundle bundle = new BasicFileBundle();
		bundle.initialPut(readInfoAsFile);
		return builder
				.withFileBundle(bundle)
				.build();
	}

	private WorkspaceFileResource handleZip(WorkspaceResourceBuilder builder, ZipFileInfo zipInfo, ByteSource source) throws IOException {
		logger.info("Reading input from ZIP container '{}'", zipInfo.getName());
		builder.withFileInfo(zipInfo);
		BasicJvmClassBundle classes = new BasicJvmClassBundle();
		BasicFileBundle files = new BasicFileBundle();
		Map<String, AndroidClassBundle> androidClassBundles = new HashMap<>();
		NavigableMap<Integer, JvmClassBundle> versionedJvmClassBundles = new TreeMap<>();
		Map<String, WorkspaceFileResource> embeddedResources = new HashMap<>();

		// Read ZIP entries
		ZipArchive archive = ZipIO.readJvm(source.readAll());
		archive.getLocalFiles().forEach(header -> {
			LocalFileHeaderSource headerSource = new LocalFileHeaderSource(header);
			String entryName = header.getFileNameAsString();

			// Skip the following cases:
			//  - zero-length directories
			//  - path traversal attempts
			if (entryName.contains("//") || entryName.contains("../"))
				return;

			// Read the value of the entry to figure out how to handle adding it to the resource builder.
			Info info;
			try {
				info = infoImporter.readInfo(entryName, headerSource);
			} catch (IOException ex) {
				logger.error("IO error reading ZIP entry '{}' - skipping", entryName);
				return;
			}

			// Record common entry attributes
			ZipCompressionProperty.set(info, header.getCompressionMethod());
			// TODO: Additional ZIP properties

			// Handle the value
			if (info.isClass()) {
				// Must be a JVM class since Android classes do not exist in single-file form.
				JvmClassInfo classInfo = info.asClass().asJvmClass();
				String className = classInfo.getName();

				// First we must handle edge cases. Up first, we'll look at multi-release jar prefixes.
				if (entryName.startsWith(JarFileInfo.MULTI_RELEASE_PREFIX) &&
						!className.startsWith(JarFileInfo.MULTI_RELEASE_PREFIX)) {
					// Extract version from '<prefix>/version/<class-name>' pattern
					int startOffset = JarFileInfo.MULTI_RELEASE_PREFIX.length();
					String versionName = entryName.substring(startOffset, entryName.indexOf('/', startOffset));
					try {
						// Put it into the correct versioned class bundle.
						int version = Integer.parseInt(versionName);
						BasicJvmClassBundle bundle = (BasicJvmClassBundle) versionedJvmClassBundles
								.computeIfAbsent(version, v -> new BasicJvmClassBundle());
						bundle.initialPut(classInfo);
						return;
					} catch (NumberFormatException ex) {
						// Version is invalid, record it as a file instead.
						logger.warn("Class ZIP entry seemed to be for multi-release jar, " +
								"but version is non-numeric value: " + versionName);

						// Warn if there is also a duplicate file with the path.
						if (files.containsKey(entryName)) {
							logger.warn("Multiple duplicate entries in zip for class '{}', " +
									"dropping older entry", className);
						}

						// Override the prior value.
						// The JVM always selects the last option if there are duplicates.
						files.initialPut(new FileInfoBuilder<>()
								.withName(entryName)
								.withRawContent(classInfo.getBytecode())
								.build());
					}
				}

				// Handle duplicate classes by placing duplicates into the file bundle,
				// where their paths should be recognized as being unique.
				if (classes.containsKey(className)) {
					// Warn that there are multiple classes by the given name.
					// This won't occur for legit JAR files using multi-version release.
					// So the likely cases are:
					//  - ZIP is intentionally tampered with to include multiple entries by the same name
					//  - There is another legit case we do not account for in prior logic
					logger.warn("Class ZIP entry already recorded '{}' - Saving duplicate as file instead", className);

					// Warn if there is also a duplicate file with the path.
					if (files.containsKey(entryName)) {
						logger.warn("Multiple duplicate entries in zip for class '{}'," +
								" dropping older entry", className);
					}

					// Override the prior value.
					// The JVM always selects the last option if there are duplicates.
					files.initialPut(new FileInfoBuilder<>()
							.withName(entryName)
							.withRawContent(classInfo.getBytecode())
							.build());
				}

				// Record the class name, including path suffix/prefix.
				// If the name is totally different, record the original path name.
				int index = entryName.indexOf(className);
				if (index >= 0) {
					// Class name is within the entry name.
					// Record the prefix before the class name, and suffix after it (extension).
					if (index > 0) {
						String prefix = entryName.substring(0, index);
						PathPrefixProperty.set(classInfo, prefix);
					}
					int suffixIndex = index + className.length();
					if (suffixIndex < entryName.length()) {
						String suffix = entryName.substring(suffixIndex);
						PathSuffixProperty.set(classInfo, suffix);
					}
				} else {
					// Class name doesn't match entry name.
					PathOriginalNameProperty.set(classInfo, entryName);
				}

				classes.initialPut(classInfo);
			} else if (info.isFile()) {
				FileInfo fileInfo = info.asFile();

				// Check for special file cases (Currently just DEX)
				if (fileInfo instanceof DexFileInfo) {
					try {
						AndroidClassBundle dexBundle = DexIOUtil.read(headerSource);
						androidClassBundles.put(entryName, dexBundle);
						return;
					} catch (IOException ex) {
						logger.error("Failed to read embedded DEX '{}' in containing archive '{}'",
								entryName, zipInfo.getName(), ex);
					}
				}

				// Check for container file cases (Any ZIP type, JAR/WAR/etc)
				if (fileInfo.isZipFile()) {
					try {
						WorkspaceResourceBuilder embeddedResourceBuilder = new WorkspaceResourceBuilder()
								.withFileInfo(fileInfo);
						WorkspaceFileResource embeddedResource = handleZip(embeddedResourceBuilder,
								fileInfo.asZipFile(), headerSource);
						embeddedResources.put(entryName, embeddedResource);
					} catch (IOException ex) {
						logger.error("Failed to read embedded ZIP '{}' in containing archive '{}'",
								entryName, zipInfo.getName(), ex);
					}
					return;
				}

				// Warn if there are duplicate file entries.
				// Same cases for why this may occur are described above when handling classes.
				// The JVM will always use the last item for duplicate entries anyways.
				if (files.containsKey(entryName)) {
					logger.warn("Multiple duplicate entries in zip for file '{}', dropping older entry", entryName);
				}

				// Store in bundle.
				files.initialPut(fileInfo);
			} else {
				throw new IllegalStateException("Unknown info type: " + info);
			}
		});
		return builder
				.withJvmClassBundle(classes)
				.withAndroidClassBundles(androidClassBundles)
				.withVersionedJvmClassBundles(versionedJvmClassBundles)
				.withFileBundle(files)
				.withEmbeddedResources(embeddedResources)
				.withFileInfo(zipInfo)
				.build();
	}

	private WorkspaceResource handleModules(WorkspaceResourceBuilder builder, ModulesFileInfo moduleInfo) throws IOException {
		BasicJvmClassBundle classes = new BasicJvmClassBundle();
		BasicFileBundle files = new BasicFileBundle();

		// The file-info name should be an absolute path for any non-uri driven import.
		// We have to use a path because unless we implement our own module reader, the internal API
		// only provides reader access via a path item.
		Path pathToModuleFile = Paths.get(moduleInfo.asFile().getName());
		ModulesIOUtil.stream(pathToModuleFile)
				.forEach(entry -> {
					// Follows the pattern: /<module-name>/<file-name>
					//  - entry extracts these values
					ModulesIOUtil.Entry moduleEntry = entry.getElement();
					ByteSource moduleFileSource = entry.getByteSource();
					Info info;
					try {
						info = infoImporter.readInfo(moduleEntry.getFileName(), moduleFileSource);
					} catch (IOException ex) {
						logger.error("IO error reading modules entry '{}' - skipping", moduleEntry.getOriginalPath());
						return;
					}
					// Add to appropriate bundle.
					// Modules file only has two expected kinds of content, classes and generic files.
					if (info.isClass()) {
						// Modules file only contains JVM classes
						classes.initialPut(info.asClass().asJvmClass());
					} else {
						// Anything else should be a general file
						files.initialPut(info.asFile());
					}

					// Record the original prefix '/<module-name>/' for the input
					PathPrefixProperty.set(info, "/" + moduleEntry.getModuleName() + "/");
				});

		return builder
				.withJvmClassBundle(classes)
				.withFileBundle(files)
				.build();
	}

	@Override
	public WorkspaceResource importResource(ByteSource source) throws IOException {
		return handle(new WorkspaceResourceBuilder(), "unknown.dat", source);
	}

	@Override
	public WorkspaceFileResource importResource(Path path) throws IOException {
		// Load name/data from path, parse into resource.
		String absolutePath = path.toAbsolutePath().toString();
		ByteSource byteSource = ByteSources.forPath(path);
		return (WorkspaceFileResource) handle(new WorkspaceResourceBuilder(), absolutePath, byteSource);
	}

	@Override
	public WorkspaceFileResource importResource(URL url) throws IOException {
		// Extract name from URL
		String path = url.getFile();
		if (path.isEmpty()) {
			path = url.toString();
		}

		// Load content, parse into resource.
		byte[] bytes = IOUtil.toByteArray(url.openStream());
		ByteSource byteSource = ByteSources.wrap(bytes);
		return (WorkspaceFileResource) handle(new WorkspaceResourceBuilder(), path, byteSource);
	}

	@Override
	public WorkspaceRemoteVmResource importResource(VirtualMachineDescriptor virtualMachineDescriptor) {
		// TODO: copy over instrumentation-service from dev3, then @Inject the interface
		throw new UnsupportedOperationException("Instrumentation support not yet implemented");
	}
}
