/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.ai;

import java.util.List;
import tuda.ai1.propro25.ai.eval.*;
import tuda.ai1.propro25.model.Board;
import tuda.ai1.propro25.model.Color;
import tuda.ai1.propro25.model.Move;
import tuda.ai1.propro25.model.Player;

/**
 * Diese AI erstellt einen Suchbaum und sucht diesen bis zur angegebenen Tiefe
 * ab. Dabei wird depth-first jeder Knoten expandiert und dann nach Negamax
 * (einer einfacher zu implementierenden Variante von Minimax) der beste nächste
 * Zug gewählt. Wenn möglich werden Teilbäume ignoriert / "gepruned", wodurch
 * deutlich weniger Zustände tatsächlich abgesucht werden müssen. Die AI
 * sortiert die Züge aber vorher nicht, was das Pruning etwas in der
 * Effektivität einschränkt.
 */
public class PruningAI extends SearchAI {

	Move currentlyPreferredMove;

	/**
	 * Diese AI erstellt einen Suchbaum bis zur angegebenen Tiefe und nutzt alpha
	 * beta pruning.
	 * 
	 * @param player
	 *            Spieler, welchen diese AI steuert/repräsentiert
	 * @param depth
	 *            Tiefe, bis zu welcher der Suchbaum gebildet werden soll
	 */
	public PruningAI(Player player, int depth) {
		this(player == null ? null : player.getName(), player == null ? null : player.getColor(),
				player == null ? -1 : player.getRemainingTime(), depth);
	}

	/**
	 * Diese AI erstellt einen Suchbaum bis zur angegebenen Tiefe und nutzt alpha
	 * beta pruning.
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
	public PruningAI(String name, Color color, int remainingTime, int depth) {
		super(name, color, remainingTime, depth);
		this.evaluationPipeline = List.of(new EvaluationStep(new EndConditionEvaluator(), 1.0),
				new EvaluationStep(new CheckEvaluator(), 1.0), new EvaluationStep(new MaterialEvaluator(), 1.0));
	}

	/**
	 * Sucht den aktuellen Zustandsbaum bis zur übrigen Tiefe ab und bewertet die
	 * Zustände. Das ganze passiert rekursiv. Auf der obersten Ebene werden die
	 * tatsächlichen Züge im Klassenfeld currentlyPreferredMove gespeichert, um die
	 * rekursive Methode schneller zu halten. Die Suche nutzt Alpha-Beta pruning, um
	 * die Anzahl an Teilbäumen, die tatsächlich evaluiert werden muss, zu
	 * verringern. Dabei werden Teilbäume gar nicht mehr abgesucht, wenn der
	 * gegnerische Spieler sie nie zulassen würde, weil er einen besseren Zug machen
	 * könnte. Siehe "Alpha-Beta-Pruning" um die Parameter alpha und beta besser zu
	 * verstehen.
	 *
	 * @param board
	 *            Brettzustand, der als Wurzelknoten dient
	 * @param remainingDepth
	 *            übrige Tiefe, die noch gesucht werden soll
	 * @param alpha
	 *            Der Maximizer kann garantieren, mindestens diesen Wert zu
	 *            erreichen (lower bound)
	 * @param beta
	 *            Der Minimizer kann garantieren, höchstens diesen Wert zu erreichen
	 *            (upper bound)
	 * @return beste evaluation im Teilbaum dieses Knotens (alpha) oder beste
	 *         Bekannte, wenn Suche sich nicht lohnt (beta)
	 */
	double search(Board board, int remainingDepth, double alpha, double beta) {
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
		for (Move move : moves) {
			board.makeMove(move);
			// Alpha und Beta werden getauscht da wir NegaMax statt klassischem Minimax
			// nutzen
			double moveEval = -search(board, remainingDepth - 1, -beta, -alpha);
			board.undoLastMove();
			if (moveEval > beta) {
				// Dieser Zug eben war so gut, dass der Gegner diesen Teilbaum definitiv
				// vermeiden wird. Wir brauchen also keine weiteren Züge in diesem Teilbaum zu
				// erforschen.
				return beta;
			}
			if (moveEval > alpha) {
				alpha = moveEval;
				if (remainingDepth == depth) {
					// Den Move auf oberster Ebene tatsächlich speichern
					currentlyPreferredMove = move;
				}
			}
		}
		return alpha;
	}

	@Override
	Move calculateNextMove(Board board) {
		currentlyPreferredMove = null;
		double alpha = Double.NEGATIVE_INFINITY;
		double beta = Double.POSITIVE_INFINITY;
		evaluatedPositions = 0;
		search(board, depth, alpha, beta);
		if (currentlyPreferredMove == null && !board.findAllLegalMoves().isEmpty()) {
			currentlyPreferredMove = board.findAllLegalMoves().get(0);
		}
		return currentlyPreferredMove;
	}

	@Override
	public String getAIConfigString() {
		return "PruningAI{d(" + depth + ")}";
	}

	@Override
	public String toString() {
		return "PruningAI{" + getColor() + ", d(" + depth + ")}";
	}
}
