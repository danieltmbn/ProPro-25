/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.view.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import tuda.ai1.propro25.view.GUIManager;

/**
 * Controller für das Einstellungsmenü der Schach-Anwendung.
 * <p>
 * Der SettingsMenuController erlaubt dem Nutzer, Einstellungen wie die Farben
 * des Schachbretts und die Aktivierung von Soundeffekten zu ändern. Die
 * Änderungen können sofort oder bei Verfügbarkeit des Spielfelds angewendet
 * werden.
 * </p>
 *
 * <p>
 * Abhängig davon, ob das Menü aus dem Hauptmenü oder aus dem Spiel heraus
 * geöffnet wurde, kehrt es entweder ins Hauptmenü zurück oder setzt das Spiel
 * fort.
 * </p>
 *
 * <p>
 * Hinweis: Der JavaFX {@link ColorPicker} hat bekannte Darstellungsprobleme auf
 * Multi-Monitor-Setups, insbesondere bei der Anzeige des benutzerdefinierten
 * Farbdialogs.
 * </p>
 *
 * @author TUDA AI1
 * @version ProPro 2025
 */
public class SettingsMenuController {
	/** Der GUIManager zur Koordination der Oberflächenkomponenten. */
	private GUIManager guiManager;
	/** Die aktuelle JavaFX Stage, auf der das Menü dargestellt wird. */
	private Stage stage;

	/** Die Wurzel des Einstellungsmenüs. */
	@FXML
	private StackPane root;

	/** Farbauswahl für helle Felder. */
	@FXML
	private ColorPicker lightFieldColorPicker; // javafx colorpicker is bugged on multi monitor setups which
												// leads to the custom color dialog opening up offscreen sometimes
												// (potential fix would be to use java swing colorpicker)
	/** Farbauswahl für dunkle Felder. */
	@FXML
	private ColorPicker darkFieldColorPicker;

	/** Checkbox zur Aktivierung/Deaktivierung von Soundeffekten. */
	@FXML
	private CheckBox soundCheckBox;

	/**
	 * Zwischengespeicherte Auswahl für helle Felder, falls das Spielfeld noch nicht
	 * bereit ist.
	 */
	private javafx.scene.paint.Color pendingLightColor = null;
	/**
	 * Zwischengespeicherte Auswahl für dunkle Felder, falls das Spielfeld noch
	 * nicht bereit ist.
	 */
	private javafx.scene.paint.Color pendingDarkColor = null;

	/**
	 * Gibt die zwischengespeicherte Farbe für helle Felder zurück.
	 * 
	 * @return die Farbe oder {@code null}, falls keine zwischengespeichert wurde
	 */
	public Color getPendingLightColor() {
		return pendingLightColor;
	}

	/**
	 * Gibt die zwischengespeicherte Farbe für dunkle Felder zurück.
	 * 
	 * @return die Farbe oder {@code null}, falls keine zwischengespeichert wurde
	 */
	public Color getPendingDarkColor() {
		return pendingDarkColor;
	}

	/**
	 * Löscht die zwischengespeicherte Farbe für dunkle Felder.
	 */
	public void clearPendingDarkColor() {
		pendingDarkColor = null;
	}

	/**
	 * Löscht die zwischengespeicherte Farbe für helle Felder.
	 */
	public void clearPendingLightColor() {
		pendingLightColor = null;
	}

	public void setCachedColoros() {
		if (pendingLightColor != null)
			lightFieldColorPicker.setValue(pendingLightColor);
		if (pendingDarkColor != null)
			darkFieldColorPicker.setValue(pendingDarkColor);
	}

	/**
	 * Gibt an, von welcher Stelle aus das Einstellungsmenü geöffnet wurde.
	 */
	public enum SettingsSource {
		/** Das Menü wurde aus dem Hauptmenü geöffnet. */
		START_MENU,
		/** Das Menü wurde während eines Spiels geöffnet. */
		IN_GAME
	}

	/** Quelle, von der aus das Einstellungsmenü geöffnet wurde. */
	private SettingsSource source = SettingsSource.START_MENU;

