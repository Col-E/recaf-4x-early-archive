package software.coley.recaf.ui.docking;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.coley.recaf.ui.docking.listener.TabClosureListener;
import software.coley.recaf.ui.docking.listener.TabCreationListener;
import software.coley.recaf.ui.docking.listener.TabMoveListener;
import software.coley.recaf.ui.docking.listener.TabSelectionListener;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class DockingManager {
	private final DockingRegionFactory factory = new DockingRegionFactory(this);
	private final DockingRegion primaryRegion;
	private final List<DockingRegion> regions = new ArrayList<>();
	private final List<TabSelectionListener> tabSelectionListeners = new ArrayList<>();
	private final List<TabCreationListener> tabCreationListeners = new ArrayList<>();
	private final List<TabClosureListener> tabClosureListeners = new ArrayList<>();
	private final List<TabMoveListener> tabMoveListeners = new ArrayList<>();

	@Inject
	public DockingManager() {
		primaryRegion = newRegion();
		primaryRegion.setCloseIfEmpty(false);
	}

	// Need to track:
	//  - Access to all open tabs
	//  - Access to all open regions
	//  - Current open tabs (and which is the most recently interacted with)

	// Need to do:
	//  - Open new tab (of type, like for a class)
	//    - Can just slap them in the default region, now that we keep that one around.
	//    - Allow edge cases:
	//       - SPAWN_IN_DEFAULT
	//       - SPAWN_IN_ORIGINATOR

	/**
	 * The primary region is where tabs should open by default.
	 * It is locked to the main window where the initial workspace info is displayed.
	 * Compared to an IDE, this would occupy the same space where open classes are shown.
	 *
	 * @return Primary region.
	 */
	public DockingRegion getPrimaryRegion() {
		return primaryRegion;
	}

	/**
	 * @return All current docking regions.
	 */
	public List<DockingRegion> getRegions() {
		return regions;
	}

	/**
	 * @return All current docking tabs.
	 */
	public List<DockingTab> getDockTabs() {
		return regions.stream().flatMap(r -> r.getDockTabs().stream()).toList();
	}

	/**
	 * @return New rgion.
	 */
	public DockingRegion newRegion() {
		DockingRegion region = factory.create();
		factory.init(region);
		regions.add(region);
		return region;
	}

	/**
	 * Configured by {@link DockingRegionFactory} this method is called when a {@link DockingRegion} is closed.
	 *
	 * @param region
	 * 		Region being closed.
	 *
	 * @return {@code true} when region closure was a success.
	 * {@code false} when a region denied closure.
	 */
	boolean onRegionClose(DockingRegion region) {
		// Close any tabs that are closable.
		// If there are tabs that cannot be closed, deny region closure.
		boolean allowClosure = true;
		for (DockingTab tab : new ArrayList<>(region.getDockTabs()))
			if (tab.isClosable())
				tab.close();
			else
				allowClosure = false;
		if (!allowClosure)
			return false;

		// Update internal state
		regions.remove(region);

		// Needed in case a window containing the region gets closed
		for (DockingTab tab : new ArrayList<>(region.getDockTabs()))
			tab.close();

		// Closure allowed.
		return true;
	}

	/**
	 * Configured by {@link DockingRegion#createTab(DockingTabFactory)}, called when a tab is created.
	 *
	 * @param parent
	 * 		Parent region.
	 * @param tab
	 * 		Tab created.
	 */
	void onTabCreate(DockingRegion parent, DockingTab tab) {
		for (TabCreationListener listener : tabCreationListeners)
			listener.onCreate(parent, tab);
	}

	/**
	 * Configured by {@link DockingRegion#createTab(DockingTabFactory)}, called when a tab is closed.
	 *
	 * @param parent
	 * 		Parent region.
	 * @param tab
	 * 		Tab created.
	 */
	void onTabClose(DockingRegion parent, DockingTab tab) {
		for (TabClosureListener listener : tabClosureListeners)
			listener.onClose(parent, tab);
	}

	/**
	 * Configured by {@link DockingRegion#createTab(DockingTabFactory)}, called when a tab is
	 * moved between {@link DockingRegion}s.
	 *
	 * @param oldRegion
	 * 		Prior parent region.
	 * @param newRegion
	 * 		New parent region.
	 * @param tab
	 * 		Tab created.
	 */
	void onTabMove(DockingRegion oldRegion, DockingRegion newRegion, DockingTab tab) {
		for (TabMoveListener listener : tabMoveListeners)
			listener.onMove(oldRegion, newRegion, tab);
	}

	/**
	 * Configured by {@link DockingRegion#createTab(DockingTabFactory)}, called when a tab is selected.
	 *
	 * @param parent
	 * 		Parent region.
	 * @param tab
	 * 		Tab created.
	 */
	void onTabSelection(DockingRegion parent, DockingTab tab) {
		for (TabSelectionListener listener : tabSelectionListeners)
			listener.onSelection(parent, tab);
	}

	/**
	 * @param listener
	 * 		Listener to add.
	 */
	public void addTabSelectionListener(TabSelectionListener listener) {
		tabSelectionListeners.add(listener);
	}

	/**
	 * @param listener
	 * 		Listener to remove.
	 *
	 * @return {@code true} upon removal. {@code false} when listener wasn't present.
	 */
	public boolean removeTabSelectionListener(TabSelectionListener listener) {
		return tabSelectionListeners.remove(listener);
	}

	/**
	 * @param listener
	 * 		Listener to add.
	 */
	public void addTabCreationListener(TabCreationListener listener) {
		tabCreationListeners.add(listener);
	}

	/**
	 * @param listener
	 * 		Listener to remove.
	 *
	 * @return {@code true} upon removal. {@code false} when listener wasn't present.
	 */
	public boolean removeTabCreationListener(TabCreationListener listener) {
		return tabCreationListeners.remove(listener);
	}

	/**
	 * @param listener
	 * 		Listener to add.
	 */
	public void addTabClosureListener(TabClosureListener listener) {
		tabClosureListeners.add(listener);
	}

	/**
	 * @param listener
	 * 		Listener to remove.
	 *
	 * @return {@code true} upon removal. {@code false} when listener wasn't present.
	 */
	public boolean removeTabClosureListener(TabClosureListener listener) {
		return tabClosureListeners.remove(listener);
	}

	/**
	 * @param listener
	 * 		Listener to add.
	 */
	public void addTabMoveListener(TabMoveListener listener) {
		tabMoveListeners.add(listener);
	}

	/**
	 * @param listener
	 * 		Listener to remove.
	 *
	 * @return {@code true} upon removal. {@code false} when listener wasn't present.
	 */
	public boolean removeTabMoveListener(TabMoveListener listener) {
		return tabMoveListeners.remove(listener);
	}
}
