/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.view.controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Controller für den Endbildschirm der Schach-Anwendung.
 * <p>
 * Der Endbildschirm zeigt das Ergebnis eines Spiels (z. B. "Weiß gewinnt") an
 * und blendet sich mit einer Fade-Animation ein. Der Nutzer kann über einen
 * Button zurück ins Hauptmenü gelangen.
 * </p>
 *
 * <p>
 * Die Sichtbarkeit und das Verhalten des Endbildschirms werden durch diesen
 * Controller gesteuert. Ein Rückkehr-Callback wird über
 * {@link #setOnBackToMenu(Runnable)} gesetzt.
 * </p>
 *
 * @author TUDA AI1
 * @version ProPro 2025
 */
public class EndScreenController {

	public Button closeOverlayButton;
	/** Die Wurzel des Endbildschirms, meist über ein StackPane dargestellt. */
	@FXML
	private StackPane endScreenRoot;

	/**
	 * Label zur Anzeige des Ergebnistextes (z. B. "Remis" oder "Schwarz gewinnt").
	 */
	@FXML
	private Label resultLabel;

	/** Button zum Zurückkehren ins Hauptmenü. */
	@FXML
	private Button backToMenuButton;

	/** Callback, der beim Klick auf den Zurück-Button ausgeführt wird. */
	private Runnable onBackToMenu;

	/**
	 * Wird nach dem Laden der FXML-Datei automatisch von JavaFX aufgerufen.
	 * Initialisiert das Verhalten des Zurück-Buttons inklusive Ausblenden und
	 * Rückruf.
	 */
	@FXML
	public void initialize() {
		backToMenuButton.setOnAction(e -> {
			fadeOut();
			if (onBackToMenu != null) {
				onBackToMenu.run();
			}
		});
		closeOverlayButton.setOnAction(e -> {
			fadeOut();
		});
	}

	/**
	 * Zeigt den Endbildschirm an und blendet ihn mit einer Fade-Animation ein.
	 *
	 * @param winnerText
	 *            Der anzuzeigende Ergebnistext
	 */
	public void show(String winnerText) {
		resultLabel.setText(winnerText);
		endScreenRoot.setVisible(true);
		FadeTransition fadeIn = new FadeTransition(Duration.millis(800), endScreenRoot);
		fadeIn.setFromValue(0.0);
		fadeIn.setToValue(1.0);
		fadeIn.play();
	}

	/**
	 * Blendet den Endbildschirm mit einer Fade-Animation aus und versteckt ihn
	 * danach.
	 */
	public void fadeOut() {
		FadeTransition fadeOut = new FadeTransition(Duration.millis(500), endScreenRoot);
		fadeOut.setFromValue(1.0);
		fadeOut.setToValue(0.0);
		fadeOut.setOnFinished(e -> endScreenRoot.setVisible(false));
		fadeOut.play();
	}

	/**
	 * Setzt den Callback, der beim Klick auf den Zurück-Button ausgeführt wird.
	 *
	 * @param callback
	 *            Runnable mit dem Verhalten beim Zurückkehren zum Menü
	 */
	public void setOnBackToMenu(Runnable callback) {
		this.onBackToMenu = callback;
	}
}