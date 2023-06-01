package software.coley.recaf.ui.menubar;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.kordamp.ikonli.carbonicons.CarbonIcons;
import org.slf4j.Logger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.services.cell.CellConfigurationService;
import software.coley.recaf.services.mapping.IntermediateMappings;
import software.coley.recaf.services.mapping.MappingApplier;
import software.coley.recaf.services.mapping.MappingResults;
import software.coley.recaf.services.mapping.aggregate.AggregateMappingManager;
import software.coley.recaf.services.mapping.aggregate.AggregatedMappings;
import software.coley.recaf.services.mapping.format.MappingFileFormat;
import software.coley.recaf.services.mapping.format.MappingFormatManager;
import software.coley.recaf.services.window.WindowManager;
import software.coley.recaf.ui.config.RecentFilesConfig;
import software.coley.recaf.ui.control.FontIconView;
import software.coley.recaf.util.Lang;
import software.coley.recaf.util.threading.ThreadPoolFactory;
import software.coley.recaf.workspace.WorkspaceManager;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;

import static software.coley.recaf.util.Lang.getBinding;
import static software.coley.recaf.util.Menus.*;

/**
 * Mapping menu component for {@link MainMenu}.
 *
 * @author Matt Coley
 */
@Dependent
public class MappingMenu extends WorkspaceAwareMenu {
	private static final Logger logger = Logging.get(MappingMenu.class);
	private final ExecutorService exportPool = ThreadPoolFactory.newSingleThreadExecutor("mapping-export");
	private final ExecutorService importPool = ThreadPoolFactory.newSingleThreadExecutor("mapping-import");
	private final WindowManager windowManager;
	private final WorkspaceManager workspaceManager;
	private final AggregateMappingManager aggregateMappingManager;
	private final CellConfigurationService configurationService;

	@Inject
	public MappingMenu(@Nonnull WindowManager windowManager,
					   @Nonnull WorkspaceManager workspaceManager,
					   @Nonnull AggregateMappingManager aggregateMappingManager,
					   @Nonnull MappingFormatManager formatManager,
					   @Nonnull MappingApplier mappingApplier,
					   @Nonnull CellConfigurationService configurationService,
					   @Nonnull RecentFilesConfig recentFiles) {
		super(workspaceManager);

		this.windowManager = windowManager;
		this.workspaceManager = workspaceManager;
		this.aggregateMappingManager = aggregateMappingManager;
		this.configurationService = configurationService;

		textProperty().bind(getBinding("menu.mappings"));
		setGraphic(new FontIconView(CarbonIcons.MAP_BOUNDARY));

		Menu apply = menu("menu.mappings.apply", CarbonIcons.DOCUMENT_IMPORT);
		Menu export = menu("menu.mappings.export", CarbonIcons.DOCUMENT_EXPORT);

		// Use a shared file-chooser for mapping menu actions.
		// That way there is some continuity when working with mappings.
		FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(recentFiles.getLastWorkspaceOpenDirectory().unboxingMap(File::new));
		chooser.setTitle(Lang.get("dialog.file.open"));

		for (String formatName : formatManager.getMappingFileFormats()) {
			apply.getItems().add(actionLiteral(formatName, CarbonIcons.LICENSE, () -> {
				// Show the prompt, load the mappings text ant attempt to load them.
				File file = chooser.showOpenDialog(windowManager.getMainWindow());
				if (file != null) {
					importPool.submit(() -> {
						try {
							MappingFileFormat format = formatManager.createFormatInstance(formatName);
							String mappingsText = Files.readString(file.toPath());
							IntermediateMappings parsedMappings = format.parse(mappingsText);
							logger.info("Loaded mappings from {} in {} format", file.getName(), formatName);

							// TODO: UI to preview before/after using TreeMapPane
							MappingResults results = mappingApplier.applyToPrimaryResource(parsedMappings);
							results.apply();
							logger.info("Applied mappings from {}", file.getName());
						} catch (Exception ex) {
							logger.error("Failed to read mappings from {}", file.getName(), ex);
						}
					});
				}
			}));

			// Temp instance to check for export support.
			MappingFileFormat tmp = formatManager.createFormatInstance(formatName);
			if (tmp == null) continue;
			if (tmp.supportsExportText()) {
				export.getItems().add(actionLiteral(formatName, CarbonIcons.LICENSE, () -> {
					// Show the prompt, write current mappings to the given path.
					File file = chooser.showSaveDialog(windowManager.getMainWindow());
					if (file != null) {
						exportPool.submit(() -> {
							try {
								AggregatedMappings mappings = aggregateMappingManager.getAggregatedMappings();
								MappingFileFormat format = formatManager.createFormatInstance(formatName);
								if (format != null) {
									String mappingsText = format.exportText(mappings);
									Files.writeString(file.toPath(), mappingsText);
									logger.info("Exporting mappings to {} in {} format", file.getName(), formatName);
								} else {
									throw new IllegalStateException("Format was unregistered: " + formatName);
								}
							} catch (Exception ex) {
								logger.error("Failed to write mappings in {} format to {}",
										formatName, file.getName(), ex);
							}
						});
					}
				}));
			} else {
				MenuItem item = new MenuItem();
				item.textProperty().bind(Lang.formatLiterals("menu.mappings.export.unsupported", formatName));
				item.setGraphic(new FontIconView(CarbonIcons.CLOSE));
				item.setDisable(true);
				export.getItems().add(item);
			}
		}

		getItems().add(apply);
		getItems().add(export);

		// getItems().add(action("menu.mappings.generate", CarbonIcons.LICENSE_MAINTENANCE, this::openGenerate));
		getItems().add(action("menu.mappings.view", CarbonIcons.VIEW, this::openView));

		// Disable if attached via agent, or there is no workspace
		disableProperty().bind(hasAgentWorkspace.or(hasWorkspace.not()));
	}

	private void openGenerate() {
		// TODO: Configurable UI, preview (% already mapped, % will be mapped(already/unmapped), % unmapped)
	}

	private void openView() {
		Stage window = windowManager.getMappingPreviewWindow();
		window.show();
		window.requestFocus();
	}
}
