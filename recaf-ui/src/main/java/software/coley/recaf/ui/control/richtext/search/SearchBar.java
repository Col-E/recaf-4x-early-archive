package software.coley.recaf.ui.control.richtext.search;

import atlantafx.base.controls.CustomTextField;
import atlantafx.base.controls.Popover;
import atlantafx.base.controls.Spacer;
import atlantafx.base.theme.Styles;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import jregex.Matcher;
import org.fxmisc.richtext.CodeArea;
import org.kordamp.ikonli.carbonicons.CarbonIcons;
import software.coley.recaf.ui.config.KeybindingConfig;
import software.coley.recaf.ui.control.ActionButton;
import software.coley.recaf.ui.control.ActionMenuItem;
import software.coley.recaf.ui.control.BoundToggleIcon;
import software.coley.recaf.ui.control.FontIconView;
import software.coley.recaf.ui.control.richtext.Editor;
import software.coley.recaf.ui.control.richtext.EditorComponent;
import software.coley.recaf.util.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Search bar component, with layout and functionality inspired from IntelliJ's.
 *
 * @author Matt Coley
 */
@Dependent
public class SearchBar implements EditorComponent, EventHandler<KeyEvent> {
	private final KeybindingConfig keys;
	private Bar bar;

	@Inject
	public SearchBar(@Nonnull KeybindingConfig keys) {
		this.keys = keys;
	}

	@Override
	public void install(@Nonnull Editor editor) {
		bar = new Bar(editor);
		NodeEvents.addKeyPressHandler(editor, this);
	}

	@Override
	public void uninstall(@Nonnull Editor editor) {
		editor.setTop(null);
		NodeEvents.removeKeyPressHandler(editor, this);
		bar = null;
	}

	@Override
	public void handle(KeyEvent event) {
		if (keys.getFind().match(event)) {
			// Show if not visible
			if (!bar.isVisible())
				bar.show();

			// Grab focus
			bar.requestSearchFocus();
		}
	}

	/**
	 * The actual search bar.
	 */
	private static class Bar extends VBox {
		private static final int MAX_HISTORY = 10;
		private final SimpleIntegerProperty lastResultIndex = new SimpleIntegerProperty(-1);
		private final SimpleBooleanProperty hasResults = new SimpleBooleanProperty();
		private final SimpleBooleanProperty caseSensitivity = new SimpleBooleanProperty();
		private final SimpleBooleanProperty regex = new SimpleBooleanProperty();
		private final CustomTextField searchInput = new CustomTextField();
		private final ObservableList<String> pastSearches = FXCollections.observableArrayList();
		private final ObservableList<IntRange> resultRanges = FXCollections.observableArrayList();
		private final Editor editor;

		private Bar(Editor editor) {
			this.editor = editor;
			getStyleClass().add("search-bar");

			// Initially hidden.
			hide();

			// Refresh results when text changes.
			editor.getTextChangeEventStream()
					.successionEnds(Duration.ofMillis(150))
					.addObserver(changes -> refreshResults());

			// Remove border from search text field.
			searchInput.getStyleClass().addAll(Styles.ACCENT);

			// Create menu for search input left graphic (like IntelliJ) to display prior searches when clicked.
			Button oldSearches = new Button();
			oldSearches.setFocusTraversable(false);
			oldSearches.setDisable(true); // re-enabled when searches are populated.
			oldSearches.setGraphic(new FontIconView(CarbonIcons.SEARCH));
			oldSearches.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.ACCENT, Styles.FLAT, Styles.SMALL); // Tweaks.NO_ARROW
			searchInput.setLeft(oldSearches);

