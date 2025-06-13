/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.model.piece;

import java.util.List;
import tuda.ai1.propro25.model.*;

/**
 * Eine Queen ist eine Dame, eine der Schachfiguren und erbt von SlidingPiece
 * (Schiebefigur) und damit auch von Piece (Figur) (Figur)
 */
public class Queen extends SlidingPiece {
	public Queen(Color color) {
		super(color, 9);
	}

	@Override
	public char getAlgebraicNotationSymbol() {
		return 'Q';
	}

	@Override
	public List<Move> getPseudolegalMoves(Coordinate currentCoordinate, Board board) {

		int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}, {0, 1}, {0, -1}, {1, 0}, {-1, 0}};

		return getSlidingMoves(currentCoordinate, board, directions);
	}
}
