/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.fen;

import tuda.ai1.propro25.model.Board;
import tuda.ai1.propro25.model.CastlingAvailability;
import tuda.ai1.propro25.model.Color;
import tuda.ai1.propro25.model.Coordinate;
import tuda.ai1.propro25.model.piece.*;

/**
 * Ein {@link FENParser} ist dafür verantwortlich, einen in der FEN kodierten
 * Spielstand zu parsen. Weitere Informationen zur FEN sind hier zu finden:
 * <a href="https://de.wikipedia.org/wiki/Forsyth-Edwards-Notation">Wikipedia
 * (Deutsch)</a>
 */
public class FENParser {

	/**
	 * Parst den gesamten FEN-Record. Die Eingabe muss vollständig syntaktisch
	 * korrekt sein, damit der Parser die Eingabe akzeptiert.
	 *
	 * @param encodedRecord
	 *            der gesamte FEN-Record als String kodiert
	 * @return der geparste {@link FENRecord}
	 * @throws FENFormatException
	 *             falls der übergebene String syntaktische Fehler enthält bzw.
	 *             keine gültige FEN enthält
	 */
	public FENRecord parseRecord(String encodedRecord) throws FENFormatException {
		if (encodedRecord == null) {
			throw new IllegalArgumentException("FEN Record ist null");
		}

		String[] fields = encodedRecord.split(" ");
		if (fields.length != 6) {
			throw new FENFormatException("FEN Record muss genau 6 durch Leerzeichen getrennte Felder enthalten");
		}

		Piece[][] board = parseBoard(fields[0]);
		Color color = parseColor(fields[1]);
		CastlingAvailability castlingAvailability = parseCastlingAvailability(fields[2]);
		Coordinate enPassantTarget = parseEnPassantTarget(fields[3]);
		int halfMoveClock = parseHalfMoveClock(fields[4]);
		int fullMoveClock = parseFullMoveClock(fields[5]);

		return new FENRecord(board, color, castlingAvailability, enPassantTarget, halfMoveClock, fullMoveClock);
	}

	/**
	 * Parst den FEN-kodierten Spielbrettzustand. Das zurückgegebene Array ist
	 * Spalten-basiert.
	 * 
	 * @param encoded
	 *            die Figurenanordnung in FEN
	 * @return ein 2D-Array, dass den aktuellen Spielbrettzustand repräsentiert
	 * @throws FENFormatException
	 *             falls der FEN-String keine valide Figurenstellung enthält
	 */
	Piece[][] parseBoard(String encoded) throws FENFormatException {
		String[] ranks = encoded.split("/");
		if (ranks.length != Board.BOARD_SIZE) {
			throw new FENFormatException("Der FEN Brettzustand muss genau " + Board.BOARD_SIZE
					+ " durch '/' getrennte Abschnitte enthalten");
		}

		Piece[][] board = new Piece[Board.BOARD_SIZE][Board.BOARD_SIZE];

		for (int rank = 0; rank < Board.BOARD_SIZE; rank++) {
			String encodedRank = ranks[Board.BOARD_SIZE - rank - 1]; // FEN starts with rank 8

			int file = 0;
			int stringIdx = 0;

			while (stringIdx < encodedRank.length()) {
				char c = encodedRank.charAt(stringIdx++);

				if (file >= Board.BOARD_SIZE) {
					throw new FENFormatException(
							"Zeile " + (rank + 1) + " enthält mehr als " + Board.BOARD_SIZE + " Figuren");
				}

				if (c >= '0' && c <= '9') {
					int skipCount = c - '0';
					file += skipCount;
				} else {
					Piece piece = parsePiece(c);
					board[file++][rank] = piece;
				}
			}

			if (file != Board.BOARD_SIZE) {
				throw new FENFormatException(
						"Zeile " + (rank + 1) + " enthielt weniger als " + Board.BOARD_SIZE + " Figuren");
			}
		}

		return board;
	}

	/**
	 * Parst eine Schachfigur, die auf dem Spielfeld platziert ist. Großbuchstaben
	 * kodieren weiße und Kleinbuchstaben schwarze Figuren
	 *
	 * @param c
	 *            das Zeichen, das die Schachfigur kodiert
	 * @return eine neue Instanz der korrekten Schachfigur mit der passenden Farbe
	 * @throws FENFormatException
	 *             falls das Zeichen keine Figur kodiert
	 */
	Piece parsePiece(char c) throws FENFormatException {
		return switch (c) {
			case 'P' -> new Pawn(Color.WHITE);
			case 'N' -> new Knight(Color.WHITE);
			case 'B' -> new Bishop(Color.WHITE);
			case 'R' -> new Rook(Color.WHITE);
			case 'Q' -> new Queen(Color.WHITE);
			case 'K' -> new King(Color.WHITE);

			case 'p' -> new Pawn(Color.BLACK);
			case 'n' -> new Knight(Color.BLACK);
			case 'b' -> new Bishop(Color.BLACK);
			case 'r' -> new Rook(Color.BLACK);
			case 'q' -> new Queen(Color.BLACK);
			case 'k' -> new King(Color.BLACK);

			default -> throw new FENFormatException("'" + c + "' kodiert keine Figur");
		};
	}

	/**
	 * Parst die Farbe des Spielers, der als Nächstes am Zug ist.
	 *
	 * @param encodedColor
	 *            entweder "w" oder "b"
	 * @return die kodierte Farbe des Spielers
	 * @throws FENFormatException
	 *             falls der String weder "w" noch "b" ist
	 */
	Color parseColor(String encodedColor) throws FENFormatException {
		if (encodedColor.equals("w")) {
			return Color.WHITE;
		} else if (encodedColor.equals("b")) {
			return Color.BLACK;
		} else {
			throw new FENFormatException(
					"'" + encodedColor + "' stellt keine Farbe da. Valide Farben sind 'w' und 'b'");
		}
	}

