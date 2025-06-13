/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.pgn;

/**
 * Diese Exception wird geworfen, wenn ein PGN kodiertes Spiel aufgrund von
 * fehlerhafter Syntax nicht geparst werden kann.
 */
public class PGNParseException extends Exception {
	public PGNParseException(String message) {
		super(message);
	}
}
