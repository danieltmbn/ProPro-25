/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.ai.eval;

import tuda.ai1.propro25.model.Board;

public class EndConditionEvaluator implements BoardEvaluator {

	/**
	 * @return +-unendlich für gewonnen/verloren, -1 bei unentschieden, sonst 0
	 */
	@Override
	public double evaluate(Board board) {
		if (board.getWinner() != null) {
			return board.getWinner() == board.getCurrentPlayer() ? 10000 : -10000;
		}
		return 0;
	}
}