	/**
	 * Parst die Rochadenrechte, also ob Turm bzw. König schon bewegt wurden. Ob
	 * Rochaden temporär blockiert werden (z.B. Schach etc.), wird nicht
	 * berücksichtigt.
	 * 
	 * @param encoded
	 *            die FEN-kodierten Rochadenrechte
	 * @return die Rochadenrechte
	 * @throws FENFormatException
	 *             falls der String leer ist, länger als 4 Zeichen ist, illegale
	 *             Buchstaben enthält oder die Buchstaben in der falschen
	 *             Reihenfolge sind
	 */
	CastlingAvailability parseCastlingAvailability(String encoded) throws FENFormatException {
		if (encoded.isEmpty() || encoded.length() > 4) {
			throw new FENFormatException("Der Rochaden-String muss zwischen 1 und 4 Zeichen lang sein (inklusive)");
		}

		if (encoded.equals("-")) {
			return new CastlingAvailability(false, false, false, false);
		}

		boolean whiteCastleKingSide = false;
		boolean whiteCastleQueenSide = false;
		boolean blackCastleKingSide = false;
		boolean blackCastleQueenSide = false;

		int stringIdx = 0;
		if (encoded.charAt(stringIdx) == 'K') {
			whiteCastleKingSide = true;
			stringIdx++;
		}

		if (stringIdx < encoded.length() && encoded.charAt(stringIdx) == 'Q') {
			whiteCastleQueenSide = true;
			stringIdx++;
		}

		if (stringIdx < encoded.length() && encoded.charAt(stringIdx) == 'k') {
			blackCastleKingSide = true;
			stringIdx++;
		}

		if (stringIdx < encoded.length() && encoded.charAt(stringIdx) == 'q') {
			blackCastleQueenSide = true;
			stringIdx++;
		}

		if (stringIdx != encoded.length()) {
			throw new FENFormatException(
					"Rochaden-String konnte nicht geparst werden: Die " + (encoded.length() - stringIdx)
							+ " letzten Buchstaben sind nicht legal oder die Reihenfolge ist falsch");
		}

		return new CastlingAvailability(whiteCastleKingSide, whiteCastleQueenSide, blackCastleKingSide,
				blackCastleQueenSide);
	}

	/**
	 * Parst das Zielfeld eines En-Passant-Angriffs. Wenn der letzte Zug nicht von
	 * einem Bauern gemacht wurde und das Zielfeld mit "-" kodiert ist, wird null
	 * zurückgegeben
	 *
	 * @param encoded
	 *            "-" oder ein in algebraischer Notation kodiertes Feld
	 * @return die Koordinaten des En-Passant-Angriffs oder null
	 * @throws FENFormatException
	 *             falls der String nicht "-" oder korrekt kodierte Koordinaten
	 *             enthält
	 */
	Coordinate parseEnPassantTarget(String encoded) throws FENFormatException {
		if (encoded.equals("-")) {
			return null;
		}

		if (encoded.length() != 2) {
			throw new FENFormatException("Koordinaten in algebraischer Notation muss exakt zwei Zeichen lang sein");
		}

		int file = encoded.charAt(0) - 'a';
		int rank = encoded.charAt(1) - '0' - 1;

		if (file >= Board.BOARD_SIZE || file < 0 || rank >= Board.BOARD_SIZE || rank < 0) {
			throw new FENFormatException("Die Koordinate " + encoded + " ist nicht auf dem Brett");
		}

		return new Coordinate(file, rank);
	}

	/**
	 * Parst die Anzahl der Halbzüge, die seit dem letzten Bauernzug oder * Schlagen
	 * einer Figur vergangen sind
	 * 
	 * @param encoded
	 *            die Anzahl als String
	 * @return die Anzahl Halbzüge
	 * @throws FENFormatException
	 *             falls der String keine Zahl darstellt oder die Zahl negativ ist
	 */
	int parseHalfMoveClock(String encoded) throws FENFormatException {
		int number = parseNumber(encoded);
		if (number < 0) {
			throw new FENFormatException("Die Halbzug-Anzahl darf nicht negativ sein");
		}

		return number;
	}

	/**
	 * Parst die Anzahl der vollständigen Züge seit Spielbeginn der nächsten Runde
	 * 
	 * @param encoded
	 *            die Anzahl als String
	 * @return die Anzahl vollständiger Züge
	 * @throws FENFormatException
	 *             falls der String keine Zahl darstellt oder die Zahl negativ oder
	 *             0 ist
	 */
	int parseFullMoveClock(String encoded) throws FENFormatException {
		int number = parseNumber(encoded);
		if (number < 1) {
			throw new FENFormatException("Die Zug-Anzahl darf nicht negativ oder 0 sein");
		}

		return number;
	}

	/**
	 * Parst eine ganze Zahl
	 * 
	 * @param encoded
	 *            die ganze Zahl als String
	 * @return die ganze Zahl, die durch den String repräsentiert wurde
	 * @throws FENFormatException
	 *             falls der String keine ganze Zahl repräsentiert
	 */
	int parseNumber(String encoded) throws FENFormatException {
		try {
			return Integer.parseInt(encoded);
		} catch (NumberFormatException e) {
			throw new FENFormatException("Der String '" + encoded + "' enthält keine gültige Zahl");
		}
	}

}
