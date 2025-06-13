/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.view;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import tuda.ai1.propro25.controller.GameController;
import tuda.ai1.propro25.model.*;
import tuda.ai1.propro25.model.piece.Piece;
import tuda.ai1.propro25.view.controller.EndScreenController;
import tuda.ai1.propro25.view.controller.SettingsMenuController;
import tuda.ai1.propro25.view.input.BoardInputHandler;
import tuda.ai1.propro25.view.renderer.BoardRenderer;
import tuda.ai1.propro25.view.renderer.ClockRenderer;
import tuda.ai1.propro25.view.renderer.MoveHistoryMenuRenderer;
import tuda.ai1.propro25.view.renderer.PauseMenuRenderer;
import tuda.ai1.propro25.view.util.FxmlWithController;
import tuda.ai1.propro25.view.util.MenuLoader;
import tuda.ai1.propro25.view.util.Sound;
import tuda.ai1.propro25.view.util.SoundManager;

/**
 * Diese Klasse verwaltet die grafische Benutzeroberfläche (GUI) des
 * Schachspiels. Sie enthält alle relevanten Methoden zum Erstellen, Anzeigen
 * und Aktualisieren des Spielbretts, der Uhren und der Benutzerinteraktionen
 * wie Tastatur- und Mausereignisse.
 */
public class GUIManager extends Application {
	/** Der Controller, der die Spiellogik verwaltet. */
	private GameController controller;
	/** Renderer für das Schachbrett. */
	private BoardRenderer boardRenderer;
	/** Renderer für die Schachuhr. */
	private ClockRenderer clockRenderer;
	/** Handler für Eingaben durch den Benutzer (Maus). */
	BoardInputHandler inputHandler;
	/** Renderer für das Pausen Menü */
	private PauseMenuRenderer pauseMenuRenderer;
	/** Renderer für Zugverlauf-Menü */
	MoveHistoryMenuRenderer moveHistoryMenuRenderer;
	private SoundManager soundManager;
	private GameSceneBuilder gameSceneBuilder;

	private SettingsMenuController settingsMenuController;
	private boolean isBoardReady = false;

	private boolean soundEnabled = true;
	Stage stage;

	/**
	 * Initialisiert die GUI und den Controller sowie wichtige Listener für die
	 * Eingaben. Diese Methode wird beim Starten der Anwendung aufgerufen.
	 */
	@Override
	public void init() {
		this.soundManager = new SoundManager();
	}

	/**
	 * Startet die GUI-Anwendung. Setzt die Stage und zeigt das Fenster an.
	 *
	 * @param stage
	 *            Die Stage für die GUI-Anwendung.
	 */
	@Override
	public void start(Stage stage) {
		this.stage = stage;

		Platform.runLater(() -> {
			stage.setTitle("ProPro Schach");
			stage.getIcons().add(new Image(
					Objects.requireNonNull(GUIManager.class.getResourceAsStream("/images/sc-logo_quadrat_gross.png"))));
			// Setup stage size etc.
			var bounds = Screen.getPrimary().getBounds();
			stage.setWidth(bounds.getWidth() * 0.75);
			stage.setHeight(bounds.getHeight() * 0.75);
			stage.setMinWidth(bounds.getWidth() / 2);
			stage.setMinHeight(bounds.getHeight() / 2);

			showStartMenu(stage);
			stage.centerOnScreen();
			stage.show();

			Platform.runLater(stage::centerOnScreen);

			stage.setOnCloseRequest(event -> {
				event.consume(); // prevent immediate exit
				if (gameSceneBuilder == null) {
					stage.close();
					return;
				}
				if (!gameSceneBuilder.getChildren().contains(pauseMenuRenderer)) {
					gameSceneBuilder.getChildren().add(pauseMenuRenderer);
				}
			});

		});
	}

	@Override
	public void stop() {
		if (controller != null) {
			controller.onGameStop();
		}
	}

	/**
	 * Setzt eine neue aktive Scene und setzt die Fenstergröße neu, um ein GUI
	 * Update zu forcieren. Die ist als Workaround auf manchen System nötig (Linux
	 * mit Cinnamon Desktop).
	 */
	private void setSceneAndForceRedraw(Stage stage, Scene newScene) {
		stage.setScene(newScene);
		newScene.getWindow().setWidth(newScene.getWindow().getWidth());
	}

