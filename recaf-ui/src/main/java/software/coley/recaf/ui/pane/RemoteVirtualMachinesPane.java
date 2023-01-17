package software.coley.recaf.ui.pane;

import atlantafx.base.theme.Styles;
import com.sun.tools.attach.VirtualMachineDescriptor;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.carbonicons.CarbonIcons;
import org.slf4j.Logger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.services.attach.AttachManager;
import software.coley.recaf.services.attach.AttachManagerConfig;
import software.coley.recaf.services.attach.JmxBeanServerConnection;
import software.coley.recaf.services.attach.PostScanListener;
import software.coley.recaf.ui.control.FontIconView;
import software.coley.recaf.ui.window.RemoteVirtualMachinesWindow;
import software.coley.recaf.util.FxThreadUtil;
import software.coley.recaf.util.Lang;
import software.coley.recaf.util.UncheckedSupplier;
import software.coley.recaf.util.threading.ThreadUtil;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanInfo;
import javax.management.ObjectName;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Pane for displaying available remote JVMs from {@link AttachManager}.
 *
 * @author Matt Coley
 * @see RemoteVirtualMachinesWindow
 */
@Dependent
public class RemoteVirtualMachinesPane extends BorderPane implements PostScanListener {
	private static final Logger logger = Logging.get(RemoteVirtualMachinesPane.class);
	private final Map<VirtualMachineDescriptor, VmCell> vmCellMap = new HashMap<>();
	private final VBox vmCellBox = new VBox();
	private final AttachManager attachManager;
	private final AttachManagerConfig attachManagerConfig;

	@Inject
	public RemoteVirtualMachinesPane(AttachManager attachManager, AttachManagerConfig attachManagerConfig) {
		this.attachManager = attachManager;
		this.attachManagerConfig = attachManagerConfig;

		// Register this class as scan listener so we can update the UI live as updates come in.
		attachManager.addPostScanListener(this);

		// Setup UI
		if (attachManager.canAttach())
			initialize();
		else
			initializeWithoutAttach();
	}

	/**
	 * Sets up the UI, and binds passive scanning to only occur while this pane is displayed.
	 */
	private void initialize() {
		// Add listener so that passive scanning in the attach manager only occurs while this pane is visible.
		sceneProperty().addListener((obScene, initialScene, scene) -> {
			scene.windowProperty().addListener((obWindow, initialWindow, window) -> {
				window.showingProperty().addListener((obShowing, oldShowing, showing) -> {
					// When showing run a scan immediately.
					// We are already registered as a scan listener, so we can update the display after it finishes.
					if (showing)
						ThreadUtil.run(attachManager::scan);

					// Bind scanning to only run when the UI is displayed.
					logger.debug("Passive JVM scanning: {}", showing ? "ENABLED" : "DISABLED");
					attachManagerConfig.getPassiveScanning().setValue(showing);
				});
			});
		});

		vmCellBox.setPadding(new Insets(20));
		vmCellBox.setAlignment(Pos.CENTER);
		vmCellBox.setSpacing(10);
		ScrollPane scroll = new ScrollPane(vmCellBox);
		scroll.setFitToWidth(true);
		setCenter(scroll);
	}

	/**
	 * Place a warning box stating that the feature is not available.
	 */
	private void initializeWithoutAttach() {
		Label graphic = new Label();
		graphic.setGraphic(new FontIconView(CarbonIcons.ERROR, 128, Color.RED));
		graphic.setAlignment(Pos.CENTER);

		Label title = new Label();
		title.getStyleClass().add(Styles.TITLE_1);
		title.textProperty().bind(Lang.getBinding("attach.unsupported"));
		title.setAlignment(Pos.CENTER);

		Label description = new Label();
		description.getStyleClass().add(Styles.TITLE_4);
		description.textProperty().bind(Lang.getBinding("attach.unsupported.detail"));
		description.setAlignment(Pos.CENTER);

		VBox box = new VBox(graphic, title, description);
		box.setMaxHeight(Double.MAX_VALUE);
		box.setMaxWidth(Double.MAX_VALUE);
		box.setMinHeight(250);
		box.setMinWidth(300);
		box.setAlignment(Pos.CENTER);
		box.getStyleClass().add("tooltip");

		// Layout 'box' centered on the pane
		VBox vwrap = new VBox(box);
		vwrap.setAlignment(Pos.CENTER);
		vwrap.setMaxHeight(Double.MAX_VALUE);
		vwrap.setMaxWidth(Double.MAX_VALUE);
		HBox hwrap = new HBox(vwrap);
		hwrap.setAlignment(Pos.CENTER);
		hwrap.setMaxHeight(Double.MAX_VALUE);
		hwrap.setMaxWidth(Double.MAX_VALUE);
		hwrap.setMouseTransparent(true);

		setCenter(hwrap);
	}

