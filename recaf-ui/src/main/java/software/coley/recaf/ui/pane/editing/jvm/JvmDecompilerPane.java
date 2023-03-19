package software.coley.recaf.ui.pane.editing.jvm;

import atlantafx.base.theme.Styles;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.animation.Transition;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import software.coley.observables.ObservableBoolean;
import software.coley.observables.ObservableInteger;
import software.coley.observables.ObservableObject;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.info.builder.JvmClassInfoBuilder;
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
import software.coley.recaf.services.navigation.Navigable;
import software.coley.recaf.services.navigation.UpdatableNavigable;
import software.coley.recaf.ui.config.KeybindingConfig;
import software.coley.recaf.ui.control.BoundLabel;
import software.coley.recaf.ui.control.richtext.Editor;
import software.coley.recaf.ui.control.richtext.bracket.BracketMatchGraphicFactory;
import software.coley.recaf.ui.control.richtext.bracket.SelectedBracketTracking;
import software.coley.recaf.ui.control.richtext.problem.Problem;
import software.coley.recaf.ui.control.richtext.problem.ProblemGraphicFactory;
import software.coley.recaf.ui.control.richtext.problem.ProblemPhase;
import software.coley.recaf.ui.control.richtext.problem.ProblemTracking;
import software.coley.recaf.ui.control.richtext.search.SearchBar;
import software.coley.recaf.ui.control.richtext.syntax.RegexLanguages;
import software.coley.recaf.ui.control.richtext.syntax.RegexSyntaxHighlighter;
import software.coley.recaf.util.*;
import software.coley.recaf.util.threading.ThreadPoolFactory;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.Bundle;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;

import java.util.Collection;
import java.util.Collections;
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
public class JvmDecompilerPane extends BorderPane implements UpdatableNavigable {
	private static final Logger logger = Logging.get(JvmDecompilerPane.class);
	private static final ExecutorService compilePool = ThreadPoolFactory.newSingleThreadExecutor("recompile");
	private final ObservableObject<JvmDecompiler> decompiler = new ObservableObject<>(NoopJvmDecompiler.getInstance());
	private final ObservableInteger javacTarget = new ObservableInteger(-1); // use negative to match class file's ver
	private final ObservableBoolean javacDebug = new ObservableBoolean(true);
	private final ObservableBoolean decompileInProgress = new ObservableBoolean(false);
	private final AtomicBoolean updateLock = new AtomicBoolean();
	private final ProblemTracking problemTracking = new ProblemTracking();
	private final JvmDecompilerPaneConfig config;
	private final DecompilerManager decompilerManager;
	private final JavacCompiler javac;
	private final Editor editor;
	private ClassPathNode path;

	@Inject
	public JvmDecompilerPane(@Nonnull JvmDecompilerPaneConfig config,
							 @Nonnull KeybindingConfig keys,
							 @Nonnull SearchBar searchBar,
							 @Nonnull DecompilerManager decompilerManager,
							 @Nonnull JavacCompiler javac) {
		this.config = config;
		this.decompilerManager = decompilerManager;
		this.javac = javac;
		decompiler.setValue(decompilerManager.getTargetJvmDecompiler());
		editor = new Editor();
		editor.getStylesheets().add("/syntax/java.css");
		editor.setSyntaxHighlighter(new RegexSyntaxHighlighter(RegexLanguages.getJavaLanguage()));
		editor.setSelectedBracketTracking(new SelectedBracketTracking());
		editor.setProblemTracking(problemTracking);
		editor.getRootLineGraphicFactory().addLineGraphicFactories(
				new BracketMatchGraphicFactory(),
				new ProblemGraphicFactory()
		);

		// Install additional editor components
		JvmDecompilerPaneConfigurator configurator = new JvmDecompilerPaneConfigurator(config, decompiler,
				javacTarget, javacDebug, decompilerManager);
		configurator.install(editor);
		searchBar.install(editor);

		// Add overlay for when decompilation is in-progress
		DecompileProgressOverlay overlay = new DecompileProgressOverlay();
		decompileInProgress.addChangeListener((ob, old, cur) -> {
			ObservableList<Node> children = editor.getPrimaryStack().getChildren();
			if (cur) children.add(overlay);
			else children.remove(overlay);
		});

		// TODO: Hook up AST analysis for contextual right-click actions

		// Setup keybindings
		setOnKeyPressed(e -> {
			if (keys.getSave().match(e))
				save();
		});

		// Layout
		setCenter(editor);
	}

	@Nonnull
	@Override
	public PathNode<?> getPath() {
		return path;
	}

	@Nonnull
	@Override
	public Collection<Navigable> getNavigableChildren() {
		return Collections.emptyList();
	}

