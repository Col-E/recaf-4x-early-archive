package software.coley.recaf.ui.pane.editing.jvm;

import atlantafx.base.controls.Spacer;
import atlantafx.base.theme.Styles;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.animation.Transition;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.util.Duration;
import org.fxmisc.richtext.CodeArea;
import org.kordamp.ikonli.carbonicons.CarbonIcons;
import org.objectweb.asm.ClassReader;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.tree.J;
import org.slf4j.Logger;
import software.coley.observables.ObservableBoolean;
import software.coley.observables.ObservableInteger;
import software.coley.observables.ObservableObject;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.info.builder.JvmClassInfoBuilder;
import software.coley.recaf.info.member.ClassMember;
import software.coley.recaf.info.properties.builtin.RemapOriginTaskProperty;
import software.coley.recaf.path.ClassPathNode;
import software.coley.recaf.path.PathNode;
import software.coley.recaf.services.compile.CompileMap;
import software.coley.recaf.services.compile.CompilerDiagnostic;
import software.coley.recaf.services.compile.JavacArgumentsBuilder;
import software.coley.recaf.services.compile.JavacCompiler;
import software.coley.recaf.services.decompile.DecompileResult;
import software.coley.recaf.services.decompile.DecompilerManager;
import software.coley.recaf.services.decompile.JvmDecompiler;
import software.coley.recaf.services.decompile.NoopJvmDecompiler;
import software.coley.recaf.services.mapping.MappingResults;
import software.coley.recaf.services.mapping.Mappings;
import software.coley.recaf.services.navigation.Actions;
import software.coley.recaf.services.navigation.ClassNavigable;
import software.coley.recaf.services.navigation.Navigable;
import software.coley.recaf.services.navigation.UpdatableNavigable;
import software.coley.recaf.services.source.AstMappingVisitor;
import software.coley.recaf.services.source.AstResolveResult;
import software.coley.recaf.ui.config.KeybindingConfig;
import software.coley.recaf.ui.control.BoundLabel;
import software.coley.recaf.ui.control.FontIconView;
import software.coley.recaf.ui.control.richtext.Editor;
import software.coley.recaf.ui.control.richtext.bracket.BracketMatchGraphicFactory;
import software.coley.recaf.ui.control.richtext.bracket.SelectedBracketTracking;
import software.coley.recaf.ui.control.richtext.problem.Problem;
import software.coley.recaf.ui.control.richtext.problem.ProblemGraphicFactory;
import software.coley.recaf.ui.control.richtext.problem.ProblemPhase;
import software.coley.recaf.ui.control.richtext.problem.ProblemTracking;
import software.coley.recaf.ui.control.richtext.search.SearchBar;
import software.coley.recaf.ui.control.richtext.source.JavaContextActionSupport;
import software.coley.recaf.ui.control.richtext.syntax.RegexLanguages;
import software.coley.recaf.ui.control.richtext.syntax.RegexSyntaxHighlighter;
import software.coley.recaf.ui.pane.editing.AbstractDecompilePane;
import software.coley.recaf.util.*;
import software.coley.recaf.util.threading.ThreadPoolFactory;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.Bundle;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Displays a {@link JvmClassInfo} via a configured {@link Editor} as decompiled by {@link DecompilerManager}.
 *
 * @author Matt Coley
 */
@Dependent
public class JvmDecompilerPane extends AbstractDecompilePane {
	private static final Logger logger = Logging.get(JvmDecompilerPane.class);
	private static final ExecutorService compilePool = ThreadPoolFactory.newSingleThreadExecutor("recompile");
	private final ObservableInteger javacTarget = new ObservableInteger(-1); // use negative to match class file's ver
	private final ObservableBoolean javacDebug = new ObservableBoolean(true);
	private final JavacCompiler javac;