	/**
	 * Konfiguriert das Layout und die visuellen Komponenten der GUI.
	 */
	private void setupBoardGUI(Stage stage, GameController controller) {
		moveHistoryMenuRenderer = new MoveHistoryMenuRenderer(createExportButton());
		boardRenderer = new BoardRenderer();
		clockRenderer = new ClockRenderer(moveHistoryMenuRenderer, controller);
		gameSceneBuilder = new GameSceneBuilder(boardRenderer, clockRenderer);
		pauseMenuRenderer = new PauseMenuRenderer(stage, gameSceneBuilder, this);

		inputHandler = new BoardInputHandler(boardRenderer);
		// Setup input events
		inputHandler.setupMouseHandler();
		inputHandler.setOnTileClicked(controller::onTileClicked);

		Scene gameScene = gameSceneBuilder.buildScene();
		gameScene.setOnKeyPressed(this::handleKeyPress);
		gameScene.getStylesheets()
				.add(Objects.requireNonNull(getClass().getResource("/styles/style.css")).toExternalForm());

		// Redraw on resize
		boardRenderer.getCanvas().widthProperty().addListener((obs, o, n) -> {
			if (Math.abs(n.doubleValue() - o.doubleValue()) > 1.0) {
				refreshBoard();
				closeDialogue();
			}
		});
		boardRenderer.getCanvas().heightProperty().addListener((obs, o, n) -> {
			if (Math.abs(n.doubleValue() - o.doubleValue()) > 1.0) {
				refreshBoard();
				closeDialogue();
			}
		});
		hookUndoButton();

		Platform.runLater(() -> {
			setSceneAndForceRedraw(stage, gameScene);
			isBoardReady = true;
			applyPendingSettings(settingsMenuController);
			clockRenderer.updateClockLabel(controller.getCurrentPlayer().getRemainingTime(),
					controller.getCurrentPlayer().getColor());
			clockRenderer.updateClockLabel(controller.getNextPlayer().getRemainingTime(),
					controller.getNextPlayer().getColor());
		});
	}

	/**
	 * Zeigt Startbildschirm
	 *
	 * @param stage
	 */
	public void showStartMenu(Stage stage) {
		Scene scene = new Scene(MenuLoader.loadStartMenu(this, stage));
		Platform.runLater(() -> setSceneAndForceRedraw(stage, scene));
	}

