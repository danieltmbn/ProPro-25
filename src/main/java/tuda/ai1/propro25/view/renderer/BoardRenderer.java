/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.view.renderer;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import tuda.ai1.propro25.model.Board;
import tuda.ai1.propro25.model.piece.Piece;
import tuda.ai1.propro25.view.util.PieceImageLoader;

/**
 * Diese Klasse ist verantwortlich für das Rendern des Schachbretts und der
 * Schachfiguren auf einem {@link Canvas}. Sie kümmert sich um das Zeichnen der
 * Schachbrettfelder, Koordinaten, Umrandung sowie das Zeichnen der
 * Schachfiguren basierend auf dem aktuellen Zustand des Schachbretts. Die
 * Klasse enthält Methoden, um das Schachbrett zu zeichnen, die Layout-Parameter
 * zu berechnen und das Schachbrett zu aktualisieren, wenn sich der Zustand der
 * Schachfiguren ändert.
 */
public class BoardRenderer {

	public enum HighlightType {
		NONE, CIRCLE, FULL, CAPTURE
	}
	private final int BOARD_SIZE;

	private final Canvas canvas; // Die Zeichenfläche für das Schachbrett
	private final GraphicsContext gc; // Grafik-Kontext

	private int squareSize; // Größe eines Feldes in Pixeln
	private int boardOffsetX; // Offset für die X-Achse
	private int boardOffsetY; // Offset für die Y-Achse

	private boolean flipped = false; // Flag für die Drehung des Schachbretts

	private final HighlightType[][] highlightLayer;

	private final Image squareImage_black;
	private final Image squareImage_white;

	private Color lightColor;
	private Color darkColor;
	/**
	 * Konstruktor für die {@link BoardRenderer} Klasse.
	 * <p>
	 * Dieser Konstruktor initialisiert ein neues Schachbrett-Renderer-Objekt und
	 * erstellt die Canvas sowie den zugehörigen Grafik-Context
	 *
	 */
	public BoardRenderer() {
		this.BOARD_SIZE = Board.BOARD_SIZE;
		this.canvas = new Canvas();
		this.gc = canvas.getGraphicsContext2D();

		this.highlightLayer = new HighlightType[BOARD_SIZE][BOARD_SIZE];
		for (int x = 0; x < BOARD_SIZE; x++) {
			for (int y = 0; y < BOARD_SIZE; y++) {
				highlightLayer[x][y] = HighlightType.NONE;
			}
		}

		var stream = this.getClass().getResourceAsStream("/square_black.png");
		if (stream == null) {
			throw new IllegalStateException("Ressource square_black.png nicht gefunden");
		}
		squareImage_black = new Image(stream);
		stream = this.getClass().getResourceAsStream("/square_white.png");
		if (stream == null) {
			throw new IllegalStateException("Ressource square_white.png nicht gefunden");
		}
		squareImage_white = new Image(stream);

		canvas.setFocusTraversable(true);
		canvas.setStyle("-fx-background-color: rgba(140,28,28,0.5);");
	}
	/**
	 * Zeichnet das Schachbrett und alle zugehörigen Elemente:
	 * <ul>
	 * <li>Leert das Canvas</li>
	 * <li>Berechnet die Layout-Parameter des Schachbretts</li>
	 * <li>Zeichnet die Schachbrettfelder</li>
	 * <li>Zeichnet die Koordinaten (Buchstaben und Zahlen)</li>
	 * <li>Zeichnet die Umrandung des Schachbretts</li>
	 * <li>Zeichnet die Schachfiguren basierend auf dem aktuellen Zustand des
	 * Schachbretts</li>
	 * </ul>
	 */
	public void redrawBoard(Piece[][] board) {
		clearCanvas();
		calcBoardLayout();
		drawSquares();
		drawOutline();
		drawCoordinates();
		drawPieces(board);
	}

	/**
	 * Setzt Farbe des hellen Feldes
	 * 
	 * @param selectedColor
	 */
	public void setLightFieldColor(Color selectedColor) {
		this.lightColor = selectedColor;
	}

	/**
	 * Setzt Farbe des dunklen Feldes
	 * 
	 * @param selectedColor
	 */
	public void setDarkFieldColor(Color selectedColor) {
		this.darkColor = selectedColor;
	}

	/**
	 * Leert den Canvas (die Zeichenfläche). Dies ist notwendig vor jeder neuen
	 * Zeichnung des Schachbretts
	 */
	private void clearCanvas() {
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}

	/**
	 * Negiert den wert von {@code flipped}.
	 * <p>
	 * Wenn {@code flipped} auf {@code true} gesetzt ist, wird das Schachbrett
	 * umgekehrt dargestellt. Zudem wird die Nummerierung und Buchstabierung der
	 * Felder von der Perspektive des Spielers umgekehrt.
	 * </p>
	 */
	public void flipFlipped() {
		flipped = !flipped;
	}

