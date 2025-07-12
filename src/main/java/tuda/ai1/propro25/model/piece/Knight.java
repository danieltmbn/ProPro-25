/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.model.piece;

import java.util.ArrayList;
import java.util.List;
import tuda.ai1.propro25.model.*;

/**
 * Ein Knight ist ein Springer, eine der Schachfiguren und erbt von Piece
 * (Figur)
 */
public class Knight extends Piece {
	
	// TODO: Aufgabe 2.1
	/**
	 * Ein Knight ist ein Springer, eine der Schachfiguren und erbt von SlidingPiece
	 * (Schiebefigur) und damit auch von Piece (Figur)
	 * 
	 * @param color Die Farbe des Figurs
	 */
	public Knight(Color color) {
		super(color, 3);
	}

	// TODO: Aufgabe 2.2
	/**
	 * Gibt das Alfebraic Notation des Figur
	 * 
	 * @return liefert ein Buchstabe, der das Figur representiert
	 */
	@Override
	public char getAlgebraicNotationSymbol() {
		return 'N';
	}
	
	/**
	 * Eine Implementierung der abtrakte Methode, die alle moegliche Zuege liefert.
	 * @return liefrt alle moegliche Zuege des Figurs. Es ist ein Override von ein abstrakte Methode der abtrakte Klasse Piece
	 */
	@Override
	public List<Move> getPseudolegalMoves(Coordinate currentCoordinate, Board board) {
		// TODO: Aufgabe 2.3
		List<Move> possibleMove = new ArrayList<Move>();
		//Wenn du dir vorstellst, dass Knight in "+" sich bewegt, dann kannst du die Directions
		//in zwei unterschiedliche Richtung zerlegt. Eine horizontale eine vertikale Directions.
		//Und von dem wird das "L" Shape weitergemacht.
		int [][] horizontalDirections = {{-1, 1}, {-1, -1}, {1, 1}, {1, -1}};
		int [][] vertikalDirections = {{-1, 1}, {-1, -1}, {1, 1}, {1, -1}};
		
		//Fuer horizontale Richtung muss sich Knight zwei mal in x Richtung und ein mal in y Richtung bewegen
		for(int [] direction : horizontalDirections) {
			int xh = currentCoordinate.getFile() + 2 * direction[0];
			int yh = currentCoordinate.getRank() + direction[1];
			
			//Ueberprueft ob dem Pseudolegal Zelle ggbf. hat Schachfigur oder nicht. 
			var targetCoordinate = new Coordinate(xh, yh);
			if(targetCoordinate.isOnBoard()) {
				Piece pieceOnField = board.getPiece(targetCoordinate);
				
				if(pieceOnField == null) {
					possibleMove.add(new Move(this, currentCoordinate, targetCoordinate));
				}else if(pieceOnField.getColor() != color) { //Check ob das Schachfigur die gleiche Farbe. Wenn nicht, kann man schlagen
					possibleMove.add(new Move(this, currentCoordinate, targetCoordinate, MoveType.CAPTURE, pieceOnField));
				}
			}
		}
		
		//Fuer vertikale Richtung muss sich Knight zwei mal in y Richtung und ein mal in x Richtung bewegen
		for(int [] direction : vertikalDirections) {
			int xv = currentCoordinate.getFile() + direction[0];
			int yv = currentCoordinate.getRank() + 2 * direction[1];
			
			//Ueberprueft ob dem Pseudolegal Zelle ggbf. hat Schachfigur oder nicht.
			var targetCoordinate = new Coordinate(xv, yv);
			if(targetCoordinate.isOnBoard()) {
				Piece pieceOnField = board.getPiece(targetCoordinate);
				
				if(pieceOnField == null) {
					possibleMove.add(new Move(this, currentCoordinate, targetCoordinate));
				}else if(pieceOnField.getColor() != color) { //Check ob das Schachfigur die gleiche Farbe. Wenn nicht, kann man schlagen
					possibleMove.add(new Move(this, currentCoordinate, targetCoordinate, MoveType.CAPTURE, pieceOnField));
				}
			}
		}
		return possibleMove;
	}
}
