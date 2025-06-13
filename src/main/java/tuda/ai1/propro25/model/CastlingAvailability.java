/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.model;

/**
 * Die Rochadenrechte beider Spieler basierend darauf, ob König und Turm schon
 * bewegt wurden. Hier wird nicht berücksichtigt, ob Rochaden temporär durch
 * andere Umstände blockiert sind (Schach usw.)
 *
 * @param whiteCastleKingSide
 *            Weiß kann kurz rochieren
 * @param whiteCastleQueenSide
 *            Weiß kann lang rochieren
 * @param blackCastleKingSide
 *            Schwarz kann kurz rochieren
 * @param blackCastleQueenSide
 *            Schwarz kann lang rochieren
 */
public record CastlingAvailability(boolean whiteCastleKingSide, boolean whiteCastleQueenSide,
		boolean blackCastleKingSide, boolean blackCastleQueenSide) {

	/**
	 * Überprüft, ob einer der beiden Spieler irgendeine Rochade durchführen kann.
	 * 
	 * @return true genau dann, wenn mindestens ein Spieler mindestens eine Rochade
	 *         durchführen kann
	 */
	public boolean isAnyAvailable() {
		return whiteCastleKingSide || whiteCastleQueenSide || blackCastleKingSide || blackCastleQueenSide;
	}

	@Override
	public String toString() {
		return "Castling{" + "wK=" + whiteCastleKingSide + ", wQ=" + whiteCastleQueenSide + ", bK="
				+ blackCastleKingSide + ", bQ=" + blackCastleQueenSide + '}';
	}
}
