/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.fen;

import tuda.ai1.propro25.model.Board;

/**
 * Instanzen dieser Klasse sind dafür verantwortlich, einen {@link FENRecord} in
 * einen FEN-String zu serialisieren.
 *
 * @see FENSerializer#serializeRecord(FENRecord)
 */
public class FENSerializer {

	/**
	 * Serialisiert den übergebenen {@link FENRecord} zu einem vollständigen
	 * FEN-String. Es werden nur Bretter mit den Abmessungen
	 * {@link Board#BOARD_SIZE} x {@link Board#BOARD_SIZE} unterstützt.
	 *
	 * @param record
	 *            der zu serialisierende Record
	 * @return den Record in FEN
	 */
	public String serializeRecord(FENRecord record) {
		// TODO: Aufgabe 3.5
		return "Noch nicht implementiert";
	}

	// TODO: Aufgabe 3.1

	// TODO: Aufgabe 3.2

	// TODO: Aufgabe 3.3

	// TODO: Aufgabe 3.4

}
