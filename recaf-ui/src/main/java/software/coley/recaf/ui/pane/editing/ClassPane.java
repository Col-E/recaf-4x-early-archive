package software.coley.recaf.ui.pane.editing;

import jakarta.annotation.Nonnull;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import software.coley.recaf.info.AndroidClassInfo;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.info.member.ClassMember;
import software.coley.recaf.path.ClassPathNode;
import software.coley.recaf.path.PathNode;
import software.coley.recaf.services.navigation.ClassNavigable;
import software.coley.recaf.services.navigation.Navigable;
import software.coley.recaf.services.navigation.UpdatableNavigable;
import software.coley.recaf.ui.pane.editing.android.AndroidClassPane;
import software.coley.recaf.ui.pane.editing.jvm.JvmClassPane;

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
public abstract class ClassPane extends BorderPane implements ClassNavigable, UpdatableNavigable {
	private final List<Consumer<ClassPathNode>> pathUpdateListeners = new ArrayList<>();
	protected final List<Navigable> children = new ArrayList<>();
	private SideTabs sideTabs;
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
	 * @param node
	 * 		Node to display.
	 */
	protected void setDisplay(Node node) {
		// Remove old navigable child.
		Node old = getCenter();
		if (old instanceof Navigable navigableOld)
			children.remove(navigableOld);

		// Add navigable child.
		if (node instanceof Navigable navigableNode)
			children.add(navigableNode);

		// Set display node.
		setCenter(node);
	}

	/**
	 * Generate display for the class denoted by {@link #getPath() the class path node}.
	 * Children implementing this should call {@link #setDisplay(Node)}.
	 */
	protected abstract void generateDisplay();

	/**
	 * @param tab
	 * 		Tab to add to the side panel.
	 */
	protected void addSideTab(Tab tab) {
		// Lazily create/add side-tabs to UI.
		if (sideTabs == null) {
			sideTabs = new SideTabs();
			children.add(sideTabs);
			setRight(sideTabs);
		}

		// Add the given tab.
		sideTabs.getTabs().add(tab);
	}

	/**
	 * @param listener
	 * 		Listener to add.
	 */
	public void addPathUpdateListener(Consumer<ClassPathNode> listener) {
		pathUpdateListeners.add(listener);
	}

	@Override
	public void requestFocus(@Nonnull ClassMember member) {
		// Delegate to child components
		for (Navigable navigableChild : getNavigableChildren())
			if (navigableChild instanceof ClassNavigable navigableClass)
				navigableClass.requestFocus(member);
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
