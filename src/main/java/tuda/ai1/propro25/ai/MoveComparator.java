/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.ai;

import java.util.Comparator;
import tuda.ai1.propro25.model.Move;
import tuda.ai1.propro25.model.MoveType;
import tuda.ai1.propro25.model.piece.Piece;

/**
 * Ein Comparator, der zwei Spielzüge anhand ihrer Bewertung vergleicht. Ein Zug
 * wird danach bewertet, wie er sich auf den Wert aller Figuren auswirkt.
 */
class MoveComparator implements Comparator<Move> {

	/**
	 * Berechnet die Bewertung für den übergebenen Zug. Die Bewertung ist die Summe
	 * aus dem Wert der geschlagenen Figur und dem Wert der umgewandelten Figur. Bei
	 * der Umwandlung ist der Wert des ursprünglichen Bauern wieder abzuziehen. Ist
	 * der Zug weder Schlag noch Umwandlung, ist die Bewertung 0.
	 * 
	 * @param move der zu bewertende Zug
	 * @return die Bewertung dieses Zugs
	 */
	int scoreMove(Move move) {
		// TODO: Aufgabe 5.1
		if(move == null) {
			throw new IllegalArgumentException("Move cannot be NULL.");
		}
		
		int score = 0;
		MoveType type = move.getType();
		
		// Value of the captured piece is added if the move is a capture!
		if(type == MoveType.CAPTURE || type == MoveType.EN_PASSANT || type == MoveType.CAPTURE_PROMOTION) {
			Piece captured = move.getInvolvedPiece();
			if(captured != null) {
				score += captured.getValue();
			}
		}
		
		// Add value of promoted piece minus the pawn's value (1) if it's a promotion!
		if(type == MoveType.PROMOTION || type == MoveType.CAPTURE_PROMOTION) {
			Piece promoted = move.getPromotionPiece();
			if(promoted != null) {
				score += promoted.getValue() - 1;
			}
		}
		
		return score;
	}

	/**
	 * Vergleicht zwei Spielzüge anhand ihrer Bewertung.
	 * 
	 * @param move1
	 *            der erste Spielzug
	 * @param move2
	 *            der zweite Spielzug
	 * @return eine negative Zahl, wenn move1 eine niedrigere Bewertung als move2
	 *         hat; eine positive Zahl, wenn move1 eine höhere Bewertung als move2
	 *         hat; 0, wenn beide Züge die gleiche Bewertung haben
	 */
	@Override
	public int compare(Move move1, Move move2) {
		// TODO: Aufgabe 5.2
		//Check if the parameter is null
		if(move1.equals(null) || move2.equals(null)) {
			throw new IllegalArgumentException("Both parameter can't be null!");
		}
		
		//comparing two move with scoreMove method from Aufgabe 5.1
		if(scoreMove(move1) > scoreMove(move2)) {
			return 1; // 1 means move1 "bigger" than move2
		} else if(scoreMove(move1) < scoreMove(move2)) {
			return -1; // -1 means move 1 "smaller" than move 2
		} else {
			return 0; //if not, then move1 and move2 is equal and we return 0
		}
	}
}
