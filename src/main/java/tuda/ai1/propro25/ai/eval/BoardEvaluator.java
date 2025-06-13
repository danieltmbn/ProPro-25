/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.ai.eval;

import tuda.ai1.propro25.model.Board;

/**
 * Ein BoardEvaluator kann dem aktuellen Brett einen Wert zuweisen um die
 * Spielposition zu evaluieren. Die Evaluierung ist immer auf den aktuellen
 * Spieler im Brettzustand bezogen.
 */
public interface BoardEvaluator {

	/**
	 * Evaluiert die aktuelle Spielposition auf dem Brett. Die Evaluation ist immer
	 * auf den aktuellen Spieler bezogen. Positive Werte sehen also den aktuellen
	 * Spieler im Vorteil, um mit NegaMax kompatibel zu sein.
	 * 
	 * @param board
	 *            Board, auf dem der aktuelle Zustand evaluiert werden soll
	 * @return Evaluation der Spielposition
	 */
	double evaluate(Board board);
}
