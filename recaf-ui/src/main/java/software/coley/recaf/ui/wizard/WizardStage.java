package software.coley.recaf.ui.wizard;

import jakarta.annotation.Nonnull;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.List;

/**
 * Stage that wraps a {@link Wizard}.
 *
 * @author Matt Coley
 * @see Wizard
 */
public class WizardStage extends Stage {
	/**
	 * @param pages
	 * 		Wizard pages.
	 * @param onFinish
	 * 		Action to run when wizard finishes.
	 */
	public WizardStage(@Nonnull List<Wizard.WizardPage> pages, @Nonnull Runnable onFinish) {
		Wizard wizard = new Wizard(pages);
		wizard.setOnFinish(() -> {
			onFinish.run();
			hide();
		});
		setScene(new Scene(new BorderPane(wizard)));
	}
}
