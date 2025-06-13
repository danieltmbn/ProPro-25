/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.pgn.move;

/**
 * Information, ob der König ins Schach gestellt wird.
 */
public enum Checking {

	/**
	 * Der König wird nicht ins Schach gestellt.
	 */
	NONE,

	/**
	 * Der König wird ins Schach gestellt.
	 */
	CHECK,

	/**
	 * Der König wird ins Schachmatt gestellt.
	 */
	CHECKMATE;

	@Override
	public String toString() {
		return switch (this) {
			case NONE -> "";
			case CHECK -> "+";
			case CHECKMATE -> "#";
		};
	}
}
