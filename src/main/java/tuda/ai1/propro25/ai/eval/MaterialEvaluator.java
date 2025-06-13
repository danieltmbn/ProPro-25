/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.ai.eval;

import tuda.ai1.propro25.model.Board;
import tuda.ai1.propro25.model.Color;

/**
 * Evaluiert Figur/Material-differenzen nach Chess Piece Values
 */
public class MaterialEvaluator implements BoardEvaluator {

	/**
	 * @return Materialwertdifferenz
	 */
	@Override
	public double evaluate(Board board) {
		int[] pieces = board.getPieceValues();
		Color currentColor = board.getCurrentPlayer().getColor();
		int pieceDiff = (pieces[0] - pieces[1]);
		return pieceDiff * (currentColor == Color.WHITE ? 1 : -1);
	}
}