	/**
	 * Berechnet die Layout-Parameter für das Schachbrett, insbesondere die Größe
	 * der Felder und Offset-Werte für die Position des Schachbretts auf dem Canvas.
	 * Die Berechnungen stellen sicher, dass das Schachbrett innerhalb der
	 * verfügbaren
	 *
	 * @see #squareSize
	 * @see #boardOffsetX
	 *
	 * @see #boardOffsetY
	 */
	private void calcBoardLayout() {
		double width = canvas.getWidth();
		double height = canvas.getHeight();
		double scale = 0.9;

		int boardPixelSize = (int) (Math.min(width, height) * scale);
		this.squareSize = boardPixelSize / BOARD_SIZE;

		// Berechne das Offset, um das Schachbrett zu zentrieren
		this.boardOffsetX = ((int) width - this.squareSize * BOARD_SIZE) / 2;
		this.boardOffsetY = ((int) height - this.squareSize * BOARD_SIZE) / 2;
	}

	/**
	 * Zeichnet die Schachbrettfelder auf das Canvas. Die Felder werden in einem
	 * Schachbrettmuster abwechselnd in Weiß und Dunkelgrau gefärbt. Jedes Feld hat
	 * eine Größe, die durch die Variable {@code squareSize} bestimmt wird. Die
	 * Positionen der Felder werden durch die Variablen {@code boardOffsetX} und
	 * {@code boardOffsetY} beeinflusst, um das Schachbrett an der richtigen Stelle
	 * auf dem Canvas zu platzieren.
	 *
	 * @see #squareSize
	 * @see #boardOffsetX
	 * @see #boardOffsetY
	 */
	private void drawSquares() {
		for (int x = 0; x < BOARD_SIZE; x++) {
			for (int y = BOARD_SIZE - 1; y >= 0; y--) {
				// Calculate the x and y positions for the square
				drawSquare(x, y);
			}
		}
	}

	/**
	 * Zeichnet ein Schachbrettfeld auf das Canvas. Jedes Feld hat eine Größe, die
	 * durch die Variable {@code squareSize} bestimmt wird. Die Positionen der
	 * Felder werden durch die Variablen {@code boardOffsetX} und
	 * {@code boardOffsetY} beeinflusst, um das Schachbrett an der richtigen Stelle
	 * auf dem Canvas zu platzieren.
	 *
	 * @see #squareSize
	 * @see #boardOffsetX
	 * @see #boardOffsetY
	 */
	private void drawSquare(int x, int y) {
		// Calculate the x and y positions for the square
		var px = toPixels(x, y);
		double squareX = px[0];
		double squareY = px[1];
		Color highlightColor = new Color(0, 1, 0, 0.4);

		if (lightColor != null && darkColor != null) {
			gc.setFill((x + y) % 2 != 0 ? lightColor : darkColor);
			gc.fillRect(squareX, squareY, squareSize, squareSize);
		} else if (lightColor != null) {
			gc.setFill(lightColor);
			if ((x + y) % 2 != 0) {
				gc.fillRect(squareX, squareY, squareSize, squareSize);
			} else {
				gc.drawImage(squareImage_black, squareX, squareY, squareSize, squareSize);
			}
		} else if (darkColor != null) {
			gc.setFill(darkColor);
			if ((x + y) % 2 == 0) {
				gc.fillRect(squareX, squareY, squareSize, squareSize);
			} else {
				gc.drawImage(squareImage_white, squareX, squareY, squareSize, squareSize);
			}
		} else {
			if ((x + y) % 2 != 0) {
				gc.drawImage(squareImage_white, squareX, squareY, squareSize, squareSize);
			} else {
				gc.drawImage(squareImage_black, squareX, squareY, squareSize, squareSize);
			}
		}
		HighlightType highlight = highlightLayer[x][y];
		if (highlight == HighlightType.CIRCLE) {
			gc.setFill(highlightColor);
			double circlePadding = squareSize * 0.2;
			double circleSize = squareSize - 2 * circlePadding;
			gc.fillOval(squareX + circlePadding, squareY + circlePadding, circleSize, circleSize);
		} else if (highlight == HighlightType.FULL) {
			gc.setFill(highlightColor);
			gc.fillRect(squareX, squareY, squareSize, squareSize);
		} else if (highlight == HighlightType.CAPTURE) {
			gc.setStroke(highlightColor);
			gc.setLineWidth(8);
			double circlePadding = squareSize * 0.1;
			double circleSize = squareSize - 2 * circlePadding;
			gc.strokeOval(squareX + circlePadding, squareY + circlePadding, circleSize, circleSize);
		}

	}

