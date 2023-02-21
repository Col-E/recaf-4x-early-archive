package software.coley.recaf.ui.pane;

import atlantafx.base.theme.Styles;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.beans.binding.StringBinding;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.carbonicons.CarbonIcons;
import org.slf4j.Logger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.services.script.ScriptEngine;
import software.coley.recaf.services.script.ScriptFile;
import software.coley.recaf.services.script.ScriptManager;
import software.coley.recaf.services.script.ScriptManagerConfig;
import software.coley.recaf.services.window.WindowFactory;
import software.coley.recaf.ui.control.ActionButton;
import software.coley.recaf.ui.window.RecafScene;
import software.coley.recaf.util.Animations;
import software.coley.recaf.util.DesktopUtil;
import software.coley.recaf.util.FxThreadUtil;

import java.io.IOException;
import java.util.TreeSet;

import static software.coley.recaf.util.Lang.getBinding;

/**
 * Pane to display available scripts.
 *
 * @author Matt Coley
 * @author yapht
 * @see ScriptManager Source of scripts to pull from.
 */
@Dependent
public class ScriptManagerPane extends BorderPane {
	private static final Logger logger = Logging.get(ScriptManagerPane.class);
	private final VBox scriptList = new VBox();
	private final ScriptManager scriptManager;
	private final ScriptManagerConfig config;
	private final ScriptEngine engine;
	private final WindowFactory windowFactory;

	@Inject
	public ScriptManagerPane(ScriptManagerConfig config, ScriptManager scriptManager, ScriptEngine engine,
							 WindowFactory windowFactory) {
		this.windowFactory = windowFactory;
		this.scriptManager = scriptManager;
		this.config = config;
		this.engine = engine;

		scriptManager.getScriptFiles().addChangeListener((ob, old, cur) -> refreshScripts());
		refreshScripts();

		scriptList.setFillWidth(true);
		scriptList.setSpacing(10);
		scriptList.setPadding(new Insets(10));

		ScrollPane scroll = new ScrollPane(scriptList);
		scroll.setFitToWidth(true);
		setCenter(scroll);

		HBox controls = new HBox();
		controls.setStyle("""
				-fx-background-color: -color-base-3;
				-fx-border-color: -color-border-default;
				-fx-border-width: 1 0 0 0;
				""");
		controls.setPadding(new Insets(10));
		controls.setSpacing(10);
		controls.getChildren().addAll(
				new ActionButton(CarbonIcons.EDIT, getBinding("menu.scripting.new"), this::newScript),
				new ActionButton(CarbonIcons.FOLDER, getBinding("menu.scripting.browse"), this::browse)
		);
		controls.getChildren().forEach(b -> b.getStyleClass().add(Styles.ACCENT));
		setBottom(controls);
	}

	/**
	 * Repopulate the script list.
	 */
	private void refreshScripts() {
		FxThreadUtil.run(() -> {
			scriptList.getChildren().clear();
			for (ScriptFile scriptFile : new TreeSet<>(scriptManager.getScriptFiles())) {
				scriptList.getChildren().add(new ScriptEntry(scriptFile));
			}
		});
	}

	/**
	 * @param script
	 * 		Script to edit.
	 */
	private void editScript(@Nonnull ScriptFile script) {
		// TODO: Create new window, with original text from the given script.
		//  - On save, write script text to the given script's path
		newScript();
	}

	/**
	 * Opens a new script editor.
	 */
	public void newScript() {
		// TODO: Editor save prompts file location to save to in scripts dir
		//  - Add toggle in manager button list to create 'advanced' script using class model
		TextArea textArea = new TextArea();
		textArea.setText("""
				// ==Metadata==
				// @name Name
				// @description Description
				// @version 1.0.0
				// @author Author
				// ==/Metadata==
				
				System.out.println("Hello world");
				""");
		Scene scene = new RecafScene(textArea, 750, 400);
		windowFactory.createAnonymousStage(scene, getBinding("menu.scripting.editor"), 750, 400).show();
	}

	/**
	 * Opens the local scripts directory.
	 */
	private void browse() {
		try {
			DesktopUtil.showDocument(config.getScriptsDirectory().toUri());
		} catch (IOException ex) {
			logger.error("Failed to show scripts directory", ex);
		}
	}

	private class ScriptEntry extends BorderPane {
		private ScriptEntry(ScriptFile script) {
			setPadding(new Insets(10));
			getStyleClass().add("tooltip");

			Label nameLabel = new Label(script.name());
			nameLabel.setMinSize(350, 20);
			nameLabel.getStyleClass().add(Styles.TITLE_3);

			VBox info = new VBox();
			info.getChildren().add(nameLabel);

			String description = script.description();
			String author = script.author();
			String version = script.version();
			String url = script.getTagValue("url");

			if (!description.isBlank())
				info.getChildren().add(makeAttribLabel(null, description));
			if (!author.isBlank())
				info.getChildren().add(makeAttribLabel(getBinding("menu.scripting.author"), author));
			if (!version.isBlank())
				info.getChildren().add(makeAttribLabel(getBinding("menu.scripting.version"), version));
			if (!url.isBlank()) {
				info.getChildren().add(makeAttribLabel(new StringBinding() {
					@Override
					protected String computeValue() {
						return "URL";
					}
				}, url));
			}

			VBox actions = new VBox();
			actions.setSpacing(4);
			actions.setAlignment(Pos.CENTER_RIGHT);


			ScriptEntry entry = this;
			Button executeButton = new ActionButton(CarbonIcons.PLAY_FILLED, getBinding("menu.scripting.execute"), () -> {
				script.execute(engine)
						.whenComplete((result, error) -> {
							if (result != null && result.wasSuccess()) {
								Animations.animateSuccess(entry, 1000);
							} else {
								Animations.animateFailure(entry, 1000);
							}
						});
			});
			executeButton.setAlignment(Pos.CENTER_LEFT);
			executeButton.setPrefSize(130, 30);

			Button editButton = new ActionButton(CarbonIcons.EDIT, getBinding("menu.scripting.edit"), () -> editScript(script));
			editButton.setAlignment(Pos.CENTER_LEFT);
			editButton.setPrefSize(130, 30);

			actions.getChildren().addAll(executeButton, editButton);

			Separator separator = new Separator(Orientation.HORIZONTAL);
			separator.prefWidthProperty().bind(scriptList.widthProperty());

			setLeft(info);
			setRight(actions);

			prefWidthProperty().bind(widthProperty());
		}

		/**
		 * Used to display bullet point format.
		 *
		 * @param langBinding
		 * 		Language binding for label display.
		 * @param secondaryText
		 * 		Text to appear after the initial binding text.
		 *
		 * @return Label bound to translatable text.
		 */
		private static Label makeAttribLabel(StringBinding langBinding, String secondaryText) {
			Label label = new Label(secondaryText);
			if (langBinding != null) {
				label.textProperty().bind(new StringBinding() {
					{
						bind(langBinding);
					}

					@Override
					protected String computeValue() {
						return String.format("  • %s: %s", langBinding.get(), secondaryText);
					}
				});
			}
			return label;
		}
	}
}
