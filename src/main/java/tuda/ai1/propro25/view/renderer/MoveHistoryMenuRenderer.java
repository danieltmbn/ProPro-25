/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.view.renderer;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import tuda.ai1.propro25.model.Color;

/**
 * Die Klasse {@code MoveHistoryRenderer} ist verantwortlich für die Darstellung
 * des Zugverlaufs (Move History) in einem Schachspiel.
 *
 * <p>
 * Die Züge werden in einem zweispaltigen Format angezeigt, wobei jede Zeile
 * einen vollständigen Spielzug darstellt (weiß und schwarz). Zusätzlich wird
 * die Zugnummer in einer eigenen Spalte angezeigt.
 * </p>
 */
public class MoveHistoryMenuRenderer {
	private final VBox container;
	private final GridPane moveGrid;
	private int moveIndex = 0;
	private boolean isWhiteTurn = true;
	private ScrollPane scrollPane;
	private final Button undoButton;
	private final List<Node[]> moveRows = new ArrayList<>();

	/**
	 * Konstruktor für {@code MoveHistoryRenderer}.
	 * <p>
	 * Initialisiert das Layout für die Anzeige des Zugverlaufs inklusive
	 * Überschrift und Scrollfunktion.
	 */
	public MoveHistoryMenuRenderer(Button exportButton) {
		moveGrid = new GridPane();
		moveGrid.setVgap(5);
		moveGrid.setHgap(5);
		moveGrid.setPadding(new Insets(10));

		moveGrid.heightProperty().addListener((obs, oldVal, newVal) -> scrollPane.setVvalue(1.0));

		// Set column widths to ensure longer move strings don't rescale the ui
		ColumnConstraints colNumber = new ColumnConstraints(25); // Move number
		ColumnConstraints colWhite = new ColumnConstraints(60); // White move
		ColumnConstraints colBlack = new ColumnConstraints(60); // Black move
		moveGrid.getColumnConstraints().addAll(colNumber, colWhite, colBlack);

		this.scrollPane = new ScrollPane(moveGrid);
		setScrollPaneProperties(scrollPane);

		Label title = new Label("Zugübersicht");
		title.setStyle("-fx-text-fill: white;");
		title.setFont(Font.font("System", FontWeight.BOLD, 16));
		title.setMaxWidth(Double.MAX_VALUE);
		title.setAlignment(Pos.CENTER);

		undoButton = new Button("Rückgängig");
		setUndoButtonProperties(undoButton);

		VBox.setMargin(undoButton, new Insets(10, 0, 0, 0)); // Add spacing from scroll

		container = new VBox(5, title, scrollPane, undoButton, exportButton);
		container.setPadding(new Insets(8));
		container.setStyle("-fx-border-color: #bfa76f;" + "-fx-border-width: 2;" + "-fx-border-radius: 8;"
				+ "-fx-background-color: linear-gradient(to bottom, #2e2e2e, #1c1c1c);" + "-fx-background-radius: 10px;"
				+ "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.25), 20, 0.2, 0, 4);");
	}

	/**
	 * Konfiguriert die Darstellungseigenschaften eines {@link ScrollPane} für die
	 * Move History.
	 * <p>
	 * Diese Methode setzt grundlegende Layout- und Stileigenschaften:
	 * </p>
	 * - Passt die Breite automatisch an den Inhalt an - Deaktiviert den
	 * horizontalen Scrollbalken - Aktiviert permanent den vertikalen Scrollbalken -
	 * Setzt eine feste Viewport-Höhe - Verwendet einen transparenten
	 * Hintergrundstil - Reduziert die Breite des vertikalen Scrollbalkens nach dem
	 * Laden des Skins
	 *
	 * @param scrollPane
	 *            Das {@link ScrollPane}-Objekt, dessen Eigenschaften gesetzt werden
	 *            sollen
	 */
	public void setScrollPaneProperties(ScrollPane scrollPane) {

		scrollPane.setFitToWidth(true);
		scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Disable horizontal scroll
		scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS); // Always show vertical scroll
		scrollPane.setPrefViewportHeight(200);

		// make scroll‐pane background semi‐transparent using the dark color
		scrollPane.setStyle(
				"-fx-background: transparent; " + "-fx-background-color: rgba(168,128,88,0.0); " + "-fx-padding: 5;");