	/**
	 * Zeichnet die Koordinaten (1-8 und a-h) am Rand des Schachbretts. Abhängig vom
	 * {@code flipped} Parameter entweder aufsteigend oder absteigend.
	 *
	 * @see #boardOffsetX
	 * @see #boardOffsetY
	 * @see #squareSize
	 * @see #flipped
	 */
	private void drawCoordinates() {
		gc.setFill(Color.BLACK);
		gc.setFont(Font.font(squareSize * 0.4));

		for (int i = 0; i < BOARD_SIZE; i++) {
			// Buchstaben a-h (bei flipped umgekehrt)
			char letter = (char) ('a' + (flipped ? (BOARD_SIZE - 1 - i) : i));
			double letterX = boardOffsetX + i * squareSize + squareSize * 0.35;
			double letterY = Math.floor(boardOffsetY + BOARD_SIZE * squareSize + squareSize * 0.35 + 3);
			gc.setFill(Color.web("#e0c68c"));
			gc.fillText(String.valueOf(letter), letterX, letterY);

			// Zahlen 1-8 (bei flipped vertauschte Reihenfolge)
			String number = String.valueOf(i + 1);
			double numberX = boardOffsetX - squareSize * 0.5;
			double numberY = boardOffsetY + (flipped ? i : (BOARD_SIZE - 1 - i)) * squareSize + squareSize * 0.7;
			gc.setFill(Color.web("#e0c68c"));
			gc.fillText(number, numberX, numberY);
		}
	}

	/**
	 * Zeichnet einen Rand um das Schachbrett.
	 */
	private void drawOutline() {
		double boardSize = squareSize * BOARD_SIZE;
		gc.setStroke(Color.BLACK);
		gc.setLineWidth(1);
		gc.strokeRect(boardOffsetX, boardOffsetY, boardSize, boardSize);
	}

	/**
	 * Zeichnet die Schachfiguren auf das Canvas.
	 *
	 * @param board
	 *            Das {@link Board}-Objekt, das den aktuellen Zustand des
	 *            Schachbretts enthält, einschließlich der Schachfiguren auf den
	 *            einzelnen Feldern.
	 */
	public void drawPieces(Piece[][] board) {
		for (int x = 0; x < BOARD_SIZE; x++) {
			for (int y = BOARD_SIZE - 1; y >= 0; y--) {
				Piece piece = board[x][y];
				if (piece != null) {
					drawPiece(piece.getFenSymbol(), x, y);
				}
			}
		}
	}

	/**
	 * Zeichnet eine Figur auf dem Brett.
	 *
	 * @param pieceSymbol
	 *            Das Fen-Symbol der Figur (z.B. 'P' für Bauer weiß, 'q' für Dame
	 *            schwarz).
	 * @param x
	 *            Die X-Koordinate der Figur auf dem Brett (in Feldern).
	 * @param y
	 *            Die Y-Koordinate der Figur auf dem Brett (in Feldern).
	 */
	public void drawPiece(char pieceSymbol, int x, int y) {
		// Flip the row and column indices based on the flipped state
		var px = toPixels(x, y);
		double squareX = px[0];
		double squareY = px[1];

		Image image = PieceImageLoader.get(pieceSymbol);

		gc.drawImage(image, squareX, squareY, squareSize, squareSize);
	}

	/**
	 * Gibt die Canvas zurück, auf der das Schachbrett gezeichnet wird.
	 */
	public Canvas getCanvas() {
		return canvas;
	}

	/**
	 * Übersetzt eine Klickposition in Bildschirmkoordinaten (x, y) in
	 * Brettkoordinaten.
	 *
	 * @param x
	 *            Die X-Position des Klicks (in Pixeln).
	 * @param y
	 *            Die Y-Position des Klicks (in Pixeln).
	 *
	 * @return Ein Array, das die Brettkoordinaten [Reihe, Spalte] enthält.
	 */
	public int[] translateClickToCoordinate(double x, double y) {
		double boardX = (x - boardOffsetX) / squareSize;
		double boardY = (y - boardOffsetY) / squareSize;
		int row = flipped ? (int) boardY : (BOARD_SIZE - 1 - ((int) boardY));
		int col = flipped ? (BOARD_SIZE - 1 - ((int) boardX)) : (int) boardX;

		return new int[]{col, row};
	}

	/**
	 * Übersetzt Brettkoordinaten (x, y) in Pixelpositionen auf der Zeichenfläche.
	 *
	 * @param x
	 *            Die X-Koordinate auf dem Brett.
	 * @param y
	 *            Die Y-Koordinate auf dem Brett.
	 *
	 * @return Ein Array mit den Pixel-Koordinaten [x, y].
	 */
	public double[] toPixels(int x, int y) {
		int displayX = flipped ? (BOARD_SIZE - 1 - x) : x;
		int displayY = flipped ? y : (BOARD_SIZE - 1 - y);

		double px = boardOffsetX + displayX * squareSize;
		double py = boardOffsetY + displayY * squareSize;

		return new double[]{px, py};
	}

	/**
	 * Markiert ein bestimmtes Feld auf dem Schachbrett.
	 *
	 * @param x
	 *            Die X-Koordinate des Feldes (in Feldern).
	 * @param y
	 *            Die Y-Koordinate des Feldes (in Feldern).
	 */
	public void highlightTile(HighlightType type, int x, int y) {
		highlightLayer[x][y] = type;
	}

	/**
	 * Löscht alle Markierungen auf dem Schachbrett.
	 */
	public void clearHighlights() {
		for (int x = 0; x < BOARD_SIZE; x++) {
			for (int y = 0; y < BOARD_SIZE; y++) {
				highlightLayer[x][y] = HighlightType.NONE;
			}
		}
	}

	public double getSquareSize() {
		return squareSize;
	}
}
