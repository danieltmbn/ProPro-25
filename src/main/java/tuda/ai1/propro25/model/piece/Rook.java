/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.model.piece;

import java.util.List;
import tuda.ai1.propro25.model.*;

/**
 * Ein Rook ist ein Turm, eine der Schachfiguren und erbt von SlidingPiece
 * (Schiebefigur) und damit auch von Piece (Figur)
 */
public class Rook extends SlidingPiece {
	public Rook(Color color) {
		super(color, 5);
	}

	@Override
	public char getAlgebraicNotationSymbol() {
		return 'R';
	}

	@Override
	public List<Move> getPseudolegalMoves(Coordinate currentCoordinate, Board board) {
		int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
		return getSlidingMoves(currentCoordinate, board, directions);
	}
}
