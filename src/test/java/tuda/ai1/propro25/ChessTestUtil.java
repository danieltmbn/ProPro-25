/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import tuda.ai1.propro25.model.*;
import tuda.ai1.propro25.model.piece.*;

/**
 * Diese Klasse enthält einige Klassenmethoden, um das Erstellen von
 * Figurenanordnungen zu erleichtern.
 */
public class ChessTestUtil {

	// darf nicht instanziiert werden
	private ChessTestUtil() {
	}

	/**
	 * Erstellt ein 2D-Array, das das leere Brett repräsentiert. Das Brett ist
	 * {@link Board#BOARD_SIZE} x {@link Board#BOARD_SIZE} groß.
	 * 
	 * @return leeres Brett
	 */
	public static Piece[][] emptyBoard() {
		return emptyBoard(Board.BOARD_SIZE);
	}

	/**
	 * Erstellt ein 2D-Array, das das leere Brett repräsentiert. Das Brett ist size
	 * x size groß.
	 *
	 * @param size
	 *            die Größe des Bretts
	 * 
	 * @return leeres Brett
	 * @throws IllegalArgumentException
	 *             * falls size negativ ist
	 */
	public static Piece[][] emptyBoard(int size) {
		if (size < 0) {
			throw new IllegalArgumentException("Brettgröße darf nicht negativ sein");
		}
		return new Piece[size][size];
	}

	/**
	 * Erstellt ein 2D-Array für die initiale Figurenanordnung. Dieses Brett ist
	 * immer 8x8 groß.
	 *
	 * @return die initiale Figurenanordnung als 2D-Array
	 */
	public static Piece[][] initialBoard() {
		return createBoard(
				new Piece[]{new Rook(Color.WHITE), new Knight(Color.WHITE), new Bishop(Color.WHITE),
						new Queen(Color.WHITE), new King(Color.WHITE), new Bishop(Color.WHITE), new Knight(Color.WHITE),
						new Rook(Color.WHITE)},
				new Piece[]{new Pawn(Color.WHITE), new Pawn(Color.WHITE), new Pawn(Color.WHITE), new Pawn(Color.WHITE),
						new Pawn(Color.WHITE), new Pawn(Color.WHITE), new Pawn(Color.WHITE), new Pawn(Color.WHITE)},
				new Piece[8], new Piece[8], new Piece[8], new Piece[8],
				new Piece[]{new Pawn(Color.BLACK), new Pawn(Color.BLACK), new Pawn(Color.BLACK), new Pawn(Color.BLACK),
						new Pawn(Color.BLACK), new Pawn(Color.BLACK), new Pawn(Color.BLACK), new Pawn(Color.BLACK)},
				new Piece[]{new Rook(Color.BLACK), new Knight(Color.BLACK), new Bishop(Color.BLACK),
						new Queen(Color.BLACK), new King(Color.BLACK), new Bishop(Color.BLACK), new Knight(Color.BLACK),
						new Rook(Color.BLACK)});
	}

	/**
	 * Erstellt ein 2D-Array, das die Figurenanordnung darstellt, der die
	 * übergebenen Reihen enthält. Das zurückgegebene Array ist Spalten-basiert.
	 *
	 * @param rows
	 *            jeweils ein Piece-Array für jede Reihe des Bretts
	 * @return die aus den angegebenen Reihen bestehenden Figurenanordnung
	 * @throws IllegalArgumentException
	 *             falls rowBased leer ist, mind. eine Reihe null ist oder die
	 *             Matrix nicht quadratisch ist
	 */
	public static Piece[][] createBoard(Piece[]... rows) {
		return transposeBoard(rows);
	}

	/**
	 * Transponiert die Brettmatrix, sodass man Zielen-basierte in Spalten-basierte
	 * Brettmatrizen übersetzen kann.
	 * 
	 * @param rowBased
	 *            die quadratische, nicht leere Spalten-basierte Brettmatrix
	 * @return die Brettmatrix Spalten-basiert
	 * @throws IllegalArgumentException
	 *             falls rowBased leer ist, mind. eine Reihe null ist oder die
	 *             Matrix nicht quadratisch ist
	 */
	static Piece[][] transposeBoard(Piece[][] rowBased) {
		if (rowBased.length == 0) {
			throw new IllegalArgumentException("Leere Brett-Matrizen können nicht transponiert werden");
		}

		for (Piece[] row : rowBased) {
			if (row == null) {
				throw new IllegalArgumentException("Die Brett-Matrix darf keine null-Zeilen enthalten");
			}

			if (row.length != rowBased.length) {
				throw new IllegalArgumentException("Nur quadratische Brett-Matrizen können transponiert werden");
			}
		}

		int boardSize = rowBased.length;

		Piece[][] columnBased = new Piece[boardSize][boardSize];

		for (int file = 0; file < boardSize; file++) {
			for (int rank = 0; rank < boardSize; rank++) {
				columnBased[file][rank] = rowBased[rank][file];
			}
		}

		return columnBased;
	}

	/**
	 * Prüft, dass die beiden Spielfelder an allen nicht ignorierten Feldern die
	 * gleichen Figuren haben
	 * 
	 * @param oldBoard
	 *            altes Spielfeld
	 * @param newBoard
	 *            neues Spielfeld
	 * @param ignoreFields
	 *            List an Feldern, die für die Prüfung ignoriert werden sollten
	 */
	public static void assertBoardUnmodified(Piece[][] oldBoard, Piece[][] newBoard, Coordinate... ignoreFields) {
		List<Coordinate> ignored = Arrays.asList(ignoreFields);
		for (int file = 0; file < oldBoard.length; file++) {
			for (int rank = 0; rank < oldBoard[file].length; rank++) {
				if (!ignored.contains(new Coordinate(file, rank))) {
					assertEquals(oldBoard[file][rank], newBoard[file][rank]);
				}
			}
		}
	}

	/**
	 * Prüft, dass die Liste an Moves genau die erwarteten Moves enthält
	 * 
	 * @param generatedMoves
	 *            Moves, die eine Figur machen kann
	 * @param expectedMoves
	 *            Moves, die erwartet sind
	 */
	public static void assertMovesAreExactly(List<Move> generatedMoves, List<Move> expectedMoves) {
		assertEquals(expectedMoves.size(), generatedMoves.size());
		for (Move move : expectedMoves) {
			assertTrue(generatedMoves.contains(move));
		}
	}

	/**
	 * Generiert Liste an identischen Moves, bei denen nur die Zielkoordinate
	 * jeweils variiert ist. Kann Fehler werfen, wenn der Movetype mehr
	 * Informationen erfordert
	 * 
	 * @param piece
	 *            Figur für diese Moves
	 * @param from
	 *            Startkoordinate
	 * @param type
	 *            Zugart
	 * @param to
	 *            Zielkoordinaten ala algebraische Notation
	 * @return Liste an Moves mit identischem Start, piece und typ aber den
	 *         verschiedenen Zielkoordinaten
	 */
	public static List<Move> generateMovesTo(Piece piece, Coordinate from, MoveType type, List<String> to) {
		return to.stream().map(ChessTestUtil::getCoordinate).map(toCoord -> new Move(piece, from, toCoord, type))
				.toList();
	}

	/**
	 * @param algebraicNotation
	 *            Algebraische Notation eines Feldes
	 * @return Coordinate die dieses Feld repräsentiert
	 */
	public static Coordinate getCoordinate(String algebraicNotation) {
		return new Coordinate(algebraicNotation.charAt(0) - 97, Integer.parseInt(algebraicNotation.substring(1)) - 1);
	}
}
