/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.view.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tuda.ai1.propro25.fen.FENRecord;
import tuda.ai1.propro25.model.Board;
import tuda.ai1.propro25.model.Color;
import tuda.ai1.propro25.pgn.*;
import tuda.ai1.propro25.pgn.move.PGNMove;
import tuda.ai1.propro25.view.GUIManager;
import tuda.ai1.propro25.view.renderer.MoveHistoryMenuRenderer;

public class StartMenuController {

	private GUIManager guiManager;
	private Stage stage;

	public void init(GUIManager guiManager, Stage stage) {
		this.guiManager = guiManager;
		this.stage = stage;
	}

	@FXML
	private void onStartGame() {
		guiManager.showCreateGameMenu(stage);
	}

	@FXML
	private void onSettings() {
		guiManager.showSettingsMenu(stage);
	}

	@FXML
	private void onExit() {
		stage.close();
	}

	@FXML
	public void onCredits() {
		guiManager.showCreditsMenu(stage);
	}

	/**
	 * Wird aufgerufen, wenn der Benutzer ein Spiel aus einer PGN-Datei laden
	 * möchte.
	 * <p>
	 * Öffnet einen Dateiauswahldialog, liest den Inhalt der gewählten Datei,
	 * rekonstruiert das Spiel über den {@link GameReconstructor} und übergibt das
	 * rekonstruierte Spielbrett an den {@link GUIManager}.
	 * </p>
	 * <p>
	 * Falls beim Laden der Datei oder bei der Rekonstruktion ein Fehler auftritt,
	 * wird ein entsprechender Fehlerdialog angezeigt.
	 * </p>
	 *
	 *
	 * @see FileChooser
	 * @see GameReconstructor
	 * @see GUIManager#setUpBoard
	 */
	public void onLoadGame() {
		try {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Spiel laden");
			File file = fileChooser.showOpenDialog(stage);
			if (file == null)
				return;

			String encodedPGN = Files.readString(file.toPath());
			PGNGame parsedGame = PGNParser.parse(encodedPGN);

			GameReconstructor gameReconstructor = new GameReconstructor(6000 * 10, 6000 * 10);
			Board reconstructedBoard = gameReconstructor.reconstructGame(parsedGame);

			guiManager.setUpBoard(reconstructedBoard, 0);

			fillMoveHistory(reconstructedBoard, parsedGame);
		} catch (IOException | GameReconstructionException | PGNParseException e) {
			e.printStackTrace();
			Alert alert = new Alert(Alert.AlertType.ERROR,
					"Etwas ist kaputt.\nPrüfe die Konsolenausgabe auf Fehlermeldungen!");
			alert.showAndWait();
		}
	}

	/**
	 * Fügt nachträglich alle rekonstruierten Züge in die UI ein
	 * 
	 * @param reconstructedBoard
	 *            das Brett mit dem rekonstruierten Spielstand
	 * @param parsedGame
	 *            das tatsächlich geparste Spiel
	 */
	private void fillMoveHistory(Board reconstructedBoard, PGNGame parsedGame) {
		MoveHistoryMenuRenderer moveRenderer = guiManager.getMoveHistoryRenderer();

		FENRecord startState = reconstructedBoard.getNonStandardStartState();
		if (startState != null && startState.activeColor() == Color.BLACK) {
			// Schwarz macht den ersten (aufgezeichneten) Move
			moveRenderer.addMove("..."); // dummy Move für Weiß
		}

		for (PGNMove move : parsedGame.moves()) {
			// toString produziert wieder die PGN-SAN darstellung
			moveRenderer.addMove(move.toString());
		}

		if (reconstructedBoard.getLastMove() != null) {
			// Es gab schon einen Move, Button darf aktiviert sein.
			guiManager.disableUndoButton(false);
		}
	}
}