		// Delay styling to ensure skin is applied
		scrollPane.skinProperty().addListener((obs, oldSkin, newSkin) -> {
			Node vBar = scrollPane.lookup(".scroll-bar:vertical");
			if (vBar != null) {
				vBar.setStyle("""
						-fx-background-color: transparent;-fx-pref-width: 10px;""");
			}
		});
	}

	/**
	 * Setzt Standarddarstellung und Verhalten für den Undo-Button.
	 * <p>
	 * Die Methode deaktiviert den Button initial, setzt eine Mindestbreite und
	 * wendet abgerundete Ecken an. Außerdem wird der Fokusrahmen beim Navigieren
	 * per Tab deaktiviert.
	 * </p>
	 */
	public void setUndoButtonProperties(Button button) {
		undoButton.setDisable(true);
		button.getStyleClass().add("main-button");
		button.setMaxHeight(20);
	}

	/**
	 * Gibt die grafische Darstellung des Zugverlaufs zurück.
	 *
	 * @return Eine {@link Node}-Instanz (VBox), die den Zugverlauf anzeigt
	 */
	public Node getView() {
		return container;
	}

	/**
	 *
	 * @return undo Button
	 */
	public Button getUndoButton() {
		return undoButton;
	}

	/**
	 * Fügt einen neuen Zug zur Darstellung hinzu.
	 * <p>
	 * Jeder vollständige Spielzug (weißer und schwarzer Zug) wird in einer eigenen
	 * Zeile dargestellt. Der weiße Zug steht in Spalte 1, der schwarze in Spalte 2.
	 * Die Zugnummer wird nur bei weißen Zügen links in Spalte 0 angezeigt.
	 * </p>
	 *
	 * @param move
	 *            Der Schachzug im String-Format (z.B. "e4", "f6" usw.)
	 */
	public void addMove(String move) {
		Text moveText = new Text(move);

		if (moveIndex == -1)
			moveIndex = 0;

		moveText.setStyle("-fx-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
		if (isWhiteTurn) {
			Text moveNumber = new Text((moveIndex + 1) + ".");
			moveNumber.setStyle("-fx-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

			moveGrid.add(moveNumber, 0, moveIndex);
			moveGrid.add(moveText, 1, moveIndex);

			moveRows.add(new Node[]{moveNumber, moveText, null});
			isWhiteTurn = false;
		} else {
			moveGrid.add(moveText, 2, moveIndex);

			Node[] row = moveRows.get(moveIndex);
			moveRows.set(moveIndex, new Node[]{row[0], row[1], moveText});
			isWhiteTurn = true;
			moveIndex++;
		}

		scrollPane.setVvalue(1.0);
	}

	/**
	 * Entfernt den zuletzt hinzugefügten Zug aus der Zugliste.
	 * <p>
	 * Die Methode unterscheidet je nach Farbe des Spielers:
	 * </p>
	 * Wenn der Zug von Schwarz war, wird nur das schwarze Zugfeld der letzten Zeile
	 * entfernt, da ein schwarzer Zug immer zu einem bereits vorhandenen weißen Zug
	 * gehört. Wenn der Zug von Weiß war, wird die gesamte Zeile (einschließlich
	 * Zugnummer, weißem und eventuell vorhandenem schwarzen Zug) entfernt, da ein
	 * weißer Zug immer eine neue Zeile beginnt. Die Methode prüft auf gültige
	 * Indizes und aktualisiert sowohl die interne Datenstruktur {@code moveRows}
	 * als auch das visuelle Grid {@code moveGrid}, um die Anzeige korrekt
	 * anzupassen.
	 *
	 * @param color
	 *            Die Farbe des Spielers, dessen letzter Zug entfernt werden soll.
	 *            Muss {@link tuda.ai1.propro25.model.Color#WHITE} oder
	 *            {@link tuda.ai1.propro25.model.Color#BLACK} sein.
	 */
	public void removeMove(Color color) {
		if (moveRows.isEmpty())
			return;

		if (color == Color.BLACK) {
			// If black move is to be removed, it was the last move added, so index is
			// (moveIndex - 1)
			int index = moveIndex - 1;
			if (index >= 0 && index < moveRows.size()) {
				Node[] row = moveRows.get(index);
				if (row[2] != null) {
					moveGrid.getChildren().remove(row[2]);
					// Update the entry in moveRows to reflect the black move is gone
					moveRows.set(index, new Node[]{row[0], row[1], null});
					isWhiteTurn = false;
					moveIndex--; // Because black move was undone
				}
			}
		} else if (color == Color.WHITE) {
			// Remove whole row for white move
			if (isWhiteTurn && moveIndex > 0) {
				moveIndex--;
			}
			if (moveIndex >= 0 && moveIndex < moveRows.size()) {
				Node[] row = moveRows.remove(moveIndex);
				if (row[0] != null)
					moveGrid.getChildren().remove(row[0]);
				if (row[1] != null)
					moveGrid.getChildren().remove(row[1]);
				if (row[2] != null)
					moveGrid.getChildren().remove(row[2]);
				isWhiteTurn = true;
			}
		}

		scrollPane.setVvalue(1.0);
	}

}
