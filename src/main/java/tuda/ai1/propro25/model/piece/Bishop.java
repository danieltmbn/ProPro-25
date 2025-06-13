/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.model.piece;

import java.util.List;
import tuda.ai1.propro25.model.*;

/**
 * Bishop ist der Läufer, eine der Schachfiguren und erbt von SlidingPiece
 * (Schiebefigur) und damit auch von Piece (Figur)
 */
public class Bishop extends SlidingPiece {

	public Bishop(Color color) {
		super(color, 3);
	}

	@Override
	public char getAlgebraicNotationSymbol() {
		return 'B';
	}

	@Override
	public List<Move> getPseudolegalMoves(Coordinate currentCoordinate, Board board) {
		int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
		return getSlidingMoves(currentCoordinate, board, directions);
	}
}
