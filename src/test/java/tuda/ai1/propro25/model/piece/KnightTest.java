/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.model.piece;

import static tuda.ai1.propro25.ChessTestUtil.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import tuda.ai1.propro25.fen.FENFormatException;
import tuda.ai1.propro25.model.Board;
import tuda.ai1.propro25.model.Coordinate;
import tuda.ai1.propro25.model.Move;
import tuda.ai1.propro25.model.MoveType;

class KnightTest {

	@Test
	void testKnightNOTBlockedByPieces() throws FENFormatException {
		Board board = new Board("8/1P1P1P2/2PPP3/1PPNPP2/2PPP3/1P1P1P2/8/8 b - - 0 1");
		Coordinate knightPosition = new Coordinate(3, 4);
		Knight knight = (Knight) board.getPiece(knightPosition);
		var pseudoLegalMoves = knight.getPseudolegalMoves(knightPosition, board);
		assertMovesAreExactly(pseudoLegalMoves, generateMovesTo(knight, knightPosition, MoveType.NORMAL,
				List.of("b4", "b6", "c3", "c7", "e3", "e7", "f4", "f6")));
	}

	@Test
	void testKnightDoesNotJumpTooFar() throws FENFormatException {
		Board board = new Board("8/5N2/8/4N1N1/8/2N5/8/8 w - - 0 1");
		Coordinate knightPosition = new Coordinate(2, 2);
		Knight knight = (Knight) board.getPiece(knightPosition);
		var pseudoLegalMoves = knight.getPseudolegalMoves(knightPosition, board);
		assertMovesAreExactly(pseudoLegalMoves, generateMovesTo(knight, knightPosition, MoveType.NORMAL,
				List.of("a2", "a4", "b5", "b1", "d5", "d1", "e2", "e4")));
	}

	@Test
	void testKnightDoesNotLeaveBoard() throws FENFormatException {
		Board board = new Board("8/8/8/8/8/4P3/5P2/6N1 b - - 0 1");
		Coordinate knightPosition = new Coordinate(6, 0);
		Knight knight = (Knight) board.getPiece(knightPosition);
		var pseudoLegalMoves = knight.getPseudolegalMoves(knightPosition, board);
		assertMovesAreExactly(pseudoLegalMoves,
				generateMovesTo(knight, knightPosition, MoveType.NORMAL, List.of("e2", "f3", "h3")));
	}

	@Test
	void testKnightMovementPattern() throws FENFormatException {
		Board board = new Board("8/1P1P1P2/2PPP3/1PPNPP2/2PPP3/1P1P1P2/8/8 b - - 0 1");
		Coordinate knightPosition = new Coordinate(3, 4);
		Knight knight = (Knight) board.getPiece(knightPosition);
		var pseudoLegalMoves = knight.getPseudolegalMoves(knightPosition, board);
		assertMovesAreExactly(pseudoLegalMoves, generateMovesTo(knight, knightPosition, MoveType.NORMAL,
				List.of("b4", "b6", "c3", "c7", "e3", "e7", "f4", "f6")));
	}

	@Test
	void testKnightMovementPatternOnEdge() throws FENFormatException {
		Board board = new Board("8/8/6N1/8/8/8/8/8 b - - 0 1");
		Coordinate knightPosition = new Coordinate(6, 5);
		Knight knight = (Knight) board.getPiece(knightPosition);
		var pseudoLegalMoves = knight.getPseudolegalMoves(knightPosition, board);
		assertMovesAreExactly(pseudoLegalMoves,
				generateMovesTo(knight, knightPosition, MoveType.NORMAL, List.of("h8", "f8", "e7", "e5", "f4", "h4")));

		board = new Board("5N2/8/8/8/8/8/8/8 b - - 0 1");
		knightPosition = new Coordinate(5, 7);
		knight = (Knight) board.getPiece(knightPosition);
		pseudoLegalMoves = knight.getPseudolegalMoves(knightPosition, board);
		assertMovesAreExactly(pseudoLegalMoves,
				generateMovesTo(knight, knightPosition, MoveType.NORMAL, List.of("d7", "e6", "g6", "h7")));
	}

	@Test
	void testKnightCanCaptureEnemy() throws FENFormatException {
		Board board = new Board("2kbr2p/5N1r/3p2kn/4p1q1/8/8/8/8 b - - 0 1");
		Coordinate knightPosition = new Coordinate(5, 6);
		Knight knight = (Knight) board.getPiece(knightPosition);
		var pseudoLegalMoves = knight.getPseudolegalMoves(knightPosition, board);
		assertMovesAreExactly(pseudoLegalMoves,
				List.of(new Move(knight, knightPosition, getCoordinate("d8"), MoveType.CAPTURE,
						board.getPiece(getCoordinate("d8"))),
						new Move(knight, knightPosition, getCoordinate("d6"), MoveType.CAPTURE,
								board.getPiece(getCoordinate("d6"))),
						new Move(knight, knightPosition, getCoordinate("e5"), MoveType.CAPTURE,
								board.getPiece(getCoordinate("e5"))),
						new Move(knight, knightPosition, getCoordinate("g5"), MoveType.CAPTURE,
								board.getPiece(getCoordinate("g5"))),
						new Move(knight, knightPosition, getCoordinate("h6"), MoveType.CAPTURE,
								board.getPiece(getCoordinate("h6"))),
						new Move(knight, knightPosition, getCoordinate("h8"), MoveType.CAPTURE,
								board.getPiece(getCoordinate("h8")))

				));
	}

	@Test
	void testKnightDoesNotCaptureFriendly() throws FENFormatException {
		Board board = new Board("2KBR2P/5N1R/3P2KN/4P1Q1/8/8/8/8 b - - 0 1");
		Coordinate knightPosition = new Coordinate(5, 6);
		Knight knight = (Knight) board.getPiece(knightPosition);
		var pseudoLegalMoves = knight.getPseudolegalMoves(knightPosition, board);
		assertMovesAreExactly(pseudoLegalMoves, List.of());
	}

	@Test
	void testKnightNotConfusedByFriendly() throws FENFormatException {
		Board board = new Board("2kbrRBp/4NNPr/3pRNkn/3kp1q1/3Q2n1/8/8/K7 b - - 0 1");
		Coordinate knightPosition = new Coordinate(5, 6);
		Knight knight = (Knight) board.getPiece(knightPosition);
		var pseudoLegalMoves = knight.getPseudolegalMoves(knightPosition, board);
		assertMovesAreExactly(pseudoLegalMoves,
				List.of(new Move(knight, knightPosition, getCoordinate("d8"), MoveType.CAPTURE,
						board.getPiece(getCoordinate("d8"))),
						new Move(knight, knightPosition, getCoordinate("d6"), MoveType.CAPTURE,
								board.getPiece(getCoordinate("d6"))),
						new Move(knight, knightPosition, getCoordinate("e5"), MoveType.CAPTURE,
								board.getPiece(getCoordinate("e5"))),
						new Move(knight, knightPosition, getCoordinate("g5"), MoveType.CAPTURE,
								board.getPiece(getCoordinate("g5"))),
						new Move(knight, knightPosition, getCoordinate("h6"), MoveType.CAPTURE,
								board.getPiece(getCoordinate("h6"))),
						new Move(knight, knightPosition, getCoordinate("h8"), MoveType.CAPTURE,
								board.getPiece(getCoordinate("h8")))

				));
	}
}
