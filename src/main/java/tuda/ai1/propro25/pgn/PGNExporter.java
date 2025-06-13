/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.pgn;

import java.io.PrintWriter;
import java.io.Writer;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import tuda.ai1.propro25.fen.FENRecord;
import tuda.ai1.propro25.fen.FENSerializer;
import tuda.ai1.propro25.model.*;
import tuda.ai1.propro25.model.history.HistoricalBoardState;
import tuda.ai1.propro25.model.history.HistoricalGame;
import tuda.ai1.propro25.pgn.move.Checking;

/**
 * Exportiert einen Spielzustand in das PGN Format
 */
public class PGNExporter {

	private final PrintWriter output;

	/**
	 * Erstellt einen neuen PGNExporter, der in den Writer schreibt
	 *
	 * @param output
	 *            der Writer, in den der Export geschrieben werden soll
	 */
	public PGNExporter(Writer output) {
		this.output = new PrintWriter(output);
	}

	/**
	 * Exportiert das gesamte Spiel
	 *
	 * @param game
	 *            das zu exportierende Spiel
	 */
	public void exportGame(HistoricalGame game) {
		FENRecord startState = game.board().getNonStandardStartState();

		writeMandatoryTags(game);

		if (startState != null) {
			writeStartState(startState);
		}

		output.println();
		output.println();
		writeMoveText(game.board());
		writeGameResult(game);
	}

	/**
	 * Schreibt die sieben verpflichtenden Tags passend zum Spiel
	 *
	 * @param game
	 *            das Spiel, für welches die sieben Tags geschrieben werden soll
	 */
	private void writeMandatoryTags(HistoricalGame game) {
		Player[] players = game.board().getPlayers();
		Player winner = game.board().getWinner();

		writeTag("Event", game.gameName());
		writeTag("Site", "AI1 ProPro");
		writeTag("Date", game.time().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
		writeTag("Round", "1"); // wir spielen immer nur eine Runde
		writeTag("White", players[0].getName());
		writeTag("Black", players[1].getName());

		String result;
		if (winner != null) {
			result = winner.getColor() == Color.WHITE ? "1-0" : "0-1";
		} else if (game.board().getGameState() == GameState.RUNNING
				|| game.board().getGameState() == GameState.PAUSED) {
			result = "*";
		} else {
			result = "1/2-1/2";
		}

		writeTag("Result", result);
	}

	/**
	 * Schreibt den Anfangszustand des Bretts in FEN
	 *
	 * @param startState
	 *            FEN-Record des Startzustands
	 */
	private void writeStartState(FENRecord startState) {
		String fen = new FENSerializer().serializeRecord(startState);

		writeTag("SetUp", "1");
		writeTag("FEN", fen);
	}

	/**
	 * Schreibt einen Tag und erzeugt einen Zeilenumbruch
	 *
	 * @param tagName
	 *            name des Tags
	 * @param value
	 *            Wert des Tags (noch ohne Anführungszeichen)
	 */
	void writeTag(String tagName, String value) {
		String escapedString = value.replace("\"", "\\\"");

		output.print('[');
		output.print(tagName);
		output.print(" \"");
		output.print(escapedString);
		output.println("\"]");
	}

	/**
	 * Schreibt die Liste der Züge mit Nummerierung
	 *
	 * @param board
	 *            das Brett, das die Zughistorie hält
	 */
	public void writeMoveText(Board board) {
		List<HistoricalBoardState> states = board.getHistory();

		if (states.isEmpty()) {
			return;
		}

		Iterator<HistoricalBoardState> iterator = states.iterator();
		Iterator<HistoricalBoardState> nextIterator = states.iterator();
		nextIterator.next(); // dieser Iterator ist uns immer einen Schritt voraus

		int firstMoveIdx;
		if (states.get(0).isIncomplete()) {
			// Wenn erster Zug double-pawn ist, welches der erste unvollständig bekannte Zug
			// ist.
			// Dieser Zug wird dann übersprungen, weil er bereits vom Start FEN abgedeckt
			// ist.
			iterator.next();

			if (states.size() <= 1) {
				// Es gibt nur diesen unvollständigen Zug, der bereits vom Start FEN abgedeckt
				// ist
				return;
			}

			nextIterator.next();
			firstMoveIdx = 1; // der erste Zug ist übersprungen
		} else {
			firstMoveIdx = 0;
		}

		if (states.get(firstMoveIdx).getPlayerIndex() == 1) {
			// der erste Zug wurde von Schwarz gemacht
			HistoricalBoardState first = iterator.next();
			output.print(first.getFullMoveClock());
			output.print("..."); // drei Punkte bedeuten, dass jetzt ein schwarzer Zug kommt

			AlgebraicNotationMoveGenerator.writeHalfMove(first, getChecking(board, nextIterator), output);

			output.print(' ');
		}

		while (iterator.hasNext()) {
			HistoricalBoardState whiteMoveState = iterator.next();

			output.print(whiteMoveState.getFullMoveClock());
			output.print('.');

			AlgebraicNotationMoveGenerator.writeHalfMove(whiteMoveState, getChecking(board, nextIterator), output);

			if (iterator.hasNext()) {
				output.print(' ');
				HistoricalBoardState blackMoveState = iterator.next();

				AlgebraicNotationMoveGenerator.writeHalfMove(blackMoveState, getChecking(board, nextIterator), output);
			}

			output.print(' ');
		}
	}

	/**
	 * Überprüft, ob der nächste Zustand einen im Schach stehenden König enthält.
	 * Der Iterator wird um eins voranbewegt.
	 *
	 * @param board
	 *            das Brett (benötigt für Schachmatt)
	 * @param nextIterator
	 *            der Iterator, der immer auf den nächsten Zustand zeigt
	 * @return ob im nächsten Zustand ein Schach besteht
	 */
	private Checking getChecking(Board board, Iterator<HistoricalBoardState> nextIterator) {
		if (!nextIterator.hasNext()) {
			// das war der letzte Zug, wir müssen das Board fragen
			if (board.getGameState() == GameState.END_CHECKMATE) {
				// letzter Zug hat zum Schachmatt geführt
				return Checking.CHECKMATE;
			} else if (board.getColorInCheck() != null) {
				return Checking.CHECK;
			}

			return Checking.NONE;
		}

		return nextIterator.next().getColorInCheck() != null ? Checking.CHECK : Checking.NONE;

	}

	/**
	 * Schreibt ans Ende des PGN Spiels, wie das Spiel ausgegangen bzw. verblieben
	 * ist.
	 * 
	 * @param game
	 *            das Spiel (im Gange oder mit Ausgang)
	 */
	void writeGameResult(HistoricalGame game) {
		Player winner = game.board().getWinner();

		if (winner != null) {
			String winnerString = winner.getColor() == Color.WHITE ? "1-0" : "0-1";
			output.print(winnerString);
			return;
		}

		GameState state = game.board().getGameState();
		if (state == GameState.RUNNING || state == GameState.PAUSED) {
			output.print("*");
		} else {
			output.print("1/2-1/2");
		}
	}

}