	@Inject
	public JvmDecompilerPane(@Nonnull DecompilerPaneConfig config,
							 @Nonnull KeybindingConfig keys,
							 @Nonnull SearchBar searchBar,
							 @Nonnull JavaContextActionSupport contextActionSupport,
							 @Nonnull DecompilerManager decompilerManager,
							 @Nonnull JavacCompiler javac,
							 @Nonnull Actions actions) {
		super(config, keys, searchBar, contextActionSupport, decompilerManager, actions);
		this.javac = javac;

		// Install configurator popup
		JvmDecompilerPaneConfigurator configurator = new JvmDecompilerPaneConfigurator(config, decompiler,
				javacTarget, javacDebug, decompilerManager);
		configurator.install(editor);

		// Setup keybindings
		setOnKeyPressed(e -> {
			if (keys.getSave().match(e))
				save();
			if (keys.getRename().match(e)) {
				// Resolve what the caret position has, then handle renaming on the generic result.
				AstResolveResult result = contextActionSupport.resolvePosition(editor.getCodeArea().getCaretPosition());
				if (result != null)
					actions.rename(result.path());
			}
		});
	}

	/**
	 * Called when {@link KeybindingConfig#getSave()} is pressed.
	 * <br>
	 * Compiles the current Java code in the {@link #editor} and updates the workspace
	 * with the newly compiled {@link JvmClassInfo}.
	 */
	private void save() {
		// Pull data from path.
		JvmClassInfo info = path.getValue().asJvmClass();
		Workspace workspace = path.getValueOfType(Workspace.class);
		JvmClassBundle bundle = (JvmClassBundle) path.getValueOfType(Bundle.class);
		if (bundle == null)
			throw new IllegalStateException("Bundle missing from class path node");

		// Clear old errors emitted by compilation.
		problemTracking.removeByPhase(ProblemPhase.BUILD);

		// Invoke compiler with data.
		String infoName = info.getName();
		CompletableFuture.supplyAsync(() -> {
			JavacArgumentsBuilder builder = new JavacArgumentsBuilder()
					.withVersionTarget(useConfiguredVersion(info))
					.withDebugVariables(javacDebug.getValue())
					.withDebugSourceName(javacDebug.getValue())
					.withDebugLineNumbers(javacDebug.getValue())
					.withClassSource(editor.getText())
					.withClassName(infoName);
			return javac.compile(builder.build(), workspace, null);
		}, compilePool).whenCompleteAsync((result, throwable) -> {
			// Handle results.
			//  - Success --> Update content in the containing bundle
			//  - Failure --> Show error + diagnostics to user
			if (result != null && result.wasSuccess()) {
				// Renaming is not allowed. Tell the user to use mapping operations.
				// This should usually be caught by javac, but we're double-checking here.
				// We *could* have some hacky code to work around the rename being done outside the dedicated API,
				// but it would be ugly. Find the new name for the class and any inners, copy over properties from
				// the old names, apply mapping operations to patch broken references, etc.
				CompileMap compilations = result.getCompilations();
				boolean wasClassRenamed = !compilations.containsKey(infoName) || info.getInnerClasses().stream()
						.anyMatch(inner -> !compilations.containsKey(inner.getInnerClassName()));
				if (wasClassRenamed) {
					logger.warn("Please only rename classes via mapping operations.");
					Animations.animateWarn(this, 1000);
					return;
				}

				// Compilation map has contents, update the workspace.
				Animations.animateSuccess(this, 1000);
				updateLock.set(true);
				compilations.forEach((name, bytecode) -> {
					JvmClassInfo newInfo;
					if (infoName.equals(name)) {
						// Adapt from existing.
						newInfo = info.toJvmClassBuilder()
								.adaptFrom(new ClassReader(bytecode))
								.build();
					} else {
						// Handle inner classes.
						JvmClassInfo originalClass = bundle.get(name);
						if (originalClass != null) {
							// Adapt from existing.
							newInfo = originalClass
									.toJvmClassBuilder()
									.adaptFrom(new ClassReader(bytecode))
									.build();
							bundle.put(newInfo);
						} else {
							// Class is new.
							newInfo = new JvmClassInfoBuilder(new ClassReader(bytecode)).build();
						}
					}

					// Update the class in the bundle.
					bundle.put(newInfo);
				});
				updateLock.set(false);
			} else {
				// Handle compile-result failure, or uncaught thrown exception.
				if (result != null) {
					for (CompilerDiagnostic diagnostic : result.getDiagnostics())
						problemTracking.add(Problem.fromDiagnostic(diagnostic));

					// For first-timers, tell them you cannot save with errors.
					if (!config.getAcknowledgedSaveWithErrors().getValue())
						showFirstTimeSaveWithErrors();
				} else {
					logger.error("Compilation encountered an error on class '{}'", infoName, throwable);
				}
				Animations.animateFailure(this, 1000);
			}

			// Redraw paragraph graphics to update things like in-line problem graphics.
			editor.redrawParagraphGraphics();
		}, FxThreadUtil.executor());
	}

