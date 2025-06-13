/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.pgn;

import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;
import tuda.ai1.propro25.model.Move;
import tuda.ai1.propro25.model.MoveType;
import tuda.ai1.propro25.model.history.HistoricalBoardState;
import tuda.ai1.propro25.model.piece.Pawn;
import tuda.ai1.propro25.pgn.move.Checking;

/**
 * Hier werden Züge in ihre algebraische Notation generiert
 */
public class AlgebraicNotationMoveGenerator {

	/**
	 * Schreibt einen einzelnen Zug eines Spielers. Hier wird darauf geachtet, dass
	 * wirklich nur die notwendigen Informationen angegeben werden.
	 *
	 * @param moveState
	 *            der Zustand, aus dem der Zug kommt
	 * @param checking
	 *            ob dieser Zug den gegnerischen König ins Schach stellt
	 */
	public static void writeHalfMove(HistoricalBoardState moveState, Checking checking, PrintWriter output) {
		if (moveState.isIncomplete()) {
			// Entsteht, wenn das Spiel aus FEN rekonstruiert wird und der erste Zug ein
			// Double-Pawn ist,
			// das ein En-Passant ermöglicht.
			// Dann gibt es einen "künstlichen" Zug, dessen Information bereits in der Start
			// FEN enthalten ist.
			throw new IllegalArgumentException("Dieser Zug ist nicht exportierbar, weil er unvollständig ist");
		}

		Move move = moveState.getMoveToNextState();
		if (move.getType() == MoveType.CASTLING_KINGSIDE) {
			output.print("O-O");
			return;
		} else if (move.getType() == MoveType.CASTLING_QUEENSIDE) {
			output.print("O-O-O");
			return;
		}

		if (!(move.getPiece() instanceof Pawn)) {
			output.print(move.getPiece().getAlgebraicNotationSymbol());
		}

		boolean capture = move.getType() == MoveType.CAPTURE || move.getType() == MoveType.CAPTURE_PROMOTION
				|| move.getType() == MoveType.EN_PASSANT;
		boolean pawnCapture = capture && move.getPiece() instanceof Pawn;

		List<Move> candidates = moveState.getLegalMovesInThisState().stream()
				// wir werden sowieso das Figurensymbol (außer beim Bauern) und das Zielfeld
				// angeben
				.filter(candidate -> candidate.getPiece().equals(move.getPiece()))
				.filter(candidate -> candidate.getTo().equals(move.getTo()))
				.filter(candidate -> Objects.equals(candidate.getPromotionPiece(), move.getPromotionPiece())).toList();

		// wenn ein Bauer schlägt, muss immer mind. die Startspalte angegeben werden
		if (candidates.size() > 1 || pawnCapture) {
			// Es reicht nicht aus, nur das Figurensymbol und das Zielfeld anzugeben.

			// Wir schauen, ob die Angabe der Startspalte ausreicht
			List<Move> candidatesFile = candidates.stream()
					.filter(candidate -> candidate.getFrom().getFile() == move.getFrom().getFile()).toList();

			if (candidatesFile.size() == 1) {
				// Angabe der Startspalte reicht
				output.print((char) (move.getFrom().getFile() + 'a'));
			} else {
				// Reicht die Angabe der Startspalte nicht, probieren wir es mit der Startreihen
				List<Move> candidatesRank = candidates.stream()
						.filter(candidate -> candidate.getFrom().getRank() == move.getFrom().getRank()).toList();

				// wenn bei einem Bauern-Schlag die Spalte nicht ausreicht, wird direkt das
				// ganze Startfeld angegeben
				if (candidatesRank.size() == 1 && !pawnCapture) {
					// Angabe der Startreihe reicht aus
					output.print((char) (move.getFrom().getRank() + '1'));
				} else {
					// Wir müssen das komplette Startfeld angeben
					output.print(move.getFrom().getAlgebraicNotation());
				}
			}
		}

		if (capture) {
			output.print('x');
		}

		output.print(move.getTo().getAlgebraicNotation());

		if (move.getType() == MoveType.PROMOTION || move.getType() == MoveType.CAPTURE_PROMOTION) {
			output.print('=');
			output.print(move.getPromotionPiece().getAlgebraicNotationSymbol());
		}

		if (checking == Checking.CHECKMATE) {
			output.print('#');
		} else if (checking == Checking.CHECK) {
			output.print('+');
		}
	}

}
