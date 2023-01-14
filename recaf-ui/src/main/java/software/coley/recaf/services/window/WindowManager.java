package software.coley.recaf.services.window;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import software.coley.collections.observable.ObservableList;
import software.coley.recaf.services.Service;

import java.util.ConcurrentModificationException;

/**
 * Manages active {@link Stage} windows.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class WindowManager implements Service {
	public static final String SERVICE_ID = "window-manager";
	private final WindowManagerConfig config;
	private final ObservableList<Stage> windows = new ObservableList<>();

	@Inject
	public WindowManager(WindowManagerConfig config) {
		this.config = config;
	}

	/**
	 * Register listeners on the stage to monitor active state.
	 *
	 * @param stage
	 * 		Stage to register.
	 */
	public void register(Stage stage) {
		EventHandler<WindowEvent> baseOnShown = stage.getOnShown();
		EventHandler<WindowEvent> baseOnHidden = stage.getOnHidden();

		// Wrap original handlers to keep existing behavior.
		// Record when windows are 'active' based on visibility.
		EventHandler<WindowEvent> onShown = e -> {
			windows.add(stage);
			if (baseOnShown != null) baseOnShown.handle(e);
		};
		EventHandler<WindowEvent> onHidden = e -> {
			windows.remove(stage);
			if (baseOnHidden != null) baseOnHidden.handle(e);
		};
		stage.setOnShown(onShown);
		stage.setOnHidden(onHidden);

		// If state is already visible, add it right away.
		if (stage.isShowing()) windows.add(stage);
	}

	/**
	 * Do not use this list to iterate over if within your loop you will be closing/creating windows.
	 * This will cause a {@link ConcurrentModificationException}. Wrap this result in a new collection
	 * if you want to do that.
	 *
	 * @return Active windows.
	 */
	public ObservableList<Stage> getWindows() {
		return windows;
	}

	@Nonnull
	@Override
	public String getServiceId() {
		return SERVICE_ID;
	}

	@Nonnull
	@Override
	public WindowManagerConfig getServiceConfig() {
		return config;
	}
}
