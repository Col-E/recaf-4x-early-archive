package software.coley.recaf.ui.control.richtext.linegraphics;

import jakarta.annotation.Nonnull;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import software.coley.recaf.ui.control.richtext.Editor;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Graphic factory for {@link Editor}.
 * <br>
 * Handles registration and display of additional {@link LineGraphicFactory} instances in a consistent manner.
 *
 * @author Matt Coley
 */
public class RootLineGraphicFactory extends AbstractLineGraphicFactory {
	private static final int LINE_V_PADDING = 1;
	private static final int LINE_H_PADDING = 5;
	private static final Insets PADDING = new Insets(LINE_V_PADDING, LINE_H_PADDING, LINE_V_PADDING, LINE_H_PADDING);
	private final SortedSet<LineGraphicFactory> factories = new TreeSet<>();
	private final Editor editor;

	/**
	 * @param editor
	 * 		Base editor to work off of.
	 */
	public RootLineGraphicFactory(@Nonnull Editor editor) {
		super(-1);
		this.editor = editor;
		addLineGraphicFactory(new LineNumberFactory());
	}

	/**
	 * @param factory
	 * 		Graphic factory to remove.
	 */
	public void addLineGraphicFactory(LineGraphicFactory factory) {
		factories.add(factory);
		factory.install(editor);
	}

	/**
	 * @param factory
	 * 		Graphic factory to remove.
	 *
	 * @return {@code true} when removed. {@code false} when did not exist.
	 */
	public boolean removeLineGraphicFactory(LineGraphicFactory factory) {
		if (factories.remove(factory)) {
			factory.uninstall(editor);
			return true;
		}
		return false;
	}

	@Override
	public Node apply(int line) {
		// Add all sub-factories in sorted order.
		HBox box = new HBox();
		box.setAlignment(Pos.CENTER_LEFT);
		box.setPadding(PADDING);
		ObservableList<Node> children = box.getChildren();
		for (LineGraphicFactory factory : factories) {
			Node child = factory.apply(line);
			if (child != null) children.add(child);
		}

		// Wrap so the padding of the HBox expands the space of the 'lineno'.
		BorderPane wrapper = new BorderPane(box);
		wrapper.getStyleClass().add("lineno");
		return wrapper;
	}

	@Override
	public void install(@Nonnull Editor editor) {
		// no-op
	}

	@Override
	public void uninstall(@Nonnull Editor editor) {
		throw new IllegalArgumentException("The root line graphic factory should never be uninstalled!");
	}
}