	/**
	 * Zeigt settinsgsMenu
	 *
	 * @param stage
	 */
	public void showSettingsMenu(Stage stage) {
		try {
			FxmlWithController<SettingsMenuController> settingsMenu = MenuLoader.loadSettingsMenu(this, stage,
					SettingsMenuController.SettingsSource.START_MENU);
			settingsMenuController = settingsMenu.controller();

			Platform.runLater(() -> setSceneAndForceRedraw(stage, new Scene(settingsMenu.root())));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Zeigt creditsMenu
	 *
	 * @param stage
	 */
	public void showCreditsMenu(Stage stage) {
		try {
			Scene scene = new Scene(MenuLoader.loadCreditsMenu(this, stage));
			Platform.runLater(() -> setSceneAndForceRedraw(stage, scene));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Zeigt createGameMenu
	 *
	 * @param stage
	 */
	public void showCreateGameMenu(Stage stage) {
		Platform.runLater(() -> {
			Scene scene = new Scene(MenuLoader.loadCreateGameMenu(this, stage));
			setSceneAndForceRedraw(stage, scene);
		});
	}

	/**
	 * Zeigt den Endscreen an
	 *
	 * @param string
	 *            message auf dem Endscreen
	 */
	public void showEndScreen(String string) {
		FxmlWithController<EndScreenController> fxml = MenuLoader.loadEndScreen();
		Parent endScreen = fxml.root();
		EndScreenController controller = fxml.controller();

		controller.setOnBackToMenu(() -> {
			clear();
			this.showStartMenu(stage);
		}); // falls nötig
		endScreen.setVisible(false);
		endScreen.setMouseTransparent(false);

		gameSceneBuilder.getChildren().add(endScreen);
		Platform.runLater(() -> controller.show(string));
	}

	/**
	 * Updated Move Historie
	 */
	public void updateMoveHistoryUI(String moveStr) {
		moveHistoryMenuRenderer.addMove(moveStr);
	}

	/**
	 * Setzt Action des UndoButtons
	 */
	private void hookUndoButton() {
		Button button = clockRenderer.getMoveHistory().getUndoButton();
		button.setOnAction(e -> controller.onUndoClicked());
	}

	private Button createExportButton() {
		Button exportButton = new Button("Exportieren...");
		exportButton.setOnAction(e -> controller.onExportClicked());
		exportButton.getStyleClass().add("main-button");
		exportButton.setMaxHeight(10);
		return exportButton;
	}

	/**
	 * Handhabt Tastatureingaben. Reagiert auf spezifische Tasten wie F für Flipping
	 * des Schachbretts, U für Undo und Esc für Öffnen des Pause-Menüs
	 *
	 * @param event
	 *            Das KeyEvent, das die gedrückte Taste enthält.
	 */
	private void handleKeyPress(KeyEvent event) {
		switch (event.getCode()) {
			case F -> {
				boardRenderer.flipFlipped();
				refreshBoard();
				clockRenderer.swapClocks();
			}
			case U -> {
				if (controller.canUndo()) {
					controller.onUndoClicked();
				}
			}
			case ESCAPE -> {
				if (pauseMenuRenderer.isShown()) {
					pauseMenuRenderer.close();
				} else {
					pauseMenuRenderer.show();
				}
			}
		}
	}

	public void closeDialogue() {
		Platform.runLater(() -> gameSceneBuilder.closeDialogue());
	}

	/**
	 * Zeigt das Dialogfenster für die Beförderung eines Bauern an und gibt die
	 * ausgewählte Figur zurück.
	 *
	 * @param position
	 *            Die Position, an der der Bauer umgewandlet wird
	 * @param color
	 *            Die Farbe des Spielers, der die Beförderung vornimmt.
	 * @param onSelected
	 *            Callback der aufgerufen wird wenn der Bauer
	 */
	public void showPromotionDialogAsync(Coordinate position, Color color, Consumer<Piece> onSelected) {
		gameSceneBuilder.showPromotionDialogAsync(position, color, onSelected);
	}

	/**
	 * Aktualisiert das Schachbrett mit den neuesten Zügen und Änderungen. Diese
	 * Methode wird aufgerufen, wenn das Brett neu gezeichnet werden muss.
	 */
	private void refreshBoard() {
		Platform.runLater(() -> boardRenderer.redrawBoard(controller.getBoard()));
	}

	/**
	 * Löscht alle Markierungen auf dem Schachbrett und aktualisiert Uhr highlight
	 */
	public void clearHighlights() {
		boardRenderer.clearHighlights();
		clockRenderer.highlightActivePlayer(controller.getCurrentColor());
		refreshBoard();
	}

	/**
	 * Markiert bestimmte Felder auf dem Schachbrett, basierend auf den Koordinaten.
	 *
	 * @param origin
	 *            Die Ausgangsposition des Zugs.
	 * @param coords
	 *            Die Koordinaten der Felder, die markiert werden sollen.
	 */
	public void highlightTiles(Coordinate origin, List<Move> coords) {
		if (origin == null) {
			return;
		}
		boardRenderer.highlightTile(BoardRenderer.HighlightType.FULL, origin.getFile(), origin.getRank());

		if (!coords.isEmpty()) {
			coords.forEach(move -> {
				if (move.getType() == MoveType.CAPTURE || move.getType() == MoveType.CAPTURE_PROMOTION) {
					boardRenderer.highlightTile(BoardRenderer.HighlightType.CAPTURE, move.getTo().getFile(),
							move.getTo().getRank());
				} else {
					boardRenderer.highlightTile(BoardRenderer.HighlightType.CIRCLE, move.getTo().getFile(),
							move.getTo().getRank());
				}
			});
		}
		refreshBoard();
	}

	/**
	 * Falls @code{disable} true, deaktiviert undo Knopf, aktiviert wenn false
	 */
	public void disableUndoButton(boolean disable) {
		gameSceneBuilder.disableUndoButton(disable);
	}

	/**
	 * Gibt MoveHistory zurück
	 *
	 * @return Mo
	 */
	public MoveHistoryMenuRenderer getMoveHistoryRenderer() {
		return clockRenderer.getMoveHistory();
	}

	/**
	 * Weist den boardRenderer an einen Move zu highlighten
	 *
	 * @param move
	 *            der Move der gehighlightet werden soll
	 */
	public void highlightMove(Move move) {
		if (move == null) {
			return;
		}
		boardRenderer.highlightTile(BoardRenderer.HighlightType.FULL, move.getFrom().getFile(),
				move.getFrom().getRank());
		boardRenderer.highlightTile(BoardRenderer.HighlightType.FULL, move.getTo().getFile(), move.getTo().getRank());
		refreshBoard();
	}

	public void showErrorScreen() {
		Alert alert = new Alert(Alert.AlertType.ERROR,
				"Etwas ist kaputt.\nPrüfe die Konsolenausgabe auf Fehlermeldungen!");
		alert.showAndWait();
	}

	/**
	 * Initialisiert den GameController mit einem bestehenden Board
	 * 
	 * @param board
	 *            das Board, auf dem gespielt werden soll
	 * @param increment
	 *            Inkrement
	 */
	public void setUpBoard(Board board, int increment) {
		controller = new GameController(board, this, increment);
		setupBoardGUI(stage, controller);
	}

	/**
	 * Spielt den passenden Sound basierend auf dem Movetype
	 *
	 * @param moveType
	 *            Art des Zuges
	 */
	public void playSound(MoveType moveType) {
		if (moveType == null) {
			throw new IllegalArgumentException("Move type darf nicht null sein!");
		}
		if (moveType == MoveType.CAPTURE_PROMOTION || moveType == MoveType.CAPTURE || moveType == MoveType.EN_PASSANT) {
			soundManager.playSound(Sound.CAPTURE);
		} else {
			soundManager.playSound(Sound.MOVE);
		}
	}

	/**
	 * Erlaubt, einen Sound direkt abzuspielen
	 *
	 * @param sound
	 *            Sound, der abgespielt werden soll
	 */
	public void playSound(Sound sound) {
		soundManager.playSound(sound);
	}

	public void enableSound(boolean enableSound) {
		soundEnabled = !soundEnabled;
		soundManager.setSoundEnabled(enableSound);
	}

	/**
	 *
	 * @return ob der Ton an ist
	 */
	public boolean isSoundEnabled() {
		return soundEnabled;
	}

	/**
	 * Hebt die aktive Spieleruhr hervor.
	 *
	 * @param color
	 *            Die Farbe des aktiven Spielers (WEISS oder SCHWARZ).
	 */
	public void highlightActivePlayer(Color color) {
		clockRenderer.highlightActivePlayer(color);
	}

	/**
	 * Startet den Timer für beide Seiten
	 */
	public void startTimer() {
		clockRenderer.startTimer();
	}

	/**
	 * Stoppt den Timer für beide Seiten
	 */
	public void deleteTimer() {
		clockRenderer.deleteTimer();
	}

	public void resumeGame() {
		pauseMenuRenderer.closeSettings();
	}

	/**
	 * Stoppt den Timer und setzt alle Referenzen auf 0, damit das Spiel sauber
	 * garbage collected wird. Wichtig für return to main menu
	 */
	public void clear() {
		deleteTimer();
		this.boardRenderer = null;
		this.gameSceneBuilder = null;
		this.moveHistoryMenuRenderer = null;
		this.clockRenderer = null;
		this.pauseMenuRenderer = null;
		isBoardReady = false;

	}

	/**
	 * Wendet cached settings an. Diese existieren, wenn Einstellungen vorgenommen
	 * wurden bevor das Spiel gestartet wurde
	 *
	 * @param settingsController
	 *            der settings controller
	 */
	private void applyPendingSettings(SettingsMenuController settingsController) {
		if (settingsController == null)
			return;
		if (settingsController.getPendingLightColor() != null) {
			setLightFieldColor(settingsController.getPendingLightColor());
		}
		if (settingsController.getPendingDarkColor() != null) {
			setDarkFieldColor(settingsController.getPendingDarkColor());
		}
	}

	/**
	 * Setzt Farbe des hellen Feldes
	 *
	 * @param selectedColor
	 *            ausgewählte Feldfarbe für das weiße Feld
	 */
	public void setLightFieldColor(javafx.scene.paint.Color selectedColor) {
		boardRenderer.setLightFieldColor(selectedColor);
		refreshBoard();
	}

	/**
	 * Setzt Farbe des dunklen Feldes
	 *
	 * @param selectedColor
	 *            ausgewählte Feldfarbe für das schwarze Feld
	 */
	public void setDarkFieldColor(javafx.scene.paint.Color selectedColor) {
		boardRenderer.setDarkFieldColor(selectedColor);
		refreshBoard();
	}

	/**
	 * @return ob das Board schon initialisiert wurde
	 */
	public boolean isBoardReady() {
		return isBoardReady;
	}

	/**
	 * Öffnet ein Dialog-Fenster, das die UI des angegebenen root enthält. Dieses
	 * Fenster ist immer über dem Hauptfenster.
	 *
	 * @param root
	 *            die UI des Dialogs
	 * @param title
	 *            der Titel des Fensters
	 */
	public void openDialogWindow(Parent root, String title) {
		Platform.runLater(() -> {
			Stage dialogStage = new Stage();
			dialogStage.setTitle(title);
			dialogStage.initOwner(this.stage);
			dialogStage.initModality(Modality.APPLICATION_MODAL);
			setSceneAndForceRedraw(dialogStage, new Scene(root));
			dialogStage.getIcons().add(new Image(
					Objects.requireNonNull(GUIManager.class.getResourceAsStream("/images/sc-logo_quadrat_gross.png"))));

			dialogStage.showAndWait();
		});
	}
}
