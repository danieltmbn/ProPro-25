/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.model.piece;

import java.util.ArrayList;
import java.util.List;
import tuda.ai1.propro25.model.*;

/**
 * Ein Pawn ist ein Bauer, eine der Schachfiguren und erbt von Piece (Figur)
 */
public class Pawn extends Piece {
	public Pawn(Color color) {
		super(color, 1);
	}

	@Override
	public char getAlgebraicNotationSymbol() {
		return 'P'; // wird in der Regel nur benutzt, wenn es Doppeldeutigkeiten gibt
	}

	@Override
	public List<Move> getPseudolegalMoves(Coordinate currentCoordinate, Board board) {
		var possibleMoves = new ArrayList<Move>();
		int moveDirection = getColor() == Color.WHITE ? 1 : -1;

		int x = currentCoordinate.getFile();
		int y = currentCoordinate.getRank();

		// Normaler Schritt nach vorne
		var targetCoordinateOneStep = new Coordinate(x, y + moveDirection);
		if (targetCoordinateOneStep.isOnBoard() && board.getPiece(targetCoordinateOneStep) == null) {
			if (y + moveDirection == (moveDirection == 1 ? 7 : 0)) {
				// Wenn ein Bauer die letzte Reihe erreicht, dann ist es kein normaler Zug,
				// sondern eine Umwandlung!
				possibleMoves.add(new Move(this, currentCoordinate, targetCoordinateOneStep, new Rook(color)));
				possibleMoves.add(new Move(this, currentCoordinate, targetCoordinateOneStep, new Knight(color)));
				possibleMoves.add(new Move(this, currentCoordinate, targetCoordinateOneStep, new Bishop(color)));
				possibleMoves.add(new Move(this, currentCoordinate, targetCoordinateOneStep, new Queen(color)));
			} else {
				possibleMoves.add(new Move(this, currentCoordinate, targetCoordinateOneStep));
			}
		}

		// Doppelschritt nach vorne
		var targetCoordinateTwoStep = new Coordinate(x, y + 2 * moveDirection);
		if (targetCoordinateOneStep.isOnBoard()
				&& ((currentCoordinate.getRank() == 1 && color == Color.WHITE)
						|| (currentCoordinate.getRank() == 6 && color == Color.BLACK))
				&& board.getPiece(targetCoordinateOneStep) == null && board.getPiece(targetCoordinateTwoStep) == null) {
			possibleMoves.add(new Move(this, currentCoordinate, targetCoordinateTwoStep, MoveType.DOUBLEPAWN));
		}

		// Schläge zur Seite vorne
		for (int deltaX : new int[]{-1, 1}) {
			int targetX = x + deltaX;
			int targetY = y + moveDirection;

			var targetCoordinateAttack = new Coordinate(targetX, targetY);
			if (!targetCoordinateAttack.isOnBoard()) {
				continue;
			}
			Piece capturedPiece = board.getPiece(targetCoordinateAttack);
			if (capturedPiece != null && capturedPiece.getColor() != color) {
				// Hier könnte tatsächlich eine Figur geschlagen werden
				if (y + moveDirection == (moveDirection == 1 ? 7 : 0)) {
					// Wenn ein Bauer die letzte Reihe erreicht, dann ist es kein normales Schlagen,
					// sondern gleichzeitig eine Umwandlung!
					possibleMoves.add(new Move(this, currentCoordinate, targetCoordinateAttack,
							MoveType.CAPTURE_PROMOTION, capturedPiece, new Rook(color)));
					possibleMoves.add(new Move(this, currentCoordinate, targetCoordinateAttack,
							MoveType.CAPTURE_PROMOTION, capturedPiece, new Knight(color)));
					possibleMoves.add(new Move(this, currentCoordinate, targetCoordinateAttack,
							MoveType.CAPTURE_PROMOTION, capturedPiece, new Bishop(color)));
					possibleMoves.add(new Move(this, currentCoordinate, targetCoordinateAttack,
							MoveType.CAPTURE_PROMOTION, capturedPiece, new Queen(color)));
				} else {
					possibleMoves.add(
							new Move(this, currentCoordinate, targetCoordinateAttack, MoveType.CAPTURE, capturedPiece));
				}
			}

		}

		// En passant - wir müssen hier nicht aufpassen, dass wir aus Versehen auch
		// normale Schläge überschreiben, da sich en passant und normales Schlagen in
		// die gleiche Richtung gegenseitig ausschließen (XOR)
		for (int deltaX : new int[]{-1, 1}) {
			int targetX = x + deltaX;
			int targetY = y + moveDirection;

			var targetCoordinateEnPassant = new Coordinate(targetX, targetY);
			if (targetCoordinateEnPassant.isOnBoard() && board.getPiece(targetCoordinateEnPassant) == null) {
				var lastMove = board.getLastMove();
				if (lastMove != null && lastMove.getType() == MoveType.DOUBLEPAWN && lastMove.getTo().getRank() == y
						&& (lastMove.getTo().getFile() == targetX)) {
					possibleMoves.add(new Move(this, currentCoordinate, targetCoordinateEnPassant, MoveType.EN_PASSANT,
							board.getPiece(lastMove.getTo())));
				}
			}
		}

		return possibleMoves;
	}

}
