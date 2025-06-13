/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.model;

import static org.junit.jupiter.api.Assertions.*;
import static tuda.ai1.propro25.ChessTestUtil.getCoordinate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tuda.ai1.propro25.fen.FENFormatException;
import tuda.ai1.propro25.model.piece.*;

public class StudiBoardTest {

	private static final String FEN = "r3k3/3p2p1/8/2n5/8/8/8/2BQK2R b Kq - 0 1";

	private Board board;

	@BeforeEach
	void setup() throws FENFormatException {
		board = new Board(FEN);
	}

	/**
	 * Testet die Positionen der folgenden weißen Figuren:
	 * <ul>
	 * <li>Läufer bei c1</li>
	 * <li>Dame bei d1</li>
	 * <li>König bei e1</li>
	 * <li>Turm bei h1</li>
	 * </ul>
	 */
	@Test
	void testWhitePiecePositions() {
		assertEquals(new Bishop(Color.WHITE), board.getPiece(getCoordinate("c1")));
		assertEquals(new Queen(Color.WHITE), board.getPiece(getCoordinate("d1")));
		assertEquals(new King(Color.WHITE), board.getPiece(getCoordinate("e1")));
		assertEquals(new Rook(Color.WHITE), board.getPiece(getCoordinate("h1")));
	}

	/**
	 * Testet die Positionen der folgenden schwarzen Figuren:
	 * <ul>
	 * <li>Turm bei a8</li>
	 * <li>Springer bei c5</li>
	 * <li>Bauer bei d7</li>
	 * <li>König bei e8</li>
	 * <li>Bauer bei g7</li>
	 * </ul>
	 */
	@Test
	void testBlackPiecePositions() {
		// TODO: Aufgabe 4
	}

	/**
	 * Testet, dass auf der Reihe 3 keine Figur steht. Erinnerung: getPiece gibt
	 * null zurück, falls an der übergebenen Position keine Figur steht.
	 */
	@Test
	void testRank3Empty() {
		// TODO: Aufgabe 4
	}

	/**
	 * Testet, dass folgende Rochadenrechte gelten:
	 * <ul>
	 * <li>Weiß kann kurz rochieren (king side)</li>
	 * <li>Weiß kann NICHT lang rochieren (queen side)</li>
	 * <li>Schwarz kann NICHT kurz rochieren</li>
	 * <li>Schwarz kann lang rochieren</li>
	 * </ul>
	 *
	 * Erinnerung: Die Klasse {@link CastlingAvailability} hat eine Getter-Methode
	 * für jede Rochade, die true zurückgibt, falls diese Rochade möglich ist.
	 */
	@Test
	void testCastlingAvailability() {
		// TODO: Aufgabe 4
	}

	/**
	 * Testet, dass die Farbe des Spielers, der gerade dran ist, Schwarz ist.
	 */
	@Test
	void testCurrentPlayerColor() {
		// TODO: Aufgabe 4
	}

}
