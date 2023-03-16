package software.coley.recaf.ui.pane.editing.jvm;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.info.builder.JvmClassInfoBuilder;
import software.coley.recaf.path.ClassPathNode;
import software.coley.recaf.path.PathNode;
import software.coley.recaf.services.compile.CompileMap;
import software.coley.recaf.services.compile.CompilerDiagnostic;
import software.coley.recaf.services.compile.JavacArgumentsBuilder;
import software.coley.recaf.services.compile.JavacCompiler;
import software.coley.recaf.services.decompile.DecompilerManager;
import software.coley.recaf.services.navigation.Navigable;
import software.coley.recaf.services.navigation.UpdatableNavigable;
import software.coley.recaf.ui.config.KeybindingConfig;
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
import software.coley.recaf.util.Animations;
import software.coley.recaf.util.FxThreadUtil;
import software.coley.recaf.util.JavaVersion;
import software.coley.recaf.util.StringUtil;
import software.coley.recaf.util.threading.ThreadPoolFactory;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.Bundle;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
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
	private final AtomicBoolean updateLock = new AtomicBoolean();
	private final ProblemTracking problemTracking = new ProblemTracking();
	private final DecompilerManager decompilerManager;
	private final Editor editor;
	private ClassPathNode path;

	@Inject
	public JvmDecompilerPane(@Nonnull KeybindingConfig keys,
							 @Nonnull SearchBar searchBar,
							 @Nonnull DecompilerManager decompilerManager,
							 @Nonnull JavacCompiler javac) {
		this.decompilerManager = decompilerManager;
		editor = new Editor();
		editor.getStylesheets().add("/syntax/java.css");
		editor.setSyntaxHighlighter(new RegexSyntaxHighlighter(RegexLanguages.getJavaLanguage()));
		editor.setSelectedBracketTracking(new SelectedBracketTracking());
		editor.setProblemTracking(problemTracking);
		editor.getRootLineGraphicFactory().addLineGraphicFactories(
				new BracketMatchGraphicFactory(),
				new ProblemGraphicFactory()
		);
		searchBar.install(editor);
		// TODO: Hook up AST analysis for contextual right-click actions
		setCenter(editor);

		setOnKeyPressed(e -> {
			if (keys.getSave().match(e)) {
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
					// TODO: allow user to manually change version target (should config be local? global?)
					JavacArgumentsBuilder builder = new JavacArgumentsBuilder()
							.withVersionTarget(JavaVersion.adaptFromClassFileVersion(info.getVersion()))
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
		});
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

			// TODO: Configurable timeout + option to have infinite timeout
			//  - 'java.decompiling' bind overlay when waiting
			decompilerManager.decompile(workspace, classInfo).whenCompleteAsync((result, throwable) -> {
				if (throwable != null) {
					editor.setText("/*\nUncaught exception when decompiling:\n" + StringUtil.traceToString(throwable) + "\n*/");
					return;
				}
				switch (result.getType()) {
					case SUCCESS -> editor.setText(result.getText());
					case SKIPPED -> editor.setText("// Decompilation skipped");
					case FAILURE -> {
						Throwable exception = result.getException();
						if (exception != null)
							editor.setText("/*\nDecompile failed:\n" + StringUtil.traceToString(exception) + "\n*/");
						else
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
}
