/* (C) 2025-2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.model.piece;

import java.util.List;
import java.util.Objects;
import tuda.ai1.propro25.model.*;

/**
 * Die abstrakte Elternklasse Piece repräsentiert eine Schachfigur
 */
public abstract class Piece {

	protected final Color color;
	protected final int value;

	/**
	 * Neue Figur die sich noch nicht bewegt hat
	 * 
	 * @param color
	 *            Farbe der Figur
	 * @param value
	 *            Wert dieser Figur (Chess piece value)
	 */
	public Piece(Color color, int value) {
		this.color = color;
		this.value = value;
	}

	/**
	 * Gibt alle möglichen pseudolegalen Züge für diese Figur vom angegebenen Feld
	 * zurück. Pseudolegale Züge sind alle Züge, die die Bewegungsmuster der Figur
	 * und Zustand der Felder beachten, allerdings nicht prüfen, ob dadurch z.B. der
	 * König im Schach steht etc
	 *
	 * @param currentCoordinate
	 *            Die aktuelle Position der Figur auf dem Schachbrett
	 * @param board
	 *            Das Brett auf dem sich diese Figur gerade befindet
	 * @return Eine Liste aller möglichen (pseudolegalen) Züge für diese Figur.
	 */
	public abstract List<Move> getPseudolegalMoves(Coordinate currentCoordinate, Board board);

	/**
	 * @return Farbe dieser Schachfigur
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Gibt das Symbol dieser Figure in der FEN Notation zurück
	 * 
	 * @return ein einzelner Buchstabe, der die Art und Farbe der Figur in der
	 *         FEN-Notation repräsentiert
	 */
	public char getFenSymbol() {
		char symbol = getAlgebraicNotationSymbol();
		if (color == Color.BLACK) {
			symbol = Character.toLowerCase(symbol);
		}

		return symbol;
	}

	/**
	 * Gibt das Symbol zurück, das diese Figur in der Standard Algebraic Notation
	 * repräsentiert. Im Unterschied zum FEN-Symbol sind diese immer großgeschrieben
	 * und enthalten keine Informationen über die Farbe.
	 *
	 * @return ein einzelner Buchstabe, der diese Figur in SAN-Notation
	 *         repräsentiert
	 */
	public abstract char getAlgebraicNotationSymbol();

	/**
	 * @return Wert dieser Figur (Chess Piece Value)
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Die Überprüfung, ob zwei Schachfiguren gleich sind, beinhaltet folgende
	 * Aspekte:
	 * <ul>
	 * <li>Haben beide Figuren die exakt gleiche Klasse?</li>
	 * <li>Haben beide Figuren die gleiche Farbe?</li>
	 * <li>Haben beide Figuren sich beide schonmal bewegt?</li>
	 * </ul>
	 *
	 * @param o
	 *            das andere Object, mit dem diese Schachfigur verglichen wird
	 * @return true genau dann, wenn diese und die übergebene Schachfigur anhand der
	 *         genannten Kriterien gleich sind
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Piece piece = (Piece) o;
		return color == piece.color;
	}

	@Override
	public int hashCode() {
		int result = Objects.hashCode(color);
		return 31 * result;
	}

	@Override
	public String toString() {
		return String.valueOf(getFenSymbol());
	}
}
