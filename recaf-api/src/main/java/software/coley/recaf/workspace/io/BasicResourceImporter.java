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
import software.coley.recaf.info.properties.builtin.OriginalPathProperty;
import software.coley.recaf.util.IOUtil;
import software.coley.recaf.util.ModulesIOUtil;
import software.coley.recaf.util.NumberUtil;
import software.coley.recaf.util.io.ByteSource;
import software.coley.recaf.util.io.ByteSources;
import software.coley.recaf.util.io.LocalFileHeaderSource;
import software.coley.recaf.workspace.model.bundle.AndroidClassBundle;
import software.coley.recaf.workspace.model.bundle.BasicFileBundle;
import software.coley.recaf.workspace.model.bundle.BasicJvmClassBundle;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;
import software.coley.recaf.workspace.model.resource.*;

import java.io.IOException;
import java.net.URISyntaxException;
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

	private WorkspaceResource handleZip(WorkspaceResourceBuilder builder, ZipFileInfo zipInfo, ByteSource source) throws IOException {
		logger.info("Reading input from ZIP container '{}'", zipInfo.getName());
		BasicJvmClassBundle classes = new BasicJvmClassBundle();
		BasicFileBundle files = new BasicFileBundle();
		Map<String, AndroidClassBundle> androidClassBundles = new HashMap<>();
		NavigableMap<Integer, JvmClassBundle> versionedJvmClassBundles = new TreeMap<>();
		Map<String, WorkspaceResource> embeddedResources = new HashMap<>();

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

			Info info = infoImporter.readInfo(entryName, headerSource);
			if (info.isClass()) {
				// Must be a JVM class since Android classes do not exist in single-file form.
				JvmClassInfo classInfo = info.asClass().asJvmClass();
				String className = classInfo.getName();

				// First we must handle edge cases. Up first, we'll look at multi-release jar prefixes.
				if (entryName.startsWith(JvmClassInfo.MULTI_RELEASE_PREFIX) &&
						!className.startsWith(JvmClassInfo.MULTI_RELEASE_PREFIX)) {
					// Extract version from '<prefix>/version/<class-name>' pattern
					int startOffset = JvmClassInfo.MULTI_RELEASE_PREFIX.length();
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

				// Record the class, adding a prefix/original-name property if the entry name in the ZIP
				// does not align with the class's name.
				String expectedEntryName = className + ".class";
				if (entryName.equals(expectedEntryName)) {
					// Entry matches class name.
				} else if (entryName.endsWith(expectedEntryName)) {
					// Entry has a prefix before the class name.
					String prefix = entryName.substring(0, entryName.length() - expectedEntryName.length());
					// Record prefix as property.
					OriginalPathProperty.setPrefix(classInfo, prefix);
					logger.debug("Class ZIP entry has prefix: '{}' for '{}'", prefix, className);
				} else {
					// Entry has different name than class, and is not just a prefix.
					// Record entry name as property.
					OriginalPathProperty.setOriginalName(classInfo, entryName);
					logger.debug("Class ZIP entry does not match file path: '{}' vs '{}'", entryName, className);
				}
				classes.initialPut(classInfo);
			} else if (info.isFile()) {
				FileInfo fileInfo = info.asFile();

				// Check for special file case
				if (fileInfo instanceof DexFileInfo) {
					// TODO: --> android dex bundle
					return;
				}

				// Check for container file cases (Any ZIP type, JAR/WAR/etc)
				if (fileInfo.isZipFile()) {
					try {
						WorkspaceResourceBuilder embeddedResourceBuilder = new WorkspaceResourceBuilder()
								.withFileInfo(fileInfo);
						WorkspaceResource embeddedResource = handleZip(embeddedResourceBuilder,
								fileInfo.asZipFile(), headerSource);
						embeddedResources.put(entryName, embeddedResource);
					} catch (IOException ex) {
						logger.error("Failed to read embedded ZIP '{}' in containing archive '{}",
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
					Info info = infoImporter.readInfo(moduleEntry.getFileName(), moduleFileSource);

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
					OriginalPathProperty.setPrefix(info, "/" + moduleEntry.getModuleName() + "/");
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
		// Initialize builder with file path
		WorkspaceResourceBuilder builder = new WorkspaceResourceBuilder().withFilePath(path);

		// Load name/data from path, parse into resource.
		String absolutePath = path.toAbsolutePath().toString();
		ByteSource byteSource = ByteSources.forPath(path);
		return (WorkspaceFileResource) handle(builder, absolutePath, byteSource);
	}

	@Override
	public WorkspaceUriResource importResource(URL url) throws IOException {
		try {
			// Initialize builder with URL
			WorkspaceResourceBuilder builder = new WorkspaceResourceBuilder().withUri(url.toURI());

			// Extract name from URL
			String path = url.getFile();
			if (path.isEmpty()) {
				path = url.toString();
			}

			// Load content, parse into resource.
			byte[] bytes = IOUtil.toByteArray(url.openStream());
			ByteSource byteSource = ByteSources.wrap(bytes);
			return (WorkspaceUriResource) handle(builder, path, byteSource);
		} catch (URISyntaxException ex) {
			throw new IOException("Unsupported URL scheme: " + url);
		}
	}

	@Override
	public WorkspaceRemoteVmResource importResource(VirtualMachineDescriptor virtualMachineDescriptor) {
		// TODO: copy over instrumentation-service from dev3, then @Inject the interface
		throw new UnsupportedOperationException("Instrumentation support not yet implemented");
	}
}
