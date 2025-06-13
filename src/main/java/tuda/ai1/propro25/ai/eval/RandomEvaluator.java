/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.ai.eval;

import java.util.Random;
import tuda.ai1.propro25.model.Board;

public class RandomEvaluator implements BoardEvaluator {

	Random rand = new Random();

	/**
	 * @return Zufälliger Wert zwischen -1 und +1
	 */
	@Override
	public double evaluate(Board board) {
		return -1 + 2 * rand.nextDouble();
	}
}
