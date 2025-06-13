/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.view.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tuda.ai1.propro25.view.GUIManager;

/**
 * Controller für das Credits-Menü der Schach-Anwendung.
 * <p>
 * Dieses Menü zeigt Informationen über das Entwicklerteam oder andere
 * Mitwirkende an. Es wird zentriert als Overlay über der aktuellen Szene
 * angezeigt. Bei Klick außerhalb des Menüs wird zurück zum Startmenü
 * gewechselt.
 * </p>
 *
 * @author TUDA AI1
 * @version ProPro 2025
 */
public class CreditsMenuController {

	/**
	 * Der Root-StackPane dieser Szene (definiert in der zugehörigen FXML-Datei).
	 */
	@FXML
	private StackPane root;

	/** Referenz auf den GUI-Manager zum Szenenwechsel. */
	private GUIManager guiManager;

	/** Referenz auf das aktuelle Stage-Fenster. */
	private Stage stage;

	/**
	 * Setzt den Kontext dieser Szene.
	 *
	 * @param guiManager
	 *            Der GUI-Manager zur Steuerung des Interfaces
	 * @param stage
	 *            Das aktuelle JavaFX-Stage
	 */
	public void setContext(GUIManager guiManager, Stage stage) {
		this.guiManager = guiManager;
		this.stage = stage;

		initializeScene();
	}

	/**
	 * Initialisiert die Platzierung und Größe des Menüs im Fenster. Zentriert das
	 * Menü relativ zur Fenstergröße und setzt das Verhalten beim Klick außerhalb.
	 */
	private void initializeScene() {
		// Constrain the menu pane (not just the root)
		BorderPane menu = (BorderPane) root.lookup("#menuBorderPane");

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
				guiManager.showStartMenu(stage);
			}
		});
	}

	/**
	 * Handler für den "Zurück"-Button in der Credits-Szene.
	 */
	public void onBack() {
		guiManager.showStartMenu(stage);
	}
}
