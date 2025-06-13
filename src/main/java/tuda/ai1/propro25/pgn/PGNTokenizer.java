/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.pgn;

import static tuda.ai1.propro25.pgn.PGNToken.Type.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Wird vom PGNParser genutzt, um eine Zeichenkette (String) in einen Strom an
 * Tokens zu zerlegen. Muss für jede Eingabe neu erstellt werden.
 */
class PGNTokenizer {

	private static final char END_CHAR = '\0'; // NULL wird nicht verwendet

	private final String input;
	private final StringBuilder spelling;
	private int index = 0;
	private char currentChar;

	/**
	 * Erstellt einen neuen PGNTokenizer, der für input einen Token-Strom
	 * bereitstellt
	 * 
	 * @param input
	 *            die Eingabe
	 */
	PGNTokenizer(String input) {
		this.input = input;
		this.spelling = new StringBuilder();
		this.currentChar = input.isEmpty() ? END_CHAR : input.charAt(index++); // erstes Zeichen einlesen
	}

	/**
	 * Liest so lange Tokens, bis ein EOF kommt. Ein EOF token wird zurückgegeben.
	 * 
	 * @return alle gelesenen Tokens inklusive erstem EOF
	 * @throws PGNParseException
	 *             falls die Eingabe lexikalische Fehler enthält
	 */
	List<PGNToken> readAllTokens() throws PGNParseException {
		var tokens = new ArrayList<PGNToken>();

		PGNToken token;
		do {
			token = nextToken();
			tokens.add(token);
		} while (token.type() != EOF);

		return tokens;
	}

	/**
	 * Liest das nächste Token. Falls bereits die gesamte Eingabe bearbeitet wurde,
	 * wird ein EOF-Token zurückgegeben.
	 * 
	 * @return das nächste Token
	 * @throws PGNParseException
	 *             falls die Eingabe lexikalische Fehler enthält
	 */
	PGNToken nextToken() throws PGNParseException {
		discardWhiteSpacesAndComments();

		if (!hasCharsLeft()) {
			return new PGNToken(EOF, "");
		}

		// StringBuilder zurücksetzen für nächste Token
		spelling.setLength(0);

		return switch (currentChar) {
			case '"' -> scanString();
			case '.' -> createTokenAndConsume(DOT);
			case '*' -> createTokenAndConsume(ASTERISK);
			case '[' -> createTokenAndConsume(TAG_OPEN);
			case ']' -> createTokenAndConsume(TAG_CLOSE);
			case '$' -> scanNAG();
			default -> {
				if (isLetter(currentChar) || isDigit(currentChar)) {
					yield scanSymbol(); // int, symbol
				} else {
					throw new PGNParseException("Illegales Zeichen zu Begin eines Tokens: '" + currentChar
							+ "' (codepoint " + (int) currentChar + ")");
				}
			}
		};
	}

	/**
	 * @return ein Symbol Token bzw. Integer, wenn das Symbol nur aus Ziffern
	 *         besteht
	 */
	private PGNToken scanSymbol() {
		boolean isInteger = true;
		while (hasCharsLeft()) {
			if (isDigit(currentChar)) {
				consume();
			} else if (isSymbolChar(currentChar)) {
				consume();
				isInteger = false; // wir haben ein Zeichen gesehen, dass keine Ziffer ist
			} else {
				break;
			}
		}

		if (isInteger) {
			return createToken(INTEGER);
		}

		String spelling = this.spelling.toString();
		PGNToken.Type type = switch (spelling) {
			case "1-0" -> WHITE_WINS;
			case "0-1" -> BLACK_WINS;
			case "1/2-1/2" -> DRAW;
			default -> SYMBOL;
		};

		return new PGNToken(type, spelling);
	}

	/**
	 * Übersprint alle Leerstellen, Kommentare und Variations-Annotationen (RAVs)
	 * Diese Informationen sind fürs Parsen irrelevant.
	 */
	private void discardWhiteSpacesAndComments() throws PGNParseException {
		while (hasCharsLeft()) {
			if (Character.isWhitespace(currentChar)) {
				consume(); // alle Leerzeichen etc. verwerfen
			} else if (currentChar == ';') {
				discardLineComment();
			} else if (currentChar == '{') {
				discardBlockComment();
			} else if (currentChar == '(') {
				discardRAV();
			} else {
				break;
			}
		}
	}

	/**
	 * Überspringt alle Zeichen bis zum Ende der Zeile
	 */
	private void discardLineComment() throws PGNParseException {
		expect(';', "Ein Zeilen-Kommentar muss mit ';' beginnen");
		while (hasCharsLeft()) {
			if (currentChar == '\n') {
				consume(); // terminator auch konsumieren
				break;
			}

			consume();
		}
	}

	/**
	 * Überspringt alle Zeichen bis zum schließenden '}'
	 */
	private void discardBlockComment() throws PGNParseException {
		expect('{', "Ein Block-Kommentar muss mit '{' beginnen");
		while (hasCharsLeft()) {
			if (currentChar == '}') {
				break;
			}

			consume();
		}

		expect('}', "Block-Kommentar muss mit '}' geschlossen werden");
	}