	@Override
	public void onScanCompleted(@Nonnull Set<VirtualMachineDescriptor> added,
								@Nonnull Set<VirtualMachineDescriptor> removed) {
		FxThreadUtil.run(() -> {
			// Add new VM's found
			for (VirtualMachineDescriptor descriptor : added) {
				VmCell cell = new VmCell(descriptor);
				vmCellMap.put(descriptor, cell);
				vmCellBox.getChildren().add(cell); // TODO: insert in sorted order
			}

			// Remove VM's that are no longer alive.
			for (VirtualMachineDescriptor descriptor : removed) {
				VmCell removedCell = vmCellMap.remove(descriptor);
				vmCellBox.getChildren().remove(removedCell);
			}

			// Refresh cells
			for (VmCell cell : vmCellMap.values()) {
				cell.update();
			}
		});
	}

	/**
	 * Cell for a remote JVM.
	 */
	private class VmCell extends VBox {
		private final ContentGrid contentGrid;

		/**
		 * @param descriptor
		 * 		Associated descriptor.
		 */
		VmCell(VirtualMachineDescriptor descriptor) {
			this.contentGrid = new ContentGrid(attachManager, descriptor);
			getStyleClass().add("tooltip");

			// Create title label
			int pid = attachManager.getVirtualMachinePid(descriptor);
			String mainClass = attachManager.getVirtualMachineMainClass(descriptor);
			boolean canConnect = attachManager.getVirtualMachineConnectionFailure(descriptor) == null;
			CarbonIcons titleIcon = canConnect ? CarbonIcons.DEBUG : CarbonIcons.ERROR_FILLED;
			FontIconView titleGraphic = new FontIconView(titleIcon, 28, canConnect ? Color.GREEN : Color.RED);
			Label title = new Label(pid + ": " + mainClass);
			title.getStyleClass().add(Styles.TEXT_CAPTION);
			title.setGraphic(titleGraphic);

			// Layout
			getChildren().addAll(title, new Separator(), contentGrid);
		}

		/**
		 * Updates the content display.
		 */
		void update() {
			contentGrid.update();
		}

		/**
		 * Wrapper of multiple content titles.
		 */
		private static class ContentGrid extends TabPane {
			private final List<AbstractContentTile> tiles = new ArrayList<>();

			public ContentGrid(AttachManager attachManager, VirtualMachineDescriptor descriptor) {
				// Layout & size
				setPrefHeight(200);

				// Property tile
				add(new AbstractContentTile() {
					private final TableView<String> propertyTable = new TableView<>();
					private Properties lastProperties;

					@Override
					void setup() {
						TableColumn<String, String> keyColumn = new TableColumn<>("Key");
						keyColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue()));

						TableColumn<String, String> valueColumn = new TableColumn<>("Value");
						valueColumn.setCellValueFactory(param -> new SimpleStringProperty(Objects.toString(lastProperties.get(param.getValue()))));

						ObservableList<TableColumn<String, ?>> columns = propertyTable.getColumns();
						columns.add(keyColumn);
						columns.add(valueColumn);

						setCenter(propertyTable);
					}

					@Override
					void update() {
						// Update property table if there are changes
						Properties properties = attachManager.getVirtualMachineProperties(descriptor);
						if (Objects.equals(lastProperties, properties)) return;
						lastProperties = properties;

						// Update table
						ObservableList<String> items = propertyTable.getItems();
						List<String> keys = properties.keySet().stream().map(Object::toString).sorted().toList();
						items.clear();
						items.addAll(keys);
					}

					@Override
					Tab tab() {
						Tab tab = new Tab();
						tab.setClosable(false);
						tab.setGraphic(new FontIconView(CarbonIcons.SETTINGS));
						tab.textProperty().bind(Lang.getBinding("attach.tab.properties"));
						return tab;
					}
				});