	@Override
	public void onUpdatePath(@Nonnull PathNode<?> path) {
		if (!updateLock.get() && path instanceof ClassPathNode classPathNode) {
			this.path = classPathNode;

			// Schedule decompilation task, update the editor's text asynchronously on the JavaFX UI thread when complete.
			Workspace workspace = classPathNode.getValueOfType(Workspace.class);
			JvmClassInfo classInfo = classPathNode.getValue().asJvmClass();
			decompileInProgress.setValue(true);
			editor.setDisable(true);
			decompilerManager.decompile(decompiler.getValue(), workspace, classInfo)
					.completeOnTimeout(timeoutResult(), config.getTimeoutSeconds().getValue(), TimeUnit.SECONDS)
					.whenCompleteAsync((result, throwable) -> {
						editor.setDisable(false);
						decompileInProgress.setValue(false);

						// Handle uncaught exceptions
						if (throwable != null) {
							String trace = StringUtil.traceToString(throwable);
							editor.setText("/*\nUncaught exception when decompiling:\n" + trace + "\n*/");
							return;
						}

						// Handle decompilation result
						String text = result.getText();
						switch (result.getType()) {
							case SUCCESS -> editor.setText(text);
							case SKIPPED -> editor.setText(text == null ? "// Decompilation skipped" : text);
							case FAILURE -> {
								Throwable exception = result.getException();
								if (exception != null) {
									String trace = StringUtil.traceToString(exception);
									editor.setText("/*\nDecompile failed:\n" + trace + "\n*/");
								} else
									editor.setText("/*\nDecompile failed, but no trace was attached:\n*/");
							}
						}

						// Prevent undo from reverting to empty state.
						editor.getCodeArea().getUndoManager().forgetHistory();
					}, FxThreadUtil.executor());
		}
	}

	@Override
	public void disable() {
		setDisable(true);
		setOnKeyPressed(null);
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
						newInfo = info.toBuilder()
								.adaptFrom(new ClassReader(bytecode))
								.build();
					} else {
						// Handle inner classes.
						JvmClassInfo originalClass = bundle.get(name);
						if (originalClass != null) {
							// Adapt from existing.
							newInfo = originalClass
									.toBuilder()
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
	private int useConfiguredVersion(JvmClassInfo info) {
		int version = javacTarget.getValue();

		// Negative: Match class file's version
		if (version < 0)
			return JavaVersion.adaptFromClassFileVersion(info.getVersion());

		// Use provided version
		return version;
	}

	/**
	 * @return Result made for timed out decompilations.
	 */
	private DecompileResult timeoutResult() {
		JvmClassInfo info = path.getValue().asJvmClass();
		JvmDecompiler jvmDecompiler = decompiler.getValue();
		return new DecompileResult("""
				// Decompilation timed out.
				//  - Class name: %s
				//  - Class size: %d bytes
				//  - Decompiler: %s - %s
				//  - Timeout: %d seconds
				//
				// Suggestions:
				//  - Increase timeout
				//  - Change decompilers
				//  - Deobfuscate heavily obfuscated code and try again
				//
				// Reminder:
				//  - Class information is still available on the side panels ==>
				""".formatted(info.getName(),
				info.getBytecode().length,
				jvmDecompiler.getName(), jvmDecompiler.getVersion(),
				config.getTimeoutSeconds().getValue()
		), null, DecompileResult.ResultType.SKIPPED, 0);
	}

	/**
	 * And overlay shown while a class is being decompiled.
	 */
	private class DecompileProgressOverlay extends BorderPane {
		private DecompileProgressOverlay() {
			Label title = new BoundLabel(Lang.getBinding("java.decompiling"));
			title.getStyleClass().add(Styles.TITLE_3);
			Label text = new Label();
			text.getStyleClass().add(Styles.TEXT_SUBTLE);
			text.setFont(new Font("JetBrains Mono", 12)); // Pulling from CSS applied to the editor.
			setCenter(new Group(new VBox(title, text)));

			// Setup transition to play whenever decompilation is in progress.
			BytecodeTransition transition = new BytecodeTransition(text);
			decompileInProgress.addChangeListener((ob, old, cur) -> {
				setVisible(cur);
				if (cur) {
					transition.update(path.getValue().asJvmClass());
					transition.play();
				} else
					transition.stop();
			});
		}

		private class BytecodeTransition extends Transition {
			private final Labeled labeled;
			private byte[] bytecode;

			/**
			 * @param labeled
			 * 		Target label.
			 */
			public BytecodeTransition(@Nonnull Labeled labeled) {
				this.labeled = labeled;
			}

			/**
			 * @param info
			 * 		Class to show bytecode of.
			 */
			public void update(@Nonnull JvmClassInfo info) {
				this.bytecode = info.getBytecode();
				setCycleDuration(Duration.millis(bytecode.length));
			}

			@Override
			protected void interpolate(double fraction) {
				int bytecodeSize = bytecode.length;
				int textLength = 18;
				int middle = (int) (fraction * bytecodeSize);
				int start = middle - (textLength / 2);
				int end = middle + (textLength / 2);

				// We have two rows, top for hex, bottom for text.
				StringBuilder sbHex = new StringBuilder();
				StringBuilder sbText = new StringBuilder();
				for (int i = start; i < end; i++) {
					if (i < 0) {
						sbHex.append("   ");
						sbText.append("   ");
					} else if (i >= bytecodeSize) {
						sbHex.append(" ..");
						sbText.append(" ..");
					} else {
						byte b = bytecode[i];
						char c = (char) b;
						if (Character.isWhitespace(c)) c = ' ';
						else if (c < 32 || c > 255) c = '?';
						String hex = StringUtil.limit(Integer.toHexString(b), 2);
						if (hex.length() == 1) hex = "0" + hex;
						sbHex.append(StringUtil.fillLeft(3, " ", hex));
						sbText.append(StringUtil.fillLeft(3, " ", String.valueOf(c)));
					}
				}
				labeled.setText(sbHex + "\n" + sbText);
			}
		}
	}
}
