/* (C) 2025-2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.model;

/**
 * Repräsentiert die verschiedenen Arten von Zügen die es geben kann
 */
public enum MoveType {
	/**
	 * Für alle Standardzüge ohne Nebeneffekte
	 */
	NORMAL,
	/**
	 * Für Bauern, die vom Startfeld aus 2 Felder voranschreiten
	 */
	DOUBLEPAWN,
	/**
	 * Für Rochade auf der Königsseite (kurz)
	 */
	CASTLING_KINGSIDE,
	/**
	 * Für Rochaden auf der Damenseite (lang)
	 */
	CASTLING_QUEENSIDE,
	/**
	 * Für das Schlagen von Figuren mittels eines ansonsten normalen Zuges
	 */
	CAPTURE,
	/**
	 * Für das Schlagen mittels en passant (Bauern)
	 */
	EN_PASSANT,
	/**
	 * Für Bauern, die die letzte Zeile erreichen und sich nun umwandeln, dabei aber
	 * keine Figur geschlagen haben
	 */
	PROMOTION,
	/**
	 * FÜr Bauern, die die letzte Zeile erreichen, indem sie eine andere Figur
	 * schlagen und sich direkt umwandeln
	 */
	CAPTURE_PROMOTION
}
