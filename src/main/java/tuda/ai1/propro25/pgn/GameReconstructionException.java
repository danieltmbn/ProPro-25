/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.pgn;

/**
 * Eine Exception, die geworfen wird, wenn ein gespeichertes Spiel nicht
 * rekonstruiert werden kann. Das kann mehrere Gründe haben wie u.a.:
 * <ul>
 * <li>Der angegebene Zug ist nicht möglich</li>
 * <li>Der angegebene Zug ist nicht eindeutig</li>
 * <li>Informationen über das Spielende sind inkonsistent</li>
 * </ul>
 */
public class GameReconstructionException extends Exception {

	public GameReconstructionException(String message) {
		super(message);
	}

	public GameReconstructionException(String message, Throwable cause) {
		super(message, cause);
	}

}
