/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.ai;

import java.util.List;
import tuda.ai1.propro25.ai.eval.*;
import tuda.ai1.propro25.model.Board;
import tuda.ai1.propro25.model.Color;
import tuda.ai1.propro25.model.Move;
import tuda.ai1.propro25.model.Player;

/**
 * Diese AI erstellt einen Suchbaum und sucht diesen vollständig bis zur
 * angegebenen Tiefe ab. Dabei wird depth-first jeder Knoten expandiert und dann
 * nach Negamax (einer einfacher zu implementierenden Variante von Minimax) der
 * beste nächste Zug gewählt.
 */
public class SearchAI extends ConfigurableAIOpponent {

	long evaluatedPositions = 0;

	/**
	 * Diese AI erstellt einen Suchbaum bis zur angegebenen Tiefe.
	 * 
	 * @param player
	 *            Spieler, welchen diese AI steuert/repräsentiert
	 * @param depth
	 *            Tiefe, bis zu welcher der Suchbaum gebildet werden soll
	 */
	public SearchAI(Player player, int depth) {
		this(player == null ? null : player.getName(), player == null ? null : player.getColor(),
				player == null ? -1 : player.getRemainingTime(), depth);
	}

	/**
	 * Diese AI erstellt einen Suchbaum bis zur angegebenen Tiefe.
	 * 
	 * @param name
	 *            Name der AI als Spieler
	 * @param color
	 *            Figurenfarbe, mit welcher diese AI spielt
	 * @param remainingTime
	 *            Zugzeit, welche diese AI noch hat
	 * @param depth
	 *            Tiefe, bis zu welcher der Suchbaum gebildet werden soll
	 */
	public SearchAI(String name, Color color, int remainingTime, int depth) {
		super(name, color, remainingTime, depth);
		this.evaluationPipeline = List.of(new EvaluationStep(new EndConditionEvaluator(), 1.0),
				new EvaluationStep(new CheckEvaluator(), 1.0), new EvaluationStep(new MaterialEvaluator(), 1.0));
	}

	/**
	 * Sucht den aktuellen Zustandsbaum bis zur übrigen Tiefe ab und bewertet die
	 * Zustände. Das ganze passiert rekursiv.
	 * 
	 * @param board
	 *            Brettzustand, der als Wurzelknoten dient
	 * @param remainingDepth
	 *            übrige Tiefe, die noch gesucht werden soll
	 * @return Beste gefundene Evaluation dieses Teilbaumes
	 */
	double search(Board board, int remainingDepth) {
		if (remainingDepth == 0) {
			evaluatedPositions++;
			return evaluate(board);
		}
		List<Move> moves = board.findAllLegalMoves();
		if (moves.isEmpty()) {
			// Wir können nicht tiefer suchen, daher diesen Zustand evaluieren
			evaluatedPositions++;
			return evaluate(board);
		}
		double bestEval = Double.NEGATIVE_INFINITY;
		for (Move move : moves) {
			board.makeMove(move);
			double moveEval = -search(board, remainingDepth - 1);
			if (moveEval > bestEval) {
				bestEval = moveEval;
			}
			board.undoLastMove();
		}
		return bestEval;
	}

	@Override
	Move calculateNextMove(Board board) {
		Move actualMove = null;
		evaluatedPositions = 0;
		double bestEval = Double.NEGATIVE_INFINITY;
		for (Move move : board.findAllLegalMoves()) {
			board.makeMove(move);
			double moveEval = -search(board, depth - 1);
			if (moveEval > bestEval) {
				bestEval = moveEval;
				actualMove = move;
			}
			board.undoLastMove();
		}
		if (actualMove == null && !board.findAllLegalMoves().isEmpty()) {
			actualMove = board.findAllLegalMoves().get(0);
		}
		return actualMove;
	}

	@Override
	public String getAIConfigString() {
		return "SearchAI{d(" + depth + ")}";
	}

	@Override
	public String toString() {
		return "SearchAI{" + getColor() + ", d(" + depth + ")}";
	}

	/**
	 * @return Wie viele Knoten des Spielbaums diese AI für die Berechnung des
	 *         letzten Zuges evaluiert hat
	 */
	public long getEvaluatedPositions() {
		return evaluatedPositions;
	}
}
