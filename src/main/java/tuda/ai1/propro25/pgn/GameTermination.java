/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.pgn;

/**
 * Information darüber, wie das Spiel laut PGN verbleibt. Das Spiel muss nicht
 * abgeschlossen sein und kann weitergespielt werden.
 */
public enum GameTermination {

	/**
	 * Das Spiel ist noch im Gang und kann fortgesetzt werden
	 */
	IN_PROGRESS,
	/**
	 * Das Spiel ist unentschieden (z.B. Patt)
	 */
	DRAW,
	/**
	 * Weiß hat das Spiel gewonnen
	 */
	WHITE_WINS,
	/**
	 * Schwarz hat das Spiel gewonnen
	 */
	BLACK_WINS;

	@Override
	public String toString() {
		return switch (this) {
			case IN_PROGRESS -> "*";
			case DRAW -> "1/2-1/2";
			case WHITE_WINS -> "1-0";
			case BLACK_WINS -> "0-1";
		};
	}
}