	/**
	 * @param info
	 * 		Class to recompile.
	 *
	 * @return Target Java version <i>(Standard versioning, not the internal one)</i>.
	 */
	private int useConfiguredVersion(@Nonnull JvmClassInfo info) {
		int version = javacTarget.getValue();

		// Negative: Match class file's version
		if (version < 0)
			return JavaVersion.adaptFromClassFileVersion(info.getVersion());

		// Use provided version
		return version;
	}

	/**
	 * Show popup telling user they cannot save with errors.
	 */
	private void showFirstTimeSaveWithErrors() {
		Button acknowledge = new Button();
		acknowledge.setGraphic(new FontIconView(CarbonIcons.TIMER));
		acknowledge.setDisable(true); // Enabled after a delay.

		VBox content = new VBox(new BoundLabel(Lang.getBinding("java.savewitherrors")), acknowledge);
		content.setSpacing(10);
		content.setAlignment(Pos.CENTER);

		TitledPane wrapper = new TitledPane();
		wrapper.textProperty().bind(Lang.getBinding("java.savewitherrors.title"));
		wrapper.setCollapsible(false);
		wrapper.setContent(content);
		wrapper.getStyleClass().add(Styles.ELEVATED_4);

		Popup popup = new Popup();
		popup.getContent().add(wrapper);
		popup.show(getScene().getWindow());
		popup.setAutoHide(false);
		popup.setHideOnEscape(false);

		// Start transition which counts down how long until the popup can be closed.
		WaitToAcknowledgeTransition wait = new WaitToAcknowledgeTransition(acknowledge);
		wait.play();

		// Enable acknowledge button after 5 seconds.
		FxThreadUtil.delayedRun(wait.getMillis(), () -> {
			wait.stop();
			acknowledge.textProperty().bind(Lang.getBinding("misc.acknowledge"));
			acknowledge.setGraphic(new FontIconView(CarbonIcons.CHECKMARK, Color.LIME));
			acknowledge.setDisable(false);
		});

		// When pressed, mark flag so prompt is not shown again.
		acknowledge.setOnAction(e -> {
			config.getAcknowledgedSaveWithErrors().setValue(true);
			popup.hide();
		});
	}

	/**
	 * Transition to handle countdown to allow acknowledging <i>"I can not save with errors"</i>.
	 */
	private static class WaitToAcknowledgeTransition extends Transition {
		private static final int SECONDS = 5;
		private final Labeled labeled;

		private WaitToAcknowledgeTransition(Labeled labeled) {
			this.labeled = labeled;
			setCycleDuration(Duration.seconds(SECONDS));
		}

		private long getMillis() {
			return SECONDS * 1000;
		}

		@Override
		protected void interpolate(double frac) {
			int secondsLeft = (int) Math.floor(SECONDS - (frac * SECONDS) + 1.01);
			labeled.setText(secondsLeft + "...");
		}
	}
}
