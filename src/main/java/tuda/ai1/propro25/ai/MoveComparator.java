/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.ai;

import java.util.Comparator;
import tuda.ai1.propro25.model.Move;

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
	 * @param move
	 *            der zu bewertende Zug
	 * @return die Bewertung dieses Zugs
	 */
	int scoreMove(Move move) {
		// TODO: Aufgabe 5.1
		return 0;
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
		return 0;
	}
}
