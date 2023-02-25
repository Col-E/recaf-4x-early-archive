package software.coley.recaf.ui.pane.editing;

import jakarta.annotation.Nonnull;
import javafx.scene.layout.BorderPane;
import software.coley.recaf.info.AndroidClassInfo;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.ui.navigation.Navigable;
import software.coley.recaf.ui.navigation.UpdatableNavigable;
import software.coley.recaf.ui.path.ClassPathNode;
import software.coley.recaf.ui.path.PathNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Common outline for displaying {@link ClassInfo} content.
 *
 * @author Matt Coley
 * @see JvmClassPane For {@link JvmClassInfo}.
 * @see AndroidClassPane For {@link AndroidClassInfo}.
 */
public abstract class ClassPane extends BorderPane implements UpdatableNavigable {
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

	@Override
	public void onUpdatePath(@Nonnull PathNode<?> path) {
		// Update if class has changed.
		if (path instanceof ClassPathNode classPath)
			this.path = classPath;

		// Notify children of change.
		getNavigableChildren().forEach(child -> {
			if (child instanceof UpdatableNavigable updatable)
				updatable.onUpdatePath(path);
		});

		// Update UI.
		generateDisplay();
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
}
