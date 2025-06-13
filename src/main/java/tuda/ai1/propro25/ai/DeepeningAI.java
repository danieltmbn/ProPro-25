/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import tuda.ai1.propro25.ai.eval.*;
import tuda.ai1.propro25.model.*;

/**
 * Diese AI basiert auf der PruningAI {@link PruningAI}, nutzt aber ein paar
 * Verbesserungen: 1: Die Züge werden sortiert, bevor die Zustandsbäume in diese
 * Richtung evaluiert werden. Dabei wird zuerst mit dem aktuell am besten
 * erscheinenden Zug gestartet. Die Sortierung basiert nur darauf, wie sich CPVs
 * mit dem Zug ändern. Es ist eine sehr simple Vorhersage, bewirkt aber, dass
 * bessere Züge mit hoher Wahrscheinlichkeit zuerst evaluiert werden. Dadurch
 * können mehr Teilbäume gepruned werden. 2: "Quiescence search": Die normale
 * Suchfunktion wird so abgeändert, dass bei instabilen Zuständen noch tiefer
 * gesucht wird als eigentlich angegeben. Dies verhindert/verringert den
 * "Horizon effect". 3: Piece square tables: Diese AI nutzt den
 * {@link PieceSquareTableEvaluator}
 */
public class DeepeningAI extends PruningAI {

	private final Comparator<Move> moveComparator;

	/**
	 * Diese AI erstellt einen Suchbaum mindestens bis zur angegebenen Tiefe, nutzt
	 * alpha beta pruning und expandiert instabile Spielzustände beliebig tief.
	 *
	 * @param player
	 *            Spieler, welchen diese AI steuert/repräsentiert
	 * @param depth
	 *            Tiefe, bis zu welcher der Suchbaum mindestens gebildet werden soll
	 */
	public DeepeningAI(Player player, int depth) {
		this(player == null ? null : player.getName(), player == null ? null : player.getColor(),
				player == null ? -1 : player.getRemainingTime(), depth);
	}

	/**
	 * Diese AI erstellt einen Suchbaum mindestens bis zur angegebenen Tiefe, nutzt
	 * alpha beta pruning und expandiert instabile Spielzustände beliebig tief.
	 *
	 * @param name
	 *            Name der AI als Spieler
	 * @param color
	 *            Figurenfarbe, mit welcher diese AI spielt
	 * @param remainingTime
	 *            Zugzeit, welche diese AI noch hat
	 * @param depth
	 *            Tiefe, bis zu welcher der Suchbaum mindestens gebildet werden soll
	 */
	public DeepeningAI(String name, Color color, int remainingTime, int depth) {
		super(name, color, remainingTime, depth);

		this.evaluationPipeline = List.of(new EvaluationStep(new EndConditionEvaluator(), 1.0),
				new EvaluationStep(new CheckEvaluator(), 1.0), new EvaluationStep(new MaterialEvaluator(), 1.0),
				new EvaluationStep(new PieceSquareTableEvaluator(), 1.0));

		this.moveComparator = new MoveComparator().reversed();
	}

