/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.ai;

import java.util.List;
import tuda.ai1.propro25.ai.eval.EvaluationStep;
import tuda.ai1.propro25.model.Board;
import tuda.ai1.propro25.model.Color;

public abstract class ConfigurableAIOpponent extends AIOpponent {

	List<EvaluationStep> evaluationPipeline;
	protected final int depth;

	/**
	 * AI Gegner, welcher eine konfigurierbare Rechentiefe hat. Bei Tiefe 1 werden
	 * nur alle direkt verfügbaren Züge evaluiert.
	 * 
	 * @param name
	 *            Name des Spielers
	 * @param color
	 *            Figurfarbe
	 * @param remainingTime
	 *            übrige Spielzeit
	 * @param depth
	 *            Rechentiefe (1...+inf)
	 */
	public ConfigurableAIOpponent(String name, Color color, int remainingTime, int depth) {
		super(name, color, remainingTime);
		if (depth <= 0) {
			throw new IllegalArgumentException("Rechentiefe muss mindestens 1 sein!");
		}
		this.depth = depth;
	}

	/**
	 * Evaluiert den Boardzustand, indem alle Evaluationsschritte in der
	 * {@link #evaluationPipeline} angewandt werden. Die einzelnen Evaluationen
	 * werden mit ihren weights gewichtet und dann addiert. Wenn ein Evaluator
	 * +-Infinity oder +-Double.MAX_VALUE zurückgibt, überschreibt dieser Wert das
	 * Ergebnis unabhängig vom Gewicht.
	 * 
	 * @param board
	 *            Boardzustand, der evaluiert werden soll
	 * @return Evaluierung des aktuellen Spielzustandes aus Sicht des Spielers, der
	 *         am Zug ist
	 */
	double evaluate(Board board) {
		double current = 0;
		if (evaluationPipeline == null) {
			return current;
		}
		for (var step : evaluationPipeline) {
			double eval = step.evaluator().evaluate(board);
			if (eval == Double.POSITIVE_INFINITY || eval == Double.NEGATIVE_INFINITY
					|| Math.abs(eval) == Double.MAX_VALUE) {
				return eval;
			}
			current += (step.weight() * eval);
		}
		return current;
	}
}