				// JMX tiles
				JmxBeanServerConnection jmxConnection = attachManager.getJmxServerConnection(descriptor);
				List<JmxWrapper> beanSuppliers = List.of(
						new JmxWrapper(CarbonIcons.OBJECT_STORAGE, "attach.tab.classloading", JmxBeanServerConnection.CLASS_LOADING, jmxConnection::getClassloadingBeanInfo),
						new JmxWrapper(CarbonIcons.QUERY_QUEUE, "attach.tab.compilation", JmxBeanServerConnection.COMPILATION, jmxConnection::getCompilationBeanInfo),
						new JmxWrapper(CarbonIcons.SCREEN, "attach.tab.system", JmxBeanServerConnection.OPERATING_SYSTEM, jmxConnection::getOperatingSystemBeanInfo),
						new JmxWrapper(CarbonIcons.METER, "attach.tab.runtime", JmxBeanServerConnection.RUNTIME, jmxConnection::getRuntimeBeanInfo),
						new JmxWrapper(CarbonIcons.PARENT_CHILD, "attach.tab.thread", JmxBeanServerConnection.THREAD, jmxConnection::getThreadBeanInfo)
				);
				for (JmxWrapper wrapper : beanSuppliers) {
					ObjectName name = wrapper.name();
					add(new AbstractContentTile() {
						private final TableView<String> propertyTable = new TableView<>();
						private Map<String, String> lastAttributeMap;

						@Override
						void setup() {
							TableColumn<String, String> keyColumn = new TableColumn<>("Key");
							keyColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue()));

							TableColumn<String, String> valueColumn = new TableColumn<>("Value");
							valueColumn.setCellValueFactory(param -> new SimpleStringProperty(Objects.toString(lastAttributeMap.get(param.getValue()))));

							ObservableList<TableColumn<String, ?>> columns = propertyTable.getColumns();
							columns.add(keyColumn);
							columns.add(valueColumn);

							setCenter(propertyTable);
						}

						@Override
						void update() {
							try {
								// Update attribute table if there are changes
								MBeanInfo beanInfo = wrapper.beanSupplier().get();
								MBeanAttributeInfo[] attributes = beanInfo.getAttributes();
								Map<String, String> attributeMap = Arrays.stream(attributes)
										.collect(Collectors.toMap(MBeanFeatureInfo::getDescription, a -> {
											try {
												return Objects.toString(jmxConnection.getConnection().getAttribute(name, a.getName()));
											} catch (Exception ex) {
												return "?";
											}
										}));
								if (Objects.equals(lastAttributeMap, attributeMap)) return;
								lastAttributeMap = attributeMap;

								// Update table
								ObservableList<String> items = propertyTable.getItems();
								List<String> keys = attributeMap.keySet().stream().map(Object::toString).sorted().toList();
								items.clear();
								items.addAll(keys);

								// Enable on success
								setDisable(false);
							} catch (Exception ex) {
								// Disable on failure
								setDisable(true);
							}
						}

						@Override
						Tab tab() {
							Tab tab = new Tab();
							tab.setClosable(false);
							tab.setGraphic(new FontIconView(wrapper.icon()));
							tab.textProperty().bind(Lang.getBinding(wrapper.langKey()));
							return tab;
						}
					});
				}
			}

			/**
			 * @param tile
			 * 		Tile to add to the content grid.
			 */
			private void add(AbstractContentTile tile) {
				tile.setup();
				tiles.add(tile);
				Tab tab = tile.tab();
				tab.setContent(tile);
				getTabs().add(tab);
			}

			/**
			 * Updates all blocks.
			 */
			public void update() {
				for (AbstractContentTile block : tiles)
					block.update();
			}
		}

		/**
		 * Simple content tile.
		 */
		private static abstract class AbstractContentTile extends BorderPane {
			abstract void setup();

			abstract void update();

			abstract Tab tab();
		}

		/**
		 * Wrapper to hold both the given values.
		 *
		 * @param icon
		 * 		Graphic icon representation.
		 * @param langKey
		 * 		Identifier for language name lookup.
		 * @param name
		 * 		JMX object name instance.
		 * @param beanSupplier
		 * 		Supplier to the current bean info.
		 */
		private record JmxWrapper(Ikon icon, String langKey, ObjectName name,
								  UncheckedSupplier<MBeanInfo> beanSupplier) {
		}
	}
}
