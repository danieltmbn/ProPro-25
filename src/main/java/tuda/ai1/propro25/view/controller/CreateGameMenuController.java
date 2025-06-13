/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.view.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tuda.ai1.propro25.ai.DeepeningAI;
import tuda.ai1.propro25.ai.PruningAI;
import tuda.ai1.propro25.ai.SearchAI;
import tuda.ai1.propro25.ai.TrueRandomOpponent;
import tuda.ai1.propro25.fen.FENFormatException;
import tuda.ai1.propro25.model.Board;
import tuda.ai1.propro25.model.Color;
import tuda.ai1.propro25.model.Player;
import tuda.ai1.propro25.view.GUIManager;
import tuda.ai1.propro25.view.util.ErrorDialog;
import tuda.ai1.propro25.view.util.PieceImageLoader;

/**
 * Controller für das "Spiel erstellen"-Menü in der JavaFX-Schachanwendung.
 * <p>
 * Ermöglicht die Konfiguration eines neuen Spiels, einschließlich Auswahl der
 * Spielerfarbe, Zeitkontrolle (Minuten und Inkrement), Startposition (FEN)
 * sowie Auswahl des Gegners (lokaler Spieler oder verschiedene KI-Gegner).
 * </p>
 * <p>
 * Nach Bestätigung wird das Spiel mit den gewählten Einstellungen gestartet.
 * </p>
 */
public class CreateGameMenuController {

	@FXML
	private Slider minuteSlider;
	@FXML
	private Slider incrementSlider;
	@FXML
	private Label minuteLabel;
	@FXML
	private Label incrementLabel;

	@FXML
	private ImageView iconWhite;
	@FXML
	private ImageView iconBlack;

	@FXML
	private TextField fenTextField;

	@FXML
	private ComboBox<OpponentLevel> comboBox;

	private GUIManager guiManager;
	private Stage stage;

	@FXML
	private StackPane root;

	public enum OpponentLevel {
		LOCAL("Gegen Spieler (Lokal)"), AI_RANDOM("KI (Zufallszug)"), AI_EASY("KI (schwach)"), AI_MEDIUM(
				"KI (mittel)"), AI_HARD("KI (stark)");

		private final String displayName;

		OpponentLevel(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String toString() {
			return displayName;
		}
	}

	private OpponentLevel selectedOpponent;

	/**
	 * Setzt den GUI-Kontext, bestehend aus GUIManager und zugehörigem
	 * Stage-Fenster.
	 *
	 * @param guiManager
	 *            Die zentrale Verwaltungsinstanz für die GUI.
	 * @param stage
	 *            Das aktuelle JavaFX-Fenster.
	 */
	public void setContext(GUIManager guiManager, Stage stage) {
		this.guiManager = guiManager;
		this.stage = stage;

		initializeScene();
	}

	/**
	 * Initialisiert die Layoutgrößen und Eventhandler für das Menü. Wird
	 * aufgerufen, nachdem der Kontext gesetzt wurde.
	 */
	private void initializeScene() {
		// Constrain the menu pane (not just the root)
		BorderPane menu = (BorderPane) root.lookup("#menuBorderPane");

		Platform.runLater(() -> {

			double stageHeight = stage.getHeight() > 0 ? stage.getHeight() : 720;
			double width = stageHeight / 2;
			menu.setPrefWidth(width);
			menu.setMinSize(400, 500);
			menu.setMaxWidth(width);
			StackPane.setAlignment(menu, Pos.CENTER);

			menu.setPrefHeight(Region.USE_COMPUTED_SIZE);
			menu.setMinHeight(Region.USE_COMPUTED_SIZE);
			menu.setMaxHeight(Region.USE_COMPUTED_SIZE);

			comboBox.setPrefWidth(width);
		});
		root.setOnMouseClicked(event -> {
			// Only go back if click is outside the menu
			if (!menu.localToScene(menu.getBoundsInLocal()).contains(event.getSceneX(), event.getSceneY())) {
				guiManager.showStartMenu(stage); // or whatever method loads your start screen
			}
		});
	}

	/**
	 * Initialisiert die Controller-Komponenten, lädt Bildressourcen und setzt
	 * Listener für Schieberegler und Auswahlfelder.
	 */
	@FXML
	private void initialize() {
		minuteSlider.valueProperty()
				.addListener((obs, oldVal, newVal) -> minuteLabel.setText("Minuten pro Seite: " + newVal.intValue()));
		incrementSlider.valueProperty().addListener(
				(obs, oldVal, newVal) -> incrementLabel.setText("Inkrement in Sekunden: " + newVal.intValue()));

		comboBox.setItems(FXCollections.observableArrayList(OpponentLevel.values()));
		comboBox.setValue(OpponentLevel.LOCAL);
		selectedOpponent = OpponentLevel.LOCAL;

		Platform.runLater(this::loadPieceImages);
	}

