/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.model;

/**
 * Repräsentiert den Zustand des aktuellen Spiels mit verschiedenen Kategorien.
 * Für Endzustände ist der Grund für das Ende des Spiels bereits beinhaltet.
 */
public enum GameState {
	/**
	 * Das Spielfeld ist bereit, aber die Uhren laufen noch nicht
	 */
	PAUSED,
	/**
	 * Da Spiel ist im Gange
	 */
	RUNNING,
	/**
	 * Das Spiel ist beendet, da ein Spieler Matt gesetzt wurde.
	 */
	END_CHECKMATE,
	/**
	 * Das Spiel ist beendet, da ein Spieler keine Zeit mehr hat
	 */
	END_TIMEOUT,
	/**
	 * Das Spiel ist beendet, da ein Spieler keine legalen Züge mehr hat, aber nicht
	 * im Schach steht (Patt)
	 */
	END_STALEMATE,
	/**
	 * Das Spiel ist beendet, da es nicht mehr möglich ist, ein Matt zu erzeugen
	 */
	END_MATERIAL,
	/**
	 * Das Spiel ist beendet, da eine dreifache Stellungswiederholung erzeugt wurde
	 */
	END_REPETITION,
	/**
	 * Das Spiel ist beendet, weil seit 50 Zügen kein Bauer bewegt oder eine Figur
	 * geschlagen wurde
	 */
	END_50MOVE,
	/**
	 * Das Spiel ist beendet, weil ein Spieler aufgegeben hat
	 */
	END_RESIGN,
	/**
	 * Das Spiel ist beendet, da sich die Spieler auf unentschieden geeinigt haben.
	 */
	END_AGREEMENT
}