	/**
	 *
	 * Für instabile Zustände suchen wir extra tief erweiterte Teilbäume ab.
	 * Zustände sind instabil, wenn jemand im Schach steht und/oder gerade eine
	 * Figur geschlagen oder umgewandelt werden kann. Da nicht bekannt ist, wie tief
	 * gesucht werden soll, wird ein Knoten evaluiert bevor er erweitert wird, um
	 * möglicherweise zu prunen. Siehe {@link #search(Board, int, double, double)}
	 * für Informationen zur Suche allgemein.
	 * 
	 * @param board
	 *            Brett mit aktuellem Zustand, welcher evaluiert werden soll
	 * @param alpha
	 *            Alpha (siehe alpha beta pruning)
	 * @param beta
	 *            Beta (siehe alpha beta pruning)
	 * @return beste evaluation im Teilbaum dieses Knotens (alpha) oder beste
	 *         Bekannte, wenn Suche sich nicht lohnt (beta)
	 */
	private double quiescenceSearch(Board board, double alpha, double beta) {
		evaluatedPositions++;
		// Dieser Evaluation wird oft "Stand-pat" genannt: Der Wert dieses Knotens im
		// Spielbaum, wenn wir hier jetzt aufhören würden zu suchen. Wir prüfen das
		// vorher, da die quiescenceSearch nicht alle Teilbäume erforscht und wir gar
		// nicht wissen, wann wir ganz unten sind und uns unnötige arbeit sparen wollen
		double currentEval = evaluate(board);
		if (currentEval >= beta) {
			// Wir werden eh nie hier landen, also brauchen wir nicht tiefer zu suchen
			return beta;
		}
		alpha = Math.max(alpha, currentEval);

		List<Move> moves;
		// Nur noch instabile Zustände sind interessant, also filtern wir alle moves
		if (board.getColorInCheck() != null) {
			// Bei Schach suchen wir ALLE weiteren Moves ab, hier könnte sich nämlich
			// einiges ändern
			moves = new ArrayList<>(board.findAllLegalMoves()); // Kopie damit modifizierbar zum sortieren
			moves.sort(moveComparator);
		} else {
			// Ansonsten nur CAPTUREs und Promotionen
			moves = board.findAllLegalMoves().stream()
					.filter(move -> move.getType() == MoveType.CAPTURE_PROMOTION || move.getType() == MoveType.CAPTURE
							|| move.getType() == MoveType.EN_PASSANT || move.getType() == MoveType.PROMOTION)
					.sorted(moveComparator).toList();
		}
		if (moves.isEmpty()) {
			// Wir können nicht tiefer suchen, daher diesen Zustand evaluieren
			return evaluate(board);
		}

		for (Move m : moves) {
			board.makeMove(m);
			double evaluation = -quiescenceSearch(board, -beta, -alpha);
			board.undoLastMove();

			if (evaluation >= beta) {
				return beta;
			}
			alpha = Math.max(alpha, evaluation);
		}
		return alpha;
	}

	/**
	 * Sucht den aktuellen Zustandsbaum bis mindestens zur übrigen Tiefe ab und
	 * bewertet die Zustände. Das ganze passiert rekursiv. Auf der obersten Ebene
	 * werden die tatsächlichen Züge im Klassenfeld currentlyPreferredMove
	 * gespeichert, um die rekursive Methode schneller zu halten. Die Suche nutzt
	 * Alpha-Beta pruning, um die Anzahl an Teilbäumen, die tatsächlich evaluiert
	 * werden muss, zu verringern. Dabei werden Teilbäume gar nicht mehr abgesucht,
	 * wenn der gegnerische Spieler sie nie zulassen würde, weil er einen besseren
	 * Zug machen könnte. Siehe "Alpha-Beta-Pruning" um die Parameter alpha und beta
	 * besser zu verstehen.
	 * 
	 * @param board
	 *            Brettzustand, der als Wurzelknoten dient
	 * @param remainingDepth
	 *            übrige Tiefe, die noch mindestens gesucht werden soll
	 * @param alpha
	 *            Der Maximizer kann garantieren, mindestens diesen Wert zu
	 *            erreichen (lower bound)
	 * @param beta
	 *            Der Minimizer kann garantieren, höchstens diesen Wert zu erreichen
	 *            (upper bound)
	 * @return beste evaluation im Teilbaum dieses Knotens (alpha) oder beste
	 *         Bekannte, wenn Suche sich nicht lohnt (beta)
	 */
	@Override
	double search(Board board, int remainingDepth, double alpha, double beta) {
		if (remainingDepth == 0) {
			return quiescenceSearch(board, alpha, beta);
		}
		// Ganz normaler Knoten im Suchbaum, tiefer gehen!
		// Wir müssen die Liste erst einmal kopieren, damit wir sie sortieren und
		// modifizieren können
		List<Move> moves = new ArrayList<>(board.findAllLegalMoves());
		moves.sort(moveComparator);
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
			if (moveEval >= beta) {
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
	public String getAIConfigString() {
		return "DeepeningAI{d(" + depth + ")}";
	}

	@Override
	public String toString() {
		return "DeepeningAI{" + getColor() + ", d(" + depth + ")}";
	}

}
