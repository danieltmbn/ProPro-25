/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.view.controller;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import tuda.ai1.propro25.model.history.HistoricalGame;
import tuda.ai1.propro25.pgn.PGNExporter;
import tuda.ai1.propro25.view.util.ErrorDialog;

public class ExportViewController {

	private final HistoricalGame game;

	@FXML
	private TextField fenOutput;
	@FXML
	private Button fenCopyButton;
	@FXML
	private TextArea pgnOutput;
	@FXML
	private Button pgnCopyButton;
	@FXML
	private Button pgnSaveButton;

	private String fen;
	private String pgn;

	public ExportViewController(HistoricalGame game) {
		this.game = game;
	}

	// wird automatisch von JavaFX aufgerufen
	@FXML
	private void initialize() {
		try {
			fen = game.board().exportToFEN();
		} catch (RuntimeException e) {
			fenOutput.setText(e.getClass().getSimpleName() + ": " + e.getMessage());
			fenCopyButton.setDisable(true);
			return;
		}

		fenOutput.setText(fen);

		try {
			StringWriter writer = new StringWriter();
			new PGNExporter(writer).exportGame(game);
			pgn = writer.toString();
		} catch (RuntimeException e) {
			pgnOutput.setText(e.getClass() + ": " + e.getMessage());
			pgnCopyButton.setDisable(true);
			pgnSaveButton.setDisable(true);
			return;
		}

		pgnOutput.setText(pgn);
	}

	@FXML
	private void onFENCopyClicked() {
		copyButtonFeedback(fen, fenCopyButton);
	}

	@FXML
	private void onPGNCopyClicked() {
		copyButtonFeedback(pgn, pgnCopyButton);
	}

	private void copyButtonFeedback(String copyString, Button button) {
		if (copyString != null) {
			copyToClipboard(copyString);
			button.setText("Kopiert ✓");
			button.setDisable(true);
			PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
			pause.setOnFinished(e -> {
				button.setText("Kopieren");
				button.setDisable(false);
			});
			pause.play();
		}
	}

	@FXML
	private void onPGNSavedClicked() {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Speicherort der PGN Datei festlegen");
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PGN Files", "*.pgn"));

		File file = chooser.showSaveDialog(null);
		if (file == null) {
			return;
		}

		try {
			Files.writeString(file.toPath(), pgn, StandardOpenOption.CREATE);
		} catch (IOException e) {
			ErrorDialog.show("Fehler beim Speichern: " + e.getMessage());
		}
	}

	/**
	 * Kopiert den Strings in die System-Zwischenablage
	 * 
	 * @param string
	 *            der zu kopierende String
	 */
	private void copyToClipboard(String string) {
		ClipboardContent content = new ClipboardContent();
		content.putString(string);
		Clipboard.getSystemClipboard().setContent(content);
	}

}
