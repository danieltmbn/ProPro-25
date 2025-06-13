/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.pgn;

import static tuda.ai1.propro25.pgn.PGNToken.Type.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import tuda.ai1.propro25.pgn.move.MoveParser;
import tuda.ai1.propro25.pgn.move.PGNMove;

/**
 * Parst PGN kodierte Spiele Siehe
 * <a href="https://www.thechessdrum.net/PGN_Reference.txt">Spezifikation</a>.
 * Hierbei handelt es sich um einen Top-Down-Parser, der den PGNTokenizer
 * verwendet.
 */
public class PGNParser {

	private final MoveParser moveParser;
	private final PGNTokenizer tokenizer;
	private PGNToken currentToken;

	/**
	 * Erstellt einen neuen PGNParser, der die gegebene Eingabe parsen kann
	 * 
	 * @param input
	 *            die zu parsende Eingabe
	 */
	private PGNParser(String input) {
		this.moveParser = new MoveParser();
		this.tokenizer = new PGNTokenizer(input);
	}

	public static PGNGame parse(String input) throws PGNParseException {
		PGNParser parser = new PGNParser(input);
		return parser.parseGame();
	}

	/**
	 * Parst das gesamte PGN Spiel
	 * 
	 * @return das geparste Spiel
	 * @throws PGNParseException
	 *             falls die Eingabe syntaktische Fehler enthält
	 */
	PGNGame parseGame() throws PGNParseException {
		this.currentToken = tokenizer.nextToken();

		Map<String, String> tags = new HashMap<>();
		while (currentToken.type() == TAG_OPEN) {
			String[] tag = parseTag();
			if (tags.put(tag[0], tag[1]) != null) {
				throw new PGNParseException("Der Tag '" + tag[0] + "' wurde mehrfach angegeben!");
			}
		}

		ArrayList<PGNMove> moves = new ArrayList<>();

		while (!isTerminationMarker(currentToken.type())) {
			moves.add(parseMove());
		}

		GameTermination termination = parseTermination();

		expect(EOF, "Überflüssige Tokens nach dem Spiel-Terminator");

		return new PGNGame(tags, moves, termination);
	}

	/**
	 * Parst einen Tag
	 * 
	 * @return Array, wobei an Index 0 der Name und an Index 1 der Wert des Tags ist
	 */
	private String[] parseTag() throws PGNParseException {
		expect(TAG_OPEN, "Am Anfang eines Tags muss ein '[' stehen");
		String tagName = expect(SYMBOL, "Nach dem '[' muss der Tag-Name stehen").spelling();
		String value = expect(STRING, "Nach dem Tag-Namen muss der Wert in '\"' stehen").spelling();
		expect(TAG_CLOSE, "Der Tag muss mit einem ']' geschlossen werden");

		return new String[]{tagName, value};
	}

	/**
	 * @return den geparsten Zug
	 */
	private PGNMove parseMove() throws PGNParseException {
		if (currentToken.type() == INTEGER) {
			// Die Zug-Nummer ist optional
			consume();
			// beim Importieren spielt es keine Rolle, wie viele ... hinter der Nummer
			// stehen
			discard(DOT);
		}

		String moveSpelling = expect(SYMBOL, "Hier muss ein Symbol für den Spielzug kommen").spelling();
		discard(NAG, STRING); // NAGs und String-Annotationen interessieren uns nicht

		return moveParser.parseMove(moveSpelling);
	}

	/**
	 * @return der geparste Terminierungs-Marker
	 */
	private GameTermination parseTermination() throws PGNParseException {
		GameTermination termination = switch (currentToken.type()) {
			case WHITE_WINS -> GameTermination.WHITE_WINS;
			case BLACK_WINS -> GameTermination.BLACK_WINS;
			case DRAW -> GameTermination.DRAW;
			case ASTERISK -> GameTermination.IN_PROGRESS;
			default -> error("Am Ende des Zugtexts muss '1-0', '0-1', '1/2-1/2' oder '*' kommen");
		};

		consume(); // Termination Token konsumieren

		return termination;
	}

	/**
	 * Verwirft so lange Tokens, wie das aktuelle Token eines der angegebenen ist
	 * 
	 * @param types
	 *            alle Typen von Token, die übersprungen werden sollen.
	 */
	private void discard(PGNToken.Type... types) throws PGNParseException {
		while (true) {
			boolean skip = false;
			for (PGNToken.Type type : types) {
				if (currentToken.type() == type) {
					skip = true;
					break;
				}
			}

			if (skip) {
				consume();
			} else {
				break;
			}
		}
	}

	/**
	 * Überprüft, ob das aktuelle Token mit dem erwarteten übereinstimmt. Ist dies
	 * der Fall, wird es konsumiert. Sonst wird eine Exception geworfen.
	 * 
	 * @param type
	 *            den Typen des Tokens, der erwartet wird
	 * @param message
	 *            die Fehlermeldung, falls es sich nicht um das Token handelt
	 * @return das konsumierte Token
	 * @throws PGNParseException
	 *             falls das aktuelle Token nicht dem Erwarteten entspricht
	 */
	private PGNToken expect(PGNToken.Type type, String message) throws PGNParseException {
		if (type == currentToken.type()) {
			return consume();
		} else {
			return error(message);
		}
	}

	/**
	 * Geht zum nächsten Token aus dem Token-Strom.
	 * 
	 * @return den dadurch konsumierten Token
	 * @throws PGNParseException
	 *             falls beim Lesen des nächsten Tokens ein lexikalischer Fehler
	 *             auftritt
	 */
	private PGNToken consume() throws PGNParseException {
		PGNToken previous = currentToken;
		currentToken = tokenizer.nextToken();
		return previous;
	}

	private boolean isTerminationMarker(PGNToken.Type type) {
		return type == WHITE_WINS || type == BLACK_WINS || type == DRAW || type == ASTERISK;
	}

	private <R> R error(String message) throws PGNParseException {
		throw new PGNParseException(message);
	}

}
