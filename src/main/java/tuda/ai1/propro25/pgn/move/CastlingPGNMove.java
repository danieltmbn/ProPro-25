/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.pgn.move;

/**
 * Ein unvollständiger Zug, der eine Rochade darstellt
 * 
 * @param kingSide
 *            true für kurze Rochade, false für lange
 * @param checking
 *            ob dieser Zug den König ins Schach stellt
 */
public record CastlingPGNMove(boolean kingSide, Checking checking) implements PGNMove {
	@Override
	public String toString() {
		return (kingSide ? "O-O" : "O-O-O") + checking;
	}
}
