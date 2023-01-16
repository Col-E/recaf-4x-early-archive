package software.coley.recaf.ui.window;

import javafx.stage.Stage;
import software.coley.recaf.util.Icons;

/**
 * Base implementation of {@link IdentifiableStage}.
 *
 * @author Matt Coley
 */
public class AbstractIdentifiableStage extends Stage implements IdentifiableStage {
	private final String id;

	/**
	 * @param id
	 * 		Unique stage identifier.
	 */
	public AbstractIdentifiableStage(String id) {
		this.id = id;

		// Add icon
		getIcons().add(Icons.getImage(Icons.LOGO));
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Stage asStage() {
		return this;
	}
}
