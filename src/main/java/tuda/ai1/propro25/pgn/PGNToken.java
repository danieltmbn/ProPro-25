/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.pgn;

/**
 * Repräsentiert einen Token eines PGN Texts
 * 
 * @param type
 *            der Typ dieses Token
 * @param spelling
 *            die genauen Zeichen, aus denen dieses Token aus der Quelle
 *            entstanden ist
 */
record PGNToken(Type type, String spelling) {

	/**
	 * Ein Typ eines Tokens
	 */
	enum Type {
		/**
		 * '*'
		 */
		ASTERISK,

		/**
		 * '0-1'
		 */
		BLACK_WINS,

		/**
		 * '.'
		 */
		DOT,

		/**
		 * '1/2-1/2'
		 */
		DRAW,

		/**
		 * Ganzzahl
		 */
		INTEGER,

		/**
		 * '$XXX'
		 */
		NAG,

		/**
		 * mit "" umschlossener Text
		 */
		STRING,

		/**
		 * Kette bestehend aus Buchstaben, Zahlen und ein paar Sonderzeichen
		 */
		SYMBOL,

		/**
		 * ']'
		 */
		TAG_CLOSE,

		/**
		 * '['
		 */
		TAG_OPEN,

		/**
		 * '1-0'
		 */
		WHITE_WINS,

		/**
		 * Ende der Eingabedatei (end of file)
		 */
		EOF

	}

}
