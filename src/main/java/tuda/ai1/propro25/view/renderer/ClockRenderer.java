/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.view.renderer;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import tuda.ai1.propro25.controller.GameController;
import tuda.ai1.propro25.model.Color;

/**
 * Die Klasse {@code ClockRenderer} ist verantwortlich für die Darstellung und
 * Verwaltung der beiden Schachuhren (Weiß und Schwarz) innerhalb der
 * Benutzeroberfläche.
 * <p>
 * Diese Klasse enthält eine Methode, um die Position der Uhren zu tauschen.
 */
public class ClockRenderer {
	private final VBox clockBox;
	private final Label whiteClockLabel;
	private final Label blackClockLabel;
	private FadeTransition whiteBlink;
	private FadeTransition blackBlink;
	private Timeline timer;
	private final GameController controller;
	private final MoveHistoryMenuRenderer moveHistoryRenderer;
	private Color currentlyHighlightedColor = null;

	private static final String WHITE_STYLE = "-fx-font-size: 18px; -fx-background-color: #e8c590;"
			+ " -fx-text-fill: black; -fx-alignment: center; -fx-background-radius: 10px;"
			+ " -fx-border-color: transparent; -fx-border-width: 3px; -fx-border-radius: 7px;";
	private static final String BLACK_STYLE = "-fx-font-size: 18px; -fx-background-color: #70401a;"
			+ " -fx-text-fill: rgb(255,255,255); -fx-alignment: center; -fx-background-radius: 10px;"
			+ " -fx-border-color: transparent; -fx-border-width: 3px; -fx-border-radius: 7px;";
	private static final String HIHGLIGHT_STYLE = "-fx-border-color: #6b2d0a; -fx-border-width: 3px; -fx-border-radius: 7px";

	/**
	 * Konstruktor für die {@code ClockRenderer}-Klasse mit Zugverlaufsanzeige und
	 * Spiel-Controller.
	 * <p>
	 * Initialisiert die Uhrenanzeige und startet den Timer, der die verbleibende
	 * Zeit der Spieler anhand des übergebenen Controllers aktualisiert.
	 * </p>
	 *
	 * @param moveHistoryMenuRenderer
	 *            Der Renderer für das Zugverlaufsmenü.
	 * @param controller
	 *            Der GameController, der den Spielzustand und die Spielerzeiten
	 *            verwaltet.
	 */
	public ClockRenderer(MoveHistoryMenuRenderer moveHistoryMenuRenderer, GameController controller) {
		this.moveHistoryRenderer = moveHistoryMenuRenderer;
		whiteClockLabel = new Label("White: --:--");
		blackClockLabel = new Label("Black: --:--");

		styleClockLabel(whiteClockLabel);
		styleClockLabel(blackClockLabel);

		clockBox = new VBox(10, blackClockLabel, moveHistoryMenuRenderer.getView(), whiteClockLabel);
		setClockBox(clockBox);

		// default styles
		whiteClockDefaultStyle(whiteClockLabel);
		blackClockDefaultStyle(blackClockLabel);

		// default highlighting at the beginning of a new game
		highlightActivePlayer(Color.WHITE);

		this.controller = controller;
	}

	/**
	 * Konfiguriert das Layout der VBox, welche die Uhranzeigen enthält.
	 *
	 * @param clockBox
	 *            Die VBox, die angepasst werden soll
	 */
	private void setClockBox(VBox clockBox) {

		clockBox.setAlignment(Pos.CENTER);
		clockBox.setPadding(new Insets(0, 37, 0, 0));
		clockBox.setSpacing(10);
		clockBox.setPrefWidth(250);
		clockBox.setMaxWidth(Double.MAX_VALUE);
		clockBox.setFillWidth(true);

		VBox.setVgrow(whiteClockLabel, Priority.ALWAYS);
		VBox.setVgrow(blackClockLabel, Priority.ALWAYS);

	}

