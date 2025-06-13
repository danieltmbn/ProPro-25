/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.fen;

/**
 * Eine Exception, die geworfen wird, wenn die Eingabe des {@link FENParser}s
 * fehlerhaft ist, sich also nicht an die FEN hält
 */
public class FENFormatException extends Exception {

	/**
	 * Erstellt eine neue {@link FENFormatException} mit der angegebenen Nachricht
	 * 
	 * @param message
	 *            die Nachricht der Exception
	 */
	public FENFormatException(String message) {
		super(message);
	}

}
