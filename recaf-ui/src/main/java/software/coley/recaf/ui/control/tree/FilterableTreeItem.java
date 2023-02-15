package software.coley.recaf.ui.control.tree;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.util.ReflectUtil;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Filterable tree item.
 *
 * @param <T>
 * 		Type of value held by the item.
 *
 * @author <a href="https://stackoverflow.com/a/34426897">kaznovac</a>
 * @author Matt Coley - Optimization of child management.
 */
public class FilterableTreeItem<T> extends TreeItem<T> {
	private static final Field CHILDREN_LISTENER_FIELD;
	private static final Field CHILDREN_FIELD;
	private static final Logger logger = Logging.get(FilterableTreeItem.class);
	private final ObservableList<TreeItem<T>> sourceChildren = FXCollections.observableArrayList();
	private final ObjectProperty<Predicate<TreeItem<T>>> predicate = new SimpleObjectProperty<>();

	protected FilterableTreeItem() {
		// Support filtering by using a filtered list backing.
		// - Apply predicate to items
		FilteredList<TreeItem<T>> filteredChildren = new FilteredList<>(sourceChildren);
		filteredChildren.predicateProperty().bind(Bindings.createObjectBinding(() -> child -> {
			if (child instanceof FilterableTreeItem<T> filterable) {
				filterable.predicateProperty().set(predicate.get());
				if (filterable.forceVisible())
					return true;
			}
			if (predicate.get() == null || !child.getChildren().isEmpty())
				return true;
			boolean matched = predicate.get().test(child);
			onMatchResult(child, matched);
			return matched;
		}, predicate));

		// Reflection hackery
		setUnderlyingChildren(filteredChildren);
	}

	/**
	 * @return {@code true} when the item MUST be shown.
	 */
	public boolean forceVisible() {
		return false;
	}

	/**
	 * Allow additional action on items when the {@link #predicate} is updated.
	 *
	 * @param child
	 * 		Child item.
	 * @param matched
	 * 		Match status.
	 */
	protected void onMatchResult(TreeItem<T> child, boolean matched) {
		// Expand items that match, hide those that do not.
		if (matched) {
			TreeItems.expandParents(this);
		} else {
			child.setExpanded(false);
		}
	}

	/**
	 * Use reflection to directly set the underlying {@link TreeItem#children} field.
	 * The javadoc for that field literally says:
	 * <pre>
	 *  It is important that interactions with this list go directly into the
	 *  children property, rather than via getChildren(), as this may be
	 *  a very expensive call.
	 * </pre>
	 * This will additionally add the current tree-item's {@link TreeItem#childrenListener} to
	 * the given list's listeners.
	 *
	 * @param list
	 * 		Children list.
	 */
	@SuppressWarnings("unchecked")
	private void setUnderlyingChildren(ObservableList<TreeItem<T>> list) {
		Field childrenListenerField = CHILDREN_LISTENER_FIELD;
		if (childrenListenerField == null) {
			// See static block below.
			return;
		}
		try {
			// Add our change listener to the passed list.
			list.addListener((ListChangeListener<? super TreeItem<T>>) childrenListenerField.get(this));

			// Directly set "TreeItem.children"
			CHILDREN_FIELD.set(this, list);
		} catch (ReflectiveOperationException ex) {
			logger.error("Failed to update filterable children", ex);
		}
	}

	/**
	 * Add an unfiltered unsorted child to this item.
	 *
	 * @param item
	 * 		Child item to add.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void addAndSortChild(TreeItem<T> item) {
		synchronized (sourceChildren) {
			int index = Collections.binarySearch((List) getChildren(), item);
			if (index < 0)
				index = -(index + 1);
			sourceChildren.add(index, item);
		}
	}

	/**
	 * Remove an unfiltered child from this item.
	 *
	 * @param child
	 * 		Child item to remove.
	 *
	 * @return {@code true} when child removed.
	 * {@code false} when there was no item removed.
	 */
	public boolean removeSourceChild(TreeItem<T> child) {
		synchronized (sourceChildren) {
			return sourceChildren.remove(child);
		}
	}

	/**
	 * @return Predicate property.
	 */
	public ObjectProperty<Predicate<TreeItem<T>>> predicateProperty() {
		return predicate;
	}

	static {
		Field childrenListenerField;
		Field childrenField;
		try {
			childrenListenerField = ReflectUtil.getDeclaredField(TreeItem.class, "childrenListener");
			childrenField = ReflectUtil.getDeclaredField(TreeItem.class, "children");
		} catch (NoSuchFieldException ex) {
			logger.error("Failed to get necessary fields to set children list for TreeItem", ex);
			logger.error("Did the implementation of JavaFX change?");
			childrenListenerField = null;
			childrenField = null;
		}
		CHILDREN_LISTENER_FIELD = childrenListenerField;
		CHILDREN_FIELD = childrenField;
	}
}