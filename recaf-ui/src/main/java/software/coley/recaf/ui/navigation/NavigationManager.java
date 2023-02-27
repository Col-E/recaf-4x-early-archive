package software.coley.recaf.ui.navigation;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import software.coley.recaf.ui.docking.DockingManager;
import software.coley.recaf.ui.docking.DockingTab;
import software.coley.recaf.path.AbstractPathNode;
import software.coley.recaf.path.PathNode;
import software.coley.recaf.path.WorkspacePathNode;
import software.coley.recaf.workspace.WorkspaceManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Tracks available {@link Navigable} content currently open in the UI.
 * <br>
 * This is done by tracking the content of {@link DockingTab} instances when they are {@link Navigable}.
 * This component is itself {@link Navigable} which means if we use these tracked instances as our
 * {@link #getNavigableChildren()} we can do dynamic look-ups with {@link #getNavigableChildrenByPath(PathNode)}
 * to discover any currently open content.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class NavigationManager implements Navigable {
	private final List<Navigable> children = new ArrayList<>();
	private PathNode<?> path = new DummyInitialNode();

	@Inject
	public NavigationManager(@Nonnull DockingManager dockingManager,
							 @Nonnull WorkspaceManager workspaceManager) {
		// Track what navigable content is available.
		NavigableSpy spy = new NavigableSpy();
		dockingManager.addTabCreationListener((parent, tab) -> {
			ObjectProperty<Node> contentProperty = tab.contentProperty();

			// Add listener, so if content changes we are made aware of the changes.
			contentProperty.addListener(spy);

			// Record initial value.
			spy.changed(contentProperty, null, contentProperty.getValue());
		});
		dockingManager.addTabClosureListener(((parent, tab) -> {
			// Remove content from navigation tracking.
			spy.remove(tab.getContent());

			// Remove the listener from the tab.
			tab.contentProperty().removeListener(spy);
		}));

		// Track current workspace so that we are navigable ourselves.
		workspaceManager.addWorkspaceOpenListener(workspace -> path = new WorkspacePathNode(workspace));
	}

	@Nonnull
	@Override
	public PathNode<?> getPath() {
		return path;
	}

	@Nonnull
	@Override
	public Collection<Navigable> getNavigableChildren() {
		return children;
	}

	@Override
	public void requestFocus() {
		// no-op
	}

	/**
	 * Listener to update {@link #children}.
	 */
	private class NavigableSpy implements ChangeListener<Node> {
		@Override
		public void changed(ObservableValue<? extends Node> observable, Node oldValue, Node newValue) {
			remove(oldValue);
			add(newValue);
		}

		void add(Node value) {
			if (value instanceof Navigable navigable)
				children.add(navigable);
		}

		void remove(Node value) {
			if (value instanceof Navigable navigable)
				children.remove(navigable);
		}
	}

	/**
	 * Dummy node for initial state of {@link #path}.
	 */
	private static class DummyInitialNode extends AbstractPathNode<Object, Object> {
		private DummyInitialNode() {
			super("dummy", null, Object.class, new Object());
		}

		@Override
		public int localCompare(PathNode<?> o) {
			return -1;
		}
	}
}