	/**
	 * Überspringt (geschachtelte) RAVs
	 */
	private void discardRAV() throws PGNParseException {
		// RAVs sind Kommentare, in denen ein Annotator Variationen vorschlagen kann
		// sie haben fürs Importieren keinen Nutzen, sollten aber verworfen werden, weil
		// einige
		// PGNs im Internet solche enthalten

		expect('(', "Eine rekursive Variations-Annotation muss mit '(' beginnen");
		int level = 1; // wir haben eine Klammer gelesen, also 1

		while (hasCharsLeft() && level > 0) {
			if (currentChar == '(') {
				level++;
			} else if (currentChar == ')') {
				level--;
			}

			consume();
		}

		if (level > 0) {
			throw new PGNParseException("Nicht geschlossenes '('");
		}
	}

	/**
	 * @return den erkannten String
	 */
	private PGNToken scanString() throws PGNParseException {
		expect('"', "Startendes '\"' eines Strings erwartet"); // " konsumieren

		StringBuilder stringValue = new StringBuilder();
		boolean escape = false;
		while (hasCharsLeft()) {
			if (currentChar == '"' && !escape) {
				break;
			} else if (currentChar == '\\' && !escape) {
				escape = true;
			} else {
				if (escape && currentChar != '"' && currentChar != '\\') {
					throw new PGNParseException("Illegales Zeichen Escape-Zeichen nach '\\': '" + currentChar + "'");
				}

				if (currentChar == '\n' || currentChar == '\t' || currentChar == '\r') {
					throw new PGNParseException("Illegales Zeichen in String: '" + currentChar + "'");
				}

				stringValue.append(currentChar);
				escape = false;
			}

			consume();
		}

		expect('"', "Schließendes '\"' eines Strings erwartet");

		return new PGNToken(STRING, stringValue.toString());
	}

	/**
	 * @return den erkannten NAG
	 */
	private PGNToken scanNAG() throws PGNParseException {
		expect('$', "'$' vor einem NAG erwartet"); // $ konsumieren
		// es gibt mindestens ein Zeichen ...
		if (!isDigit(currentChar)) { // ... das eine Ziffer ist
			throw new PGNParseException("Nach einem '$' muss mindestens eine Ziffer kommen");
		}

		do {
			consume(); // erste Ziffer auch konsumieren
		} while (hasCharsLeft() && isDigit(currentChar));

		return createToken(NAG);
	}

	/**
	 * Erwartet, dass das nächste Zeichen ein bestimmtes ist. Ist dies der Fall,
	 * wird dieses Zeichen konsumiert.
	 * 
	 * @param expected
	 *            das Zeichen, das jetzt erwartet wird
	 * @param message
	 *            die Nachricht, die bei einer Fehlermeldung verwendet werden soll
	 * @throws PGNParseException
	 *             falls das nächste Zeichen nicht das Erwartete ist
	 */
	private void expect(char expected, String message) throws PGNParseException {
		if (currentChar == expected) {
			consume();
		} else {
			throw new PGNParseException(message);
		}
	}

	/**
	 * Liest das nächste Zeichen. Wenn es keins mehr gibt, wird das NULL Zeichen
	 * gelesen.
	 */
	private void consume() {
		// konsumiertes Zeichen ist also Teil vom nächsten Token
		spelling.append(currentChar);

		if (index < input.length()) {
			currentChar = input.charAt(index++);
		} else {
			currentChar = END_CHAR;
		}
	}

	/**
	 * @return ob es noch Zeichen zu erkennen gibt
	 */
	private boolean hasCharsLeft() {
		return currentChar != END_CHAR;
	}

	/**
	 * Konsumiert ein Zeichen und erstellt ein Token
	 * 
	 * @param type
	 *            der Typ des zu erstellenden Token
	 * @return der neue Token mit dem gegebenen Typ und dem aktuellen Spelling
	 */
	private PGNToken createTokenAndConsume(PGNToken.Type type) {
		consume();
		return createToken(type);
	}

	/**
	 * Erstellt ein Token mit dem angegebenen Typen und dem aktuellen Spelling.
	 * 
	 * @param type
	 *            der Typ des zu erstellenden Token
	 * @return der neue Token mit dem gegebenen Typ und dem aktuellen Spelling
	 */
	private PGNToken createToken(PGNToken.Type type) {
		return new PGNToken(type, spelling.toString());
	}

	/**
	 * @param c
	 *            zu prüfendes Zeichen
	 * @return ob das Zeichen Teil eines Symbols im Sinne der PGN Spezifikation ist
	 */
	private boolean isSymbolChar(char c) {
		return isLetter(c) || isDigit(c) || c == '_' || c == '+' || c == '#' || c == '=' || c == ':' || c == '-'
				|| c == '/';
	}

	/**
	 * @param c
	 *            zu prüfendes Zeichen
	 * @return ob das Zeichen ein alpha-Buchstabe ist
	 */
	private boolean isLetter(char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
	}

	/**
	 * @param c
	 *            zu prüfendes Zeichen
	 * @return ob das Zeichen ine Ziffer zwischen 0 und 9 ist
	 */
	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}
}