	/**
	 * Stellt grundlegende Layout- und Styleeigenschaften für die Uhr-Labels ein.
	 *
	 * @param label
	 *            Das zu stylende Label
	 */
	private void styleClockLabel(Label label) {

		label.setMinHeight(50);
		label.setMaxWidth(Double.MAX_VALUE);

		label.setAlignment(Pos.CENTER);
		label.setPadding(new Insets(10));
		label.setWrapText(true);

	}

	/**
	 * Aktiviert die Blinkanimation für das weiße Uhr-Label.
	 *
	 * @param label
	 *            Das Label für die weiße Uhr
	 */
	private void whiteClockHighlightProperties(Label label) {
		// Blinking white -> white moves
		whiteBlink = new FadeTransition(Duration.seconds(1), label);
		whiteBlink.setFromValue(1.0);
		whiteBlink.setToValue(0.7);
		whiteBlink.setCycleCount(FadeTransition.INDEFINITE);
		whiteBlink.setAutoReverse(true);
		whiteBlink.play();
	}

	/**
	 * Aktiviert die Blinkanimation für das schwarze Uhr-Label.
	 *
	 * @param label
	 *            Das Label für die weiße Uhr
	 */
	private void blackClockHighlightProperties(Label label) {
		// Blinking black -> black moves
		blackBlink = new FadeTransition(Duration.seconds(1), label);
		blackBlink.setFromValue(1.0);
		blackBlink.setToValue(0.7);
		blackBlink.setCycleCount(FadeTransition.INDEFINITE);
		blackBlink.setAutoReverse(true);
		blackBlink.play();
	}

	/**
	 * Setzt den Standardstil für das weiße Uhr-Label.
	 *
	 * @param label
	 *            Das Label für die weiße Uhr
	 */
	private void whiteClockDefaultStyle(Label label) {
		label.setStyle(WHITE_STYLE);
	}

	/**
	 * Setzt den Standardstil für das schwarze Uhr-Label.
	 *
	 * @param label
	 *            Das Label für die schwarze Uhr
	 */
	private void blackClockDefaultStyle(Label label) {
		label.setStyle(BLACK_STYLE);
	}

	/**
	 * Setzt den Hervorhebungsstil für die weiße Uhr.
	 *
	 * @param label
	 *            Das Label für die weiße Uhr
	 */
	private void whiteClockHighlightStyle(Label label) {
		label.setStyle(label.getStyle() + HIHGLIGHT_STYLE);
	}

	/**
	 * Setzt den Hervorhebungsstil für die schwarze Uhr.
	 *
	 * @param label
	 *            Das Label für die schwarze Uhr
	 */
	private void blackClockHighlightStyle(Label label) {
		label.setStyle(label.getStyle() + HIHGLIGHT_STYLE);
	}

	/**
	 * Hebt die aktive Uhr hervor und startet eine Blinkanimation.
	 * <p>
	 * Die nicht aktive Uhr wird zurückgesetzt.
	 *
	 * @param color
	 *            Die Farbe des aktiven Spielers (Weiß oder Schwarz)
	 */
	public void highlightActivePlayer(Color color) {

		boolean isBlinking = (color == Color.WHITE && whiteBlink != null)
				|| (color == Color.BLACK && blackBlink != null);

		if (color == currentlyHighlightedColor && isBlinking) {
			return;
		}

		if (whiteBlink != null)
			whiteBlink.stop();
		if (blackBlink != null)
			blackBlink.stop();

		currentlyHighlightedColor = color;

		// Reset opacity for both
		whiteClockLabel.setOpacity(1.0);
		blackClockLabel.setOpacity(1.0);

		// board.getCurrentPlayer().getColor() == Color.WHITE
		if (color == Color.WHITE) {
			whiteClockHighlightStyle(whiteClockLabel);
			blackClockDefaultStyle(blackClockLabel);
			whiteClockHighlightProperties(whiteClockLabel);
		} else {
			whiteClockDefaultStyle(whiteClockLabel);
			blackClockHighlightStyle(blackClockLabel);
			blackClockHighlightProperties(blackClockLabel);
		}
	}