	/**
	 * Lädt die Spielfigurenbilder im Hintergrund und aktualisiert anschließend die
	 * Icons.
	 */
	private void loadPieceImages() {
		// Start loading all piece images in the background
		Thread imageLoaderThread = new Thread(() -> {
			// Preload the images for all pieces
			PieceImageLoader.get('N'); // Preload White Knight
			PieceImageLoader.get('n'); // Preload Black Knight
			PieceImageLoader.get('P'); // Preload other pieces as needed...
			PieceImageLoader.get('p');
			PieceImageLoader.get('B');
			PieceImageLoader.get('b');
			PieceImageLoader.get('R');
			PieceImageLoader.get('r');
			PieceImageLoader.get('Q');
			PieceImageLoader.get('q');
			PieceImageLoader.get('K');
			PieceImageLoader.get('k');

			// Once images are loaded, update the icons for white and black pieces
			Platform.runLater(this::updateIcons);
		});
		imageLoaderThread.setDaemon(true); // Make sure the thread won't block application exit
		imageLoaderThread.start();
	}

	/**
	 * Aktualisiert die Icons für Weiß und Schwarz, sobald die Bilder geladen sind.
	 */
	private void updateIcons() {
		// Update the icon images once they're loaded
		Image img = PieceImageLoader.get('N'); // White Knight image
		iconWhite.setImage(img);
		img = PieceImageLoader.get('n'); // Black Knight image
		iconBlack.setImage(img);
	}

	/**
	 * Startet ein neues Spiel, bei dem der menschliche Spieler die weißen Figuren
	 * übernimmt.
	 */
	@FXML
	private void handleWhite() {
		setupGame(Color.WHITE);
	}

	/**
	 * Startet ein neues Spiel, bei dem der menschliche Spieler die schwarzen
	 * Figuren übernimmt.
	 */
	@FXML
	private void handleBlack() {
		setupGame(Color.BLACK);
	}

	/**
	 * Erstellt die Spielinstanzen (menschlicher Spieler, Gegner), liest die
	 * FEN-Zeichenkette und startet das Spiel über den GUIManager.
	 *
	 * @param humanColor
	 *            Die Farbe des menschlichen Spielers (Weiß oder Schwarz).
	 */
	private void setupGame(Color humanColor) {
		int time = (int) minuteSlider.getValue() * 60000;

		Player[] players = createPlayers(humanColor, time);

		Board board;
		if (fenTextField.getText().isBlank()) {
			// kein FEN angegeben, einfach Standardboard erstellen
			board = new Board(players);
		} else {
			try {
				board = new Board(fenTextField.getText(), players);
			} catch (FENFormatException e) {
				ErrorDialog.show("Der eingegebene FEN-String ist ungültig: " + e.getMessage());
				return;
			}
		}

		int increment = (int) incrementSlider.getValue() * 1000;
		guiManager.setUpBoard(board, increment);
	}

	private Player[] createPlayers(Color humanColor, int time) {
		Player humanPlayer = new Player(humanColor == Color.WHITE ? "Weiß" : "Schwarz", humanColor, time);
		Color aiColor = humanColor == Color.WHITE ? Color.BLACK : Color.WHITE;
		String aiName = aiColor == Color.WHITE ? "Weiß" : "Schwarz";

		Player opponent = switch (selectedOpponent) {
			case LOCAL -> new Player(aiName, aiColor, time);
			case AI_EASY -> new SearchAI(aiName, aiColor, time, 2);
			case AI_MEDIUM -> new PruningAI(aiName, aiColor, time, 3);
			case AI_HARD -> new DeepeningAI(aiName, aiColor, time, 4);
			case AI_RANDOM -> new TrueRandomOpponent(aiName, aiColor, time);
		};

		return humanColor == Color.WHITE ? new Player[]{humanPlayer, opponent} : new Player[]{opponent, humanPlayer};
	}

	/**
	 * Wird aufgerufen, wenn der Nutzer einen neuen Gegnertyp aus dem Drop-down-Menü
	 * auswählt. Speichert die Auswahl in {@code selectedOpponent}.
	 */
	@FXML
	private void handleSelection() {
		selectedOpponent = comboBox.getValue();
	}

	/**
	 * Kehre zum Startmenü zurück.
	 */
	public void onBack() {
		guiManager.showStartMenu(stage);
	}
}