	private static final SettingsMenuController INSTANCE = new SettingsMenuController();

	private SettingsMenuController() {
	}

	public static SettingsMenuController getInstance() {
		return INSTANCE;
	}
	/**
	 * Setzt den Kontext für diesen Controller (GUIManager, Stage, Aufrufquelle).
	 *
	 * @param guiManager
	 *            GUIManager-Instanz
	 * @param stage
	 *            Die aktuelle Stage
	 * @param source
	 *            Die Quelle, von der das Menü geöffnet wurde
	 */
	public void setContext(GUIManager guiManager, Stage stage, SettingsSource source) {
		this.guiManager = guiManager;
		this.stage = stage;
		this.source = source;

		initializeScene();
	}

	/**
	 * Initialisiert das Einstellungsmenü, richtet Event-Handler ein und
	 * positioniert es.
	 */
	private void initializeScene() {
		// Constrain the menu pane (not just the root)
		BorderPane menu = (BorderPane) root.lookup("#menuBorderPane");
		lightFieldColorPicker.setValue(javafx.scene.paint.Color.BEIGE);
		darkFieldColorPicker.setValue(javafx.scene.paint.Color.SADDLEBROWN);

		if (source == SettingsSource.IN_GAME) {
			root.getStyleClass().add("transparent-root"); // apply transparent style
			root.setStyle("-fx-background-image: none; -fx-background-color: transparent;");
		}
		lightFieldColorPicker.setOnAction(e -> {
			javafx.scene.paint.Color selectedColor = lightFieldColorPicker.getValue();

			if (guiManager.isBoardReady()) {
				guiManager.setLightFieldColor(selectedColor);
				pendingLightColor = selectedColor;
			} else {
				pendingLightColor = selectedColor;
			}
		});

		darkFieldColorPicker.setOnAction(e -> {
			javafx.scene.paint.Color selectedColor = darkFieldColorPicker.getValue();

			if (guiManager.isBoardReady()) {
				guiManager.setDarkFieldColor(selectedColor);
				pendingDarkColor = selectedColor;
			} else {
				pendingDarkColor = selectedColor;
			}
		});

		soundCheckBox.setSelected(guiManager.isSoundEnabled());

		// Listen for changes and update GUIManager
		soundCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> guiManager.enableSound(newVal));

		Platform.runLater(() -> {
			double stageHeight = stage.getHeight() > 0 ? stage.getHeight() : 720;
			double width = stageHeight / 2;
			double height = stageHeight / 1.8;
			menu.setPrefSize(width, height);

			menu.setMaxSize(width, height);
			StackPane.setAlignment(menu, Pos.CENTER);
		});

		root.setOnMouseClicked(event -> {
			// Only go back if click is outside the menu
			if (!menu.localToScene(menu.getBoundsInLocal()).contains(event.getSceneX(), event.getSceneY())) {
				navigateBack();
			}
		});
	}

	/**
	 * Platzhalter für Sound-Handler, derzeit ungenutzt.
	 */
	@FXML
	private void handleSound() {

	}

	/**
	 * Navigiert zurück ins Hauptmenü oder ins Spiel, je nach Ursprungsansicht.
	 */
	private void navigateBack() {
		if (source == SettingsSource.START_MENU) {
			guiManager.showStartMenu(stage);
		} else if (source == SettingsSource.IN_GAME) {
			guiManager.resumeGame(); // or however you return to game
		}
	}

	/**
	 * Wird vom "Zurück"-Button aufgerufen und kehrt zur vorherigen Ansicht zurück.
	 */
	@FXML
	public void onBack() {
		navigateBack();
	}

	/**
	 * Setzt die Farben für helle und dunkle Felder auf Standard zurück.
	 */
	@FXML
	public void handleResetColors() {
		if (guiManager.isBoardReady()) {
			guiManager.setDarkFieldColor(null);
			guiManager.setLightFieldColor(null);
		}
		lightFieldColorPicker.setValue(Color.BEIGE);
		darkFieldColorPicker.setValue(Color.SADDLEBROWN);
		clearPendingDarkColor();
		clearPendingLightColor();
	}
}
