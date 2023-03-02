package software.coley.recaf.ui.pane.editing;

import jakarta.annotation.Nonnull;
import javafx.scene.layout.BorderPane;
import software.coley.recaf.info.AndroidClassInfo;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.path.ClassPathNode;
import software.coley.recaf.path.PathNode;
import software.coley.recaf.ui.navigation.Navigable;
import software.coley.recaf.ui.navigation.UpdatableNavigable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Common outline for displaying {@link ClassInfo} content.
 *
 * @author Matt Coley
 * @see JvmClassPane For {@link JvmClassInfo}.
 * @see AndroidClassPane For {@link AndroidClassInfo}.
 */
public abstract class ClassPane extends BorderPane implements UpdatableNavigable {
	private final List<Consumer<ClassPathNode>> pathUpdateListeners = new ArrayList<>();
	protected final List<Navigable> children = new ArrayList<>();
	private ClassPathNode path;

	/**
	 * Clear the display.
	 */
	protected void clearDisplay() {
		// Remove navigable child.
		if (getCenter() instanceof Navigable navigable)
			children.remove(navigable);

		// Remove display node.
		setCenter(null);
	}

	/**
	 * Generate display for the class denoted by {@link #getPath() the class path node}.
	 */
	protected abstract void generateDisplay();

	/**
	 * @param listener Listener to add.
	 */
	public void addPathUpdateListener(Consumer<ClassPathNode> listener) {
		  pathUpdateListeners.add(listener);
	}

	@Override
	public void onUpdatePath(@Nonnull PathNode<?> path) {
		// Update if class has changed.
		if (path instanceof ClassPathNode classPath) {
			this.path = classPath;
			pathUpdateListeners.forEach(listener -> listener.accept(classPath));

			// Initialize UI if it has not been done yet.
			if (getCenter() == null)
				generateDisplay();

			// Notify children of change.
			getNavigableChildren().forEach(child -> {
				if (child instanceof UpdatableNavigable updatable)
					updatable.onUpdatePath(path);
			});
		}
	}

	@Nonnull
	@Override
	public ClassPathNode getPath() {
		return path;
	}

	@Nonnull
	@Override
	public Collection<Navigable> getNavigableChildren() {
		return children;
	}

	@Override
	public void disable() {
		pathUpdateListeners.clear();
		setDisable(true);
	}
}
