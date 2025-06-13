/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.pgn.move;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tuda.ai1.propro25.model.Coordinate;
import tuda.ai1.propro25.pgn.PGNParseException;

/**
 * Diese Klasse ist dafür verantwortlich, in Standard Algebraischer Notation
 * (SAN) kodierte Züge im Kontext von PGN zu parsen. Für das Format siehe
 * <a href="https://www.chessprogramming.org/Algebraic_Chess_Notation">hier</a>.
 */
public class MoveParser {

	private static final Pattern MOVE_PATTERN = Pattern
			.compile("([RNBQK])?([a-h])?([1-8])?(x)?([a-h][1-8])(=[RNBQK])?([+#])?$");
	private static final Pattern CASTLING_PATTERN = Pattern.compile("O-O(-O)?([+#])?$");

	/**
	 * Parst einen Spielzug, der von input kodiert wird.
	 * 
	 * @param input
	 *            der zu parsende Eingabestring
	 * @return den geparsten Spielzug
	 * @throws PGNParseException
	 *             falls der Eingabestring keinen validen Spielzug enthält
	 */
	public PGNMove parseMove(String input) throws PGNParseException {
		Matcher castlingMatcher = CASTLING_PATTERN.matcher(input);
		if (castlingMatcher.matches()) {
			return parseCastlingMove(castlingMatcher);
		}

		Matcher matcher = MOVE_PATTERN.matcher(input);
		if (!matcher.matches()) {
			throw new PGNParseException("Der String '" + input + "' kodiert keinen Zug in algebraischer Notation");
		}

		char piece = parsePieceSymbol(matcher.group(1));
		int fromFile = parseOptionalFile(matcher.group(2));
		int fromRank = parseOptionalRank(matcher.group(3));
		boolean capture = matcher.group(4) != null;
		Coordinate to = parseSquare(matcher.group(5));
		Character promotionPiece = parsePromotion(matcher.group(6));
		Checking checking = parseChecking(matcher.group(7));

		if (piece == 'P' && fromFile == -1 && fromRank != -1) {
			throw new PGNParseException(
					"Züge von Bauern dürfen nur eine Reihenangabe haben, wenn auch die Spalte angegeben ist");
		}

		if (piece == 'P' && capture && fromFile == -1) {
			throw new PGNParseException("Bei einem schlagenden Bauern-Zug muss immer die Startspalte angegeben sein");
		}

		if (promotionPiece != null && piece != 'P') {
			throw new PGNParseException("Nur Bauern dürfen eine Promotion durchführen");
		}

		return new NormalPGNMove(piece, fromFile, fromRank, to, capture, promotionPiece, checking);
	}

	/**
	 * Parst eine Rochade
	 * 
	 * @param matcher
	 *            der Matcher, der die Rochade gematcht hat
	 * @return den geparsten Rochaden-Zug
	 */
	private CastlingPGNMove parseCastlingMove(Matcher matcher) {
		String queenSide = matcher.group(1);
		String checkingString = matcher.group(2);

		return new CastlingPGNMove(queenSide == null, parseChecking(checkingString));
	}

	/**
	 * Parst das Figurensymbol, das vom Eingabestring kodiert wird.
	 * 
	 * @param symbol
	 *            der Eingabestring
	 * @return das kodierte Figurensymbol
	 */
	private char parsePieceSymbol(String symbol) {
		if (symbol == null) {
			return 'P';
		}

		// Wir können uns darauf verlassen, dass dieser Buchstabe ein Symbol ist,
		// wenn der Eingabestring immer aus der Regex-Match kommt.
		return symbol.charAt(0);
	}

	/**
	 * @param file
	 *            kodierte Spalte oder null
	 * @return die 0-basierte Spalte oder -1, falls Eingabe null
	 */
	private int parseOptionalFile(String file) {
		return file == null ? -1 : file.charAt(0) - 'a';
	}

	/**
	 * @param rank
	 *            kodierte Reihe oder null
	 * @return die 0-basierte Reihe oder -1, falls Eingabe null
	 */
	private int parseOptionalRank(String rank) {
		return rank == null ? -1 : rank.charAt(0) - '1';
	}

	/**
	 * @param square
	 *            das Feld kodiert in SAN
	 * @return das vollständige Feld, das von dem Eingabestring kodiert ist
	 */
	private Coordinate parseSquare(String square) {
		int file = square.charAt(0) - 'a';
		int rank = square.charAt(1) - '1';
		return new Coordinate(file, rank);
	}

	/**
	 * @param promotion
	 *            die kodierte Promotion oder null
	 * @return das Figurensymbol, zu dem promoted wird oder null, falls der
	 *         Eingabestring null ist
	 */
	private Character parsePromotion(String promotion) {
		if (promotion == null) {
			return null;
		}

		// an Position 0 ist das '='
		return promotion.charAt(1);
	}

	/**
	 * @param checkingString
	 *            die Schachmöglichkeiten oder null, falls keine
	 * @return die im Eingabestring kodiert Schachmöglichkeit oder
	 *         {@link Checking#NONE}, falls null
	 */
	private Checking parseChecking(String checkingString) {
		if (checkingString == null) {
			return Checking.NONE;
		}

		return switch (checkingString) {
			case "+" -> Checking.CHECK;
			case "#" -> Checking.CHECKMATE;
			default -> throw new RuntimeException(
					"Darf nicht passieren! '" + checkingString + "' kodiert keine Schachinformationen");
		};
	}

}
