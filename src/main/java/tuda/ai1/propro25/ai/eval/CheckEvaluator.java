/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.ai.eval;

import tuda.ai1.propro25.model.Board;

public class CheckEvaluator implements BoardEvaluator {

	/**
	 * @return +-1 bei normalem Schach, 0 sonst
	 */
	@Override
	public double evaluate(Board board) {
		if (board.getColorInCheck() != null) {
			return board.getColorInCheck() == board.getCurrentPlayer().getColor() ? -0.5 : 0.5;
		}
		return 0;
	}
}