	/**
	 * Tauscht die Uhrzeiten zwischen den beiden Schachspielern (Weiß und Schwarz).
	 * Diese Methode vertauscht die angezeigten Zeiten der beiden Spieler. Die Uhr
	 * von Weiß wird mit der Uhr von Schwarz und umgekehrt getauscht. Die Methode
	 * stellt sicher, dass nur dann ein Tausch stattfindet, wenn beide Labels für
	 * die Uhren vorhanden sind.
	 *
	 * <p>
	 * Die Uhrzeiten werden als Text in den {@link Label}-Objekten angezeigt und
	 * vertauscht.
	 * </p>
	 */
	public void swapClocks() {
		ObservableList<Node> children = clockBox.getChildren();

		if (children.contains(whiteClockLabel) && children.contains(blackClockLabel)) {
			// Get their current positions
			int whiteIndex = children.indexOf(whiteClockLabel);
			int blackIndex = children.indexOf(blackClockLabel);

			// Remove both from VBox
			children.remove(whiteClockLabel);
			children.remove(blackClockLabel);

			// Swap positions
			if (whiteIndex < blackIndex) {
				// White was on top, now move black to top
				children.add(whiteIndex, blackClockLabel);
				children.add(blackIndex, whiteClockLabel);
			} else {
				// Black was on top, now move white to top
				children.add(blackIndex, whiteClockLabel);
				children.add(whiteIndex, blackClockLabel);
			}
		}
	}

	/**
	 * Gibt die Ansicht (VBox) zurück, die die Uhranzeigen für beide Spieler und
	 * Zugverlauf enthält.
	 *
	 * @return Die {@link VBox}-Instanz, die die Uhranzeigen für die Spieler
	 *         enthält.
	 */
	public VBox getView() {
		return clockBox;
	}

	/**
	 * Updates the clock label for the given player color with the specified time.
	 * The time is formatted as mm:ss.
	 *
	 * @param time
	 *            Remaining time in milliseconds.
	 * @param color
	 *            The color of the player (WHITE or BLACK).
	 */
	public void updateClockLabel(int time, Color color) {
		var timeInSeconds = time / 1000;
		if (color == Color.WHITE) {
			whiteClockLabel.setText("Weiß: " + timeInSeconds / 60 + ":" + String.format("%02d", timeInSeconds % 60));
		} else {
			blackClockLabel.setText("Schwarz: " + timeInSeconds / 60 + ":" + String.format("%02d", timeInSeconds % 60));
		}
	}

	public MoveHistoryMenuRenderer getMoveHistory() {
		return moveHistoryRenderer;
	}

	/**
	 * Startet den Timer, der jede Sekunde die verbleibende Zeit des aktuellen
	 * Spielers aktualisiert.
	 * <p>
	 * Diese Methode initialisiert eine {@link Timeline}, die im Sekundentakt
	 * ausgeführt wird. Bei jedem Tick wird die Methode
	 * {@code decPlayerTimeBy1Second()} des Controllers aufgerufen, um die
	 * verbleibende Zeit des aktuellen Spielers zu verringern. Anschließend wird das
	 * entsprechende Uhren-Label mit der neuen Zeit aktualisiert.
	 * </p>
	 * Die Timeline läuft unbegrenzt, bis sie explizit gestoppt wird.
	 */
	public void startTimer() {
		timer = new Timeline((new KeyFrame(Duration.seconds(1), event -> {
			controller.decPlayerTimeBy1Second();
			if (controller.getCurrentPlayer().getRemainingTime() > 0) {
				updateClockLabels();
			}
		})));
		timer.setCycleCount(Timeline.INDEFINITE);
		timer.play();
	}

	private void updateClockLabels() {
		updateClockLabel(controller.getCurrentPlayer().getRemainingTime(), controller.getCurrentPlayer().getColor());
		updateClockLabel(controller.getNextPlayer().getRemainingTime(), controller.getNextPlayer().getColor());
	}

	public void deleteTimer() {
		if (timer != null) {
			timer.stop();
			timer = null;
		}
		updateClockLabels();

		whiteClockLabel.setText(whiteClockLabel.getText() + " (⏸)");
		blackClockLabel.setText(blackClockLabel.getText() + " (⏸)");

	}
}
