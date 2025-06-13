/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.model.piece;

import java.util.ArrayList;
import java.util.List;
import tuda.ai1.propro25.model.*;

/**
 * Die abstrakte Elternklasse SlidingPiece repräsentiert eine Schachfigur,
 * welche sich pro Zug beliebig weit in eine Richtung fortschieben kann
 */
public abstract class SlidingPiece extends Piece {

	/**
	 * Eine Schachfigur, welche sich pro Zug beliebig weit in eine Richtung
	 * fortbewegen kann
	 * 
	 * @param color
	 *            Farbe dieser Figur
	 * @param value
	 *            Wert dieser Figur (Chess Piece value)
	 */
	public SlidingPiece(Color color, int value) {
		super(color, value);
	}

	/**
	 * Generiert Züge, die das Verhalten der Schiebefiguren abbilden. Schiebefiguren
	 * können beliebig weit in eine Richtung gehen, solange keine andere Figur im
	 * Weg steht und sie das Brett nicht verlassen.
	 * 
	 * @param currentCoordinate
	 *            Die aktuelle Koordinate, auf der sich diese Schiebefigur befindet
	 * @param board
	 *            Das Brett auf der sie sich befindet
	 * @param slidingDirections
	 *            Array an Richtungen, in die sie sich bewegen kann. Dabei gibt
	 *            jedes Element {x,y} an, dass die Figur von ihrem Feld in die
	 *            Richtung gehen kann, die sich in +x und +y befindet.
	 * @return Liste an pseudolegalen Schiebezügen
	 */
	List<Move> getSlidingMoves(Coordinate currentCoordinate, Board board, int[][] slidingDirections) {
		var possibleMoves = new ArrayList<Move>();

		for (int[] direction : slidingDirections) {
			int x = currentCoordinate.getFile();
			int y = currentCoordinate.getRank();

			while (true) {
				x += direction[0];
				y += direction[1];

				var targetCoordinate = new Coordinate(x, y);

				if (!targetCoordinate.isOnBoard()) {
					// Diese Koordinate ist außerhalb des Spielfeldes und kann daher kein legales
					// Ziel eines Zuges sein
					break; // Wir brauchen nicht weiter in diese Richtung zu suchen
				}
				Piece pieceOnField = board.getPiece(targetCoordinate);
				if (pieceOnField != null) {
					if (pieceOnField.getColor() != color) {
						possibleMoves.add(
								new Move(this, currentCoordinate, targetCoordinate, MoveType.CAPTURE, pieceOnField));
					}
					break; // Hindernis, wir brauchen nicht weiter in diese Richtung zu suchen
				} else {
					possibleMoves.add(new Move(this, currentCoordinate, targetCoordinate));
				}
			}
		}
		return possibleMoves;
	}

}