			// Create toggles for search input query modes.
			BoundToggleIcon toggleSensitivity = new BoundToggleIcon(Icons.CASE_SENSITIVITY, caseSensitivity).withTooltip("misc.casesensitive");
			BoundToggleIcon toggleRegex = new BoundToggleIcon(Icons.REGEX, regex).withTooltip("misc.regex");
			toggleSensitivity.setFocusTraversable(false);
			toggleRegex.setFocusTraversable(false);
			toggleSensitivity.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.ACCENT, Styles.FLAT, Styles.SMALL);
			toggleRegex.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.ACCENT, Styles.FLAT, Styles.SMALL);
			HBox inputToggles = new HBox(
					toggleSensitivity,
					toggleRegex
			);
			inputToggles.setAlignment(Pos.CENTER_RIGHT);
			searchInput.setRight(inputToggles);

			// Create label to display number of results.
			Label resultCount = new Label();
			resultCount.setMinWidth(30);
			resultCount.textProperty().bind(lastResultIndex.map(n -> {
				int i = n.intValue();
				if (i < 0) {
					return Lang.get("menu.search.noresults");
				} else {
					return (i + 1) + "/" + resultRanges.size();
				}
			}));
			resultRanges.addListener((ListChangeListener<IntRange>) c -> {
				if (resultRanges.isEmpty()) {
					resultCount.setTextFill(Color.RED);
				} else {
					resultCount.setTextFill(Color.GREEN);
				}
			});

			// Create buttons to iterate through results.
			Button prev = new ActionButton(CarbonIcons.ARROW_UP, this::prev);
			Button next = new ActionButton(CarbonIcons.ARROW_DOWN, this::next);
			prev.setFocusTraversable(false);
			next.setFocusTraversable(false);
			prev.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.ACCENT, Styles.SMALL);
			next.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.ACCENT, Styles.SMALL);
			prev.disableProperty().bind(hasResults.not());
			next.disableProperty().bind(hasResults.not());

			// Button to close the search bar.
			Button close = new ActionButton(CarbonIcons.CLOSE, this::hide);
			close.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.ACCENT, Styles.SMALL);
			close.setFocusTraversable(false);

			// Add to past searches when:
			//  - Enter pressed
			//  - Search hidden
			searchInput.setOnKeyPressed(e -> {
				String searchText = searchInput.getText();
				KeyCode code = e.getCode();
				if (code == KeyCode.ENTER) {
					pastSearches.remove(searchText);
					pastSearches.add(0, searchText);
					next();
				} else if (code == KeyCode.ESCAPE) {
					hide();
				}
				while (pastSearches.size() > MAX_HISTORY)
					pastSearches.remove(pastSearches.size() - 1);
			});
			searchInput.setOnKeyReleased(e -> refreshResults());

			// When past searches list is modified, update old search menu.
			pastSearches.addListener((ListChangeListener<String>) c -> {
				List<ActionMenuItem> items = pastSearches.stream()
						.map(search -> new ActionMenuItem(search, () -> {
							searchInput.setText(search);
							requestSearchFocus();
						}))
						.toList();
				if (items.isEmpty()) {
					oldSearches.setDisable(true);
				} else {
					oldSearches.setDisable(false);
					ContextMenu contextMenu = new ContextMenu();
					contextMenu.getItems().setAll(items);
					oldSearches.setOnMousePressed(e -> contextMenu.show(oldSearches, e.getScreenX(), e.getScreenY()));
				}
			});

			// Layout
			HBox prevAndNext = new HBox(prev, next);
			prevAndNext.setAlignment(Pos.CENTER);
			prevAndNext.setFillHeight(false);
			HBox searchLine = new HBox(searchInput, resultCount, prevAndNext, new Spacer(), close);
			searchLine.setAlignment(Pos.CENTER_LEFT);
			searchLine.setSpacing(10);
			searchLine.setPadding(new Insets(0, 5, 0, 0));
			getChildren().addAll(
					searchLine
					// TODO: Replace line (with ability to toggle its visibility)
			);
		}

		/**
		 * Refresh the results by scanning for what text ranges match
		 */
		private void refreshResults() {
			// Reset index before any results are found.
			lastResultIndex.set(-1);

			// Skip when there is nothing
			String search = searchInput.getText();
			if (search == null || search.isEmpty()) {
				resultRanges.clear();
				hasResults.set(false);
				return;
			}

			// Check for regex, then do a standard search if not regex.
			String text = editor.getText();
			List<IntRange> tempRanges = new ArrayList<>();
			if (regex.get()) {
				// Validate the regex.
				RegexUtil.RegexValidation validation = RegexUtil.validate(search);
				Popover popoverValidation = null;
				if (validation.valid()) {
					// It's valid, populate the ranges.
					Matcher matcher = RegexUtil.getMatcher(search, text);
					while (matcher.find())
						tempRanges.add(new IntRange(matcher.start(), matcher.end()));
				} else {
					// It's not valid. Tell the user what went wrong.
					popoverValidation = new Popover(new Label(validation.message()));
					popoverValidation.setHeaderAlwaysVisible(true);
					popoverValidation.setTitle("Invalid regex");
					popoverValidation.show(searchInput);
				}

				// Hide the prior popover if any exists.
				Object old = searchInput.getProperties().put("regex-popover", popoverValidation);
				if (old instanceof Popover oldPopover)
					oldPopover.hide();
			} else {
				// Modify the text/search for case-insensitive searches.
				if (!caseSensitivity.get()) {
					text = text.toLowerCase();
					search = search.toLowerCase();
				}

				// Loop over text, finding each index of matches.
				int size = search.length();
				int i = 0;
				while (i < text.length()) {
					int start = text.indexOf(search, i);
					if (start > -1) {
						int end = start + size;
						tempRanges.add(new IntRange(start, end));
						i = end;
					} else {
						break;
					}
				}
			}

			// Update properties.
			hasResults.set(!tempRanges.isEmpty());
			resultRanges.setAll(tempRanges);

			// Update selection to show the match closest to the user's current caret position.
			if (!tempRanges.isEmpty()) {
				CodeArea area = editor.getCodeArea();
				int lastMatchedTerm = area.getSelectedText().length();
				int caret = area.getCaretPosition();
				int searchStart = Math.max(0, caret - lastMatchedTerm - 1);
				int rangeIndex = CollectionUtil.sortedInsertIndex(resultRanges, new IntRange(searchStart, searchStart));
				if (rangeIndex >= resultRanges.size())
					rangeIndex = 0;
				lastResultIndex.set(rangeIndex);
				IntRange targetRange = resultRanges.get(rangeIndex);
				area.selectRange(targetRange.start(), targetRange.end());
			}
		}

		/**
		 * Select the next match.
		 */
		private void next() {
			// No ranges for current search query, so do nothing.
			if (resultRanges.isEmpty()) {
				lastResultIndex.set(-1);
				return;
			}

			// Get the next range index by doing a search starting from the current caret position + 1.
			CodeArea area = editor.getCodeArea();
			int caret = area.getCaretPosition() + 1;
			int rangeIndex = CollectionUtil.sortedInsertIndex(resultRanges, new IntRange(caret, caret));
			if (rangeIndex >= resultRanges.size())
				rangeIndex = 0;

			// Set index & select the range.
			lastResultIndex.set(rangeIndex);
			IntRange range = resultRanges.get(rangeIndex);
			area.selectRange(range.start(), range.end());
		}

		/**
		 * Select the previous match.
		 */
		private void prev() {
			// No ranges for current search query, so do nothing.
			if (resultRanges.isEmpty()) {
				lastResultIndex.set(-1);
				return;
			}

			// Get the previous range index by doing a search starting from the current selection position in the text,
			// then go back by one position, wrapping around if necessary.
			CodeArea area = editor.getCodeArea();
			int caret = area.getCaretPosition();
			int rangeIndex = CollectionUtil.sortedInsertIndex(resultRanges, new IntRange(caret - area.getSelectedText().length(), caret)) - 1;
			if (rangeIndex < 0)
				rangeIndex = resultRanges.size() - 1;

			// Set index & select the range.
			lastResultIndex.set(rangeIndex);
			IntRange range = resultRanges.get(rangeIndex);
			area.selectRange(range.start(), range.end());
		}

		/**
		 * Show the search bar.
		 */
		public void show() {
			setVisible(true);
			setDisable(false);
			editor.setTop(this);

			// If the editor has selected text, we will copy it to the search input field.
			CodeArea codeArea = editor.getCodeArea();
			String selectedText = codeArea.getSelectedText();
			if (!selectedText.isBlank())
				searchInput.setText(selectedText);
		}

		/**
		 * Hide the search bar.
		 */
		private void hide() {
			setVisible(false);
			setDisable(true);
			editor.setTop(null);

			// Need to send focus back to the editor's code-area.
			// Doesn't work without the delay when handled from 'ESCAPE' key-event.
			FxThreadUtil.delayedRun(1, () -> editor.getCodeArea().requestFocus());
		}

		/**
		 * Focus the search input field, and select the text so users can quickly retype something new if desired.
		 */
		private void requestSearchFocus() {
			searchInput.requestFocus();
			searchInput.selectAll();
		}
	}
}
