/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.model.piece;

import java.util.ArrayList;
import java.util.List;
import tuda.ai1.propro25.model.*;

/**
 * Ein King ist ein König, eine der Schachfiguren und erbt von Piece (Figur)
 */
public class King extends Piece {
	public King(Color color) {
		super(color, 0);
	}

	@Override
	public char getAlgebraicNotationSymbol() {
		return 'K';
	}

	@Override
	public List<Move> getPseudolegalMoves(Coordinate currentCoordinate, Board board) {
		var possibleMoves = new ArrayList<Move>();
		int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

		for (int[] direction : directions) {
			int x = currentCoordinate.getFile() + direction[0];
			int y = currentCoordinate.getRank() + direction[1];

			var targetCoordinate = new Coordinate(x, y);
			if (targetCoordinate.isOnBoard()) {
				var pieceOnField = board.getPiece(targetCoordinate);
				if (pieceOnField == null) {
					possibleMoves.add(new Move(this, currentCoordinate, targetCoordinate));
				} else if (pieceOnField.getColor() != color) {
					// Andere Farbe, die Figur könnte also evtl. geschlagen werden
					possibleMoves
							.add(new Move(this, currentCoordinate, targetCoordinate, MoveType.CAPTURE, pieceOnField));
				} // Ansonsten passiert nichts
			}
		}
		possibleMoves.addAll(getPseudolegalCastlingMoves(currentCoordinate, board));
		return possibleMoves;
	}

	/**
	 * Generiert die möglichen Rochadezüge(Castling) des Königs. Die Rochade ist nur
	 * möglich, wenn der König und der entsprechende Turm noch nicht gezogen wurde.
	 *
	 * @param currentCoordinate
	 *            Die aktuelle Position des Königs auf dem Schachbrett.
	 */
	private List<Move> getPseudolegalCastlingMoves(Coordinate currentCoordinate, Board board) {
		if (currentCoordinate.getFile() != 4) {
			// Sowieso keine Rochade möglich -> Leere Liste
			return List.of();
		}
		var castlingMoves = new ArrayList<Move>();

		int y = currentCoordinate.getRank();

		if (board.hasCastlingAvailability(color, false)) {
			if (board.getPiece(0, y) != null && board.getPiece(1, y) == null && board.getPiece(2, y) == null
					&& board.getPiece(3, y) == null) {
				castlingMoves.add(new Move(this, currentCoordinate, new Coordinate(2, y), MoveType.CASTLING_QUEENSIDE,
						board.getPiece(0, y)));
			}
		}
		if (board.hasCastlingAvailability(color, true)) {
			if (board.getPiece(7, y) != null && board.getPiece(5, y) == null && board.getPiece(6, y) == null) {
				castlingMoves.add(new Move(this, currentCoordinate, new Coordinate(6, y), MoveType.CASTLING_KINGSIDE,
						board.getPiece(7, y)));
			}
		}

		return castlingMoves;
	}
}
