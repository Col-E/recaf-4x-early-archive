package software.coley.recaf.ui.pane;

import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.kordamp.ikonli.Ikon;
import software.coley.recaf.config.ConfigContainer;
import software.coley.recaf.config.ConfigValue;
import software.coley.recaf.services.config.ConfigManager;
import software.coley.recaf.services.config.ManagedConfigListener;
import software.coley.recaf.ui.config.ConfigComponentFactory;
import software.coley.recaf.ui.config.ConfigComponentManager;
import software.coley.recaf.ui.config.ConfigIconManager;
import software.coley.recaf.ui.control.BoundLabel;
import software.coley.recaf.ui.control.FontIconView;
import software.coley.recaf.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static software.coley.recaf.config.ConfigGroups.PACKAGE_SPLIT;
import static software.coley.recaf.config.ConfigGroups.getGroupPackages;
import static software.coley.recaf.util.Lang.getBinding;

/**
 * Pane to display all config values.
 *
 * @author Matt Coley
 * @see ConfigManager Source of values to pull from.
 * @see ConfigComponentManager Controls how to represent {@link ConfigValue} instances.
 * @see ConfigIconManager Controls which icons to show in the tree for {@link ConfigContainer} paths.
 */
@Dependent
public class ConfigPane extends SplitPane implements ManagedConfigListener {
	private static final String CONF_LANG_PREFIX = "conf.";
	private final Map<String, ConfigPage> pages = new HashMap<>();
	private final TreeItem<String> root = new TreeItem<>("root");
	private final TreeView<String> tree = new TreeView<>();
	private final ScrollPane content = new ScrollPane();
	private final ConfigComponentManager componentManager;
	private final ConfigIconManager iconManager;

	@Inject
	public ConfigPane(ConfigManager configManager, ConfigComponentManager componentManager,
					  ConfigIconManager iconManager) {
		this.componentManager = componentManager;
		this.iconManager = iconManager;
		configManager.addManagedConfigListener(this);

		// Setup UI
		initialize();

		// Initial state from existing containers
		for (ConfigContainer container : configManager.getContainers())
			onRegister(container);
	}

	private void initialize() {
		root.setExpanded(true);
		tree.setShowRoot(false);
		tree.setRoot(root);
		tree.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
		tree.setCellFactory(param -> new TreeCell<>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);

				if (empty || item == null) {
					textProperty().unbind();
					textProperty().set(null);
					setOnMousePressed(null);
					setGraphic(null);
				} else {
					Ikon icon = iconManager.getGroupIcon(item);
					if (icon == null)
						icon = iconManager.getContainerIcon(item);
					if (icon != null)
						setGraphic(new FontIconView(icon));
					else
						setGraphic(null);
					textProperty().bind(getBinding(CONF_LANG_PREFIX + item));
					setOnMousePressed(e -> {
						ConfigPage page = pages.get(item);
						if (page != null) content.setContent(page);
					});
				}
			}
		});

		// Layout
		SplitPane.setResizableWithParent(tree, false);
		getItems().addAll(tree, content);
		setDividerPositions(0.3);
	}

	@Override
	public void onRegister(@Nonnull ConfigContainer container) {
		// Skip empty containers
		if (container.getValues().isEmpty())
			return;

		// Setup tree structure.
		TreeItem<String> item = getItem(container, true);
		if (item != null) {
			String pageKey = item.getValue() + PACKAGE_SPLIT + container.getId();
			item.getChildren().add(new TreeItem<>(pageKey));

			// Register page.
			pages.put(pageKey, new ConfigPage(container));
		}
	}

	@Override
	public void onUnregister(@Nonnull ConfigContainer container) {
		TreeItem<String> item = getItem(container, false);
		while (item != null) {
			TreeItem<String> parent = item.getParent();
			List<TreeItem<String>> children = parent.getChildren();
			children.remove(item);

			// Determine if the parent also needs to be pruned when it is empty.
			if (children.isEmpty()) item = parent;
			else break;
		}
	}

	/**
	 * @param item
	 * 		Tree item to look in.
	 * @param name
	 * 		Name to match.
	 *
	 * @return Child with {@link TreeItem#getValue()} matching the given name value.
	 */
	@Nullable
	private TreeItem<String> getChildTreeItemByName(@Nonnull TreeItem<String> item, @Nonnull String name) {
		for (TreeItem<String> child : item.getChildren())
			if (name.equals(child.getValue()))
				return child;
		return null;
	}

	/**
	 * @param container
	 * 		Container to get item of.
	 * @param createIfMissing
	 * 		Flag to create item if it does not exist.
	 *
	 * @return Tree item if it exists. Otherwise {@code null}.
	 */
	@Nullable
	private TreeItem<String> getItem(ConfigContainer container, boolean createIfMissing) {
		TreeItem<String> currentItem = root;
		String currentPackage = null;
		String[] packages = getGroupPackages(container);
		for (String packageName : packages) {
			if (currentPackage == null)
				currentPackage = packageName;
			else
				currentPackage += PACKAGE_SPLIT + packageName;

			// Get or setup tree item.
			TreeItem<String> child = getChildTreeItemByName(currentItem, currentPackage);
			if (child == null) {
				if (createIfMissing) {
					// No existing tree item, create one.
					child = new TreeItem<>(currentPackage);
					child.setExpanded(true);

					// Insert child in sorted order.
					List<String> childrenNames = currentItem.getChildren().stream()
							.map(TreeItem::getValue)
							.toList();
					int insertIndex = CollectionUtils.sortedInsertIndex(childrenNames, currentPackage);
					currentItem.getChildren().add(insertIndex, child);
				} else {
					// Exit, cannot find item
					return null;
				}
			}
			currentItem = child;
		}
		return currentItem;
	}

	/**
	 * Page for a single {@link ConfigContainer}.
	 */
	private class ConfigPage extends GridPane {
		@SuppressWarnings({"rawtypes", "unchecked"})
		private ConfigPage(ConfigContainer container) {
			// Title
			Label title = new BoundLabel(getBinding(container.getGroupAndId()));
			title.getStyleClass().add(Styles.TITLE_4);
			add(title, 0, 0, 2, 1);
			add(new Separator(), 0, 1, 2, 1);

			// Values
			Map<String, ConfigValue<?>> values = container.getValues();
			int row = 2;
			for (Map.Entry<String, ConfigValue<?>> entry : values.entrySet()) {
				ConfigValue<?> value = entry.getValue();
				ConfigComponentFactory componentFactory = componentManager.getFactory(value);
				if (componentFactory.isStandAlone()) {
					add(componentFactory.create(container, value), 0, row, 2, 1);
				} else {
					String key = container.getScopedId(value);
					add(new BoundLabel(getBinding(key)), 0, row);
					add(componentFactory.create(container, value), 1, row);
				}
				row++;
			}

			// Layout
			setPadding(new Insets(10));
			setVgap(5);
			setHgap(5);
			ColumnConstraints columnLabel = new ColumnConstraints();
			ColumnConstraints columnEditor = new ColumnConstraints();
			columnEditor.setHgrow(Priority.ALWAYS);
			getColumnConstraints().addAll(columnLabel, columnEditor);
		}
	}
}
