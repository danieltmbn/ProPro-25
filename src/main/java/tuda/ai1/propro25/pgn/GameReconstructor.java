/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.pgn;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import tuda.ai1.propro25.fen.FENFormatException;
import tuda.ai1.propro25.model.*;
import tuda.ai1.propro25.pgn.move.CastlingPGNMove;
import tuda.ai1.propro25.pgn.move.Checking;
import tuda.ai1.propro25.pgn.move.NormalPGNMove;
import tuda.ai1.propro25.pgn.move.PGNMove;

/**
 * Diese Klasse ist dafür verantwortlich, die geparsten PGN Spiele in
 * tatsächlich spielbare Spiele zu überführen. Dafür muss rekonstruiert werden,
 * ob die angegebenen Züge tatsächlich valide sind und ob auch sonst die
 * angegebenen Informationen zueinanderpassen.
 */
public class GameReconstructor {

	private final int whiteRemainingTime;
	private final int blackRemainingTime;

	/**
	 * Erstellt einen neuen {@link GameReconstructor} mit der spezifizierten
	 * Restzeit.
	 * 
	 * @param whiteRemainingTime
	 *            Restzeit für Weiß
	 * @param blackRemainingTime
	 *            Restzeit für Schwarz
	 */
	public GameReconstructor(int whiteRemainingTime, int blackRemainingTime) {
		this.whiteRemainingTime = whiteRemainingTime;
		this.blackRemainingTime = blackRemainingTime;
	}

	/**
	 * Rekonstruiert das PGN kodierte Spiel, sodass man daran weiterspielen kann
	 * 
	 * @param encodedPGN
	 *            das Spiel in PGN kodiert
	 * @return das rekonstruierte Spiel
	 * @throws PGNParseException
	 *             falls der Eingabestring syntaktische Fehler enthält
	 * @throws GameReconstructionException
	 *             falls das geparste Spiel nicht valide ist
	 */
	public Board reconstructGame(String encodedPGN) throws PGNParseException, GameReconstructionException {
		return reconstructGame(PGNParser.parse(encodedPGN));
	}

	/**
	 * Siehe {@link GameReconstructor#reconstructGame(String)}
	 */
	public Board reconstructGame(PGNGame game) throws GameReconstructionException {
		Board board = createInitialBoard(game);

		for (PGNMove move : game.moves()) {
			simulateMove(board, move);
		}

		GameTermination termination = determineTermination(game);
		verifyGameState(board, termination);

		return board;
	}

	/**
	 * Simuliert einen einzelnen Zug, indem er bestimmt und auf dem Brett ausgeführt
	 * wird.
	 * 
	 * @param board
	 *            das Simulationsbrett
	 * @param incomplete
	 *            der unvollständige PGN Zug
	 * @throws GameReconstructionException
	 *             falls der Zug nicht durchgeführt werden kann, weil er nicht
	 *             eindeutig oder nicht möglich ist
	 */
	private void simulateMove(Board board, PGNMove incomplete) throws GameReconstructionException {
		List<Move> candidates;
		if (incomplete instanceof NormalPGNMove normal) {
			candidates = findMoveCandidates(board, normal);
		} else if (incomplete instanceof CastlingPGNMove castling) {
			candidates = findCastlingCandidates(board, castling);
		} else {
			throw new RuntimeException("Darf nicht passieren! Der Zug ist von unbekanntem Typen: " + incomplete);
		}

		if (candidates.isEmpty()) {
			throw new GameReconstructionException(
					"In diesem Zustand kann keine Figure diesen Zug durchführen: " + incomplete);
		}

		if (candidates.size() > 1) {
			throw new GameReconstructionException(
					"Die Zugangabe " + incomplete + " ist in diesem Zustand doppeldeutig. Kandidaten: " + candidates);
		}

		Move candidate = candidates.get(0);

		try {
			board.makeMove(candidate);
		} catch (IllegalStateException e) {
			throw new GameReconstructionException("Der Zug '" + incomplete
					+ "' ist zwar eindeutig, ist in diesem Zustand aber nicht erlaubt: " + e.getMessage(), e);
		}

		if ((board.getColorInCheck() == null) == (incomplete.checking() != Checking.NONE)) {
			throw new GameReconstructionException(
					"Das Schachsetzen des PGN Zugs stimmt nicht mit dem tatsächlichen überein");
		}

		if ((incomplete.checking() == Checking.CHECKMATE) != (board.getGameState() == GameState.END_CHECKMATE)) {
			throw new GameReconstructionException(
					"Das Schachmattsetzen des PGN Zugs stimmt nicht mit dem tatsächlichen überein");
		}
	}

	/**
	 * Findet alle Zug-Kandidaten, die auf die nicht-Rochade passen.
	 * 
	 * @param board
	 *            das Simulationsbrett
	 * @param incomplete
	 *            der unvollständige PGN Zug
	 * @return alle Züge, die gemäß dem PGN Zug in diesem Zustand möglich sind
	 */
	private List<Move> findMoveCandidates(Board board, NormalPGNMove incomplete) {
		return board.findAllLegalMoves().stream()
				.filter(move -> incomplete.piece() == move.getPiece().getAlgebraicNotationSymbol())
				.filter(move -> move.getTo().equals(incomplete.to()))
				// Ist die Spalte/Reihe -1, dann filtern wir nicht danach.
				.filter(move -> incomplete.fromFile() == -1 || move.getFrom().getFile() == incomplete.fromFile())
				.filter(move -> incomplete.fromRank() == -1 || move.getFrom().getRank() == incomplete.fromRank())
				.filter(move -> incomplete.capture() == (move.getType() == MoveType.CAPTURE
						|| move.getType() == MoveType.CAPTURE_PROMOTION || move.getType() == MoveType.EN_PASSANT))
				.filter(move -> promotionCompatible(incomplete, move)).toList();
	}

	/**
	 * Findet alle Rochaden-Kandidaten, die zu diesem PGN Zug passen
	 * 
	 * @param board
	 *            das Simulationsbrett
	 * @param incomplete
	 *            der unvollständige PGN Zug
	 * @return alle Rochaden, die gemäß dem PGN Zug in diesem Zustand möglich sind
	 */
	private List<Move> findCastlingCandidates(Board board, CastlingPGNMove incomplete) {
		return board.findAllLegalMoves().stream()
				.filter(move -> (incomplete.kingSide() && move.getType() == MoveType.CASTLING_KINGSIDE)
						|| (!incomplete.kingSide() && move.getType() == MoveType.CASTLING_QUEENSIDE))
				.toList();
	}

	/**
	 * Erstellt den Initialzustand des Bretts. Entweder ist es das Standardbrett
	 * oder mit einer speziellen FEN-Konfiguration initialisiert, wenn es so in den
	 * Tags angegeben ist.
	 * 
	 * @param game
	 *            das PGN Spiel, für das das Brett erstellt werden soll
	 * @return das Brett in dem passenden Startzustand
	 * @throws GameReconstructionException
	 *             falls die angegebene FEN fehlerhaft ist
	 */
	private Board createInitialBoard(PGNGame game) throws GameReconstructionException {
		Map<String, String> tags = game.tags();

		Player[] players = createPlayers(tags);

		if ("1".equals(tags.get("SetUp")) && tags.containsKey("FEN")) {
			String encodedFEN = tags.get("FEN");
			try {
				return new Board(encodedFEN, players);
			} catch (FENFormatException e) {
				throw new GameReconstructionException("Die mitgelieferte FEN enthält Fehler", e);
			}
		} else {
			// das Spiel startet in der Startkonfiguration
			return new Board(players);
		}
	}

	/**
	 * Erstellt die beiden Spieler
	 * 
	 * @param tags
	 *            Tags, in dem die Spieler enthalten sind
	 * @return ein Spieler Array, wobei Weiß an Index 0 und Schwarz an Index 1 ist
	 */
	private Player[] createPlayers(Map<String, String> tags) throws GameReconstructionException {
		String whiteName = expectTag(tags, "White");
		String blackName = expectTag(tags, "Black");

		return new Player[]{new Player(whiteName, Color.WHITE, whiteRemainingTime),
				new Player(blackName, Color.BLACK, blackRemainingTime)};
	}

	/**
	 * Überprüft, ob es diesen Tag gibt und gibt dessen Wert zurück.
	 * 
	 * @param tags
	 *            die Tags, in denen gesucht werden soll
	 * @param tagName
	 *            der Name des Tags
	 * @return den Wert des Tags
	 * @throws GameReconstructionException
	 *             falls dieser Tag nicht in tags enthalten ist
	 */
	private String expectTag(Map<String, String> tags, String tagName) throws GameReconstructionException {
		String value = tags.get(tagName);
		if (value == null) {
			throw new GameReconstructionException("Der Tag '" + tagName + "' muss in einem PGN-Spiel enthalten sein");
		}

		return value;
	}

	/**
	 * Überprüft, ob der durch PGN spezifizierte Spielzustand am Ende der
	 * Zug-Historie tatsächlich eingetreten ist
	 * 
	 * @param board
	 *            das Simulationsbrett
	 * @param termination
	 *            der in PGN angegebene End-Zustand
	 * @throws GameReconstructionException
	 *             falls der PGN Zustand nicht mit dem tatsächlichen übereinstimmt
	 */
	private void verifyGameState(Board board, GameTermination termination) throws GameReconstructionException {
		GameState actualState = board.getGameState();
		switch (termination) {
			case IN_PROGRESS -> {
				if (actualState != GameState.RUNNING && actualState != GameState.PAUSED) {
					throw new GameReconstructionException(
							"Das Spiel läuft laut PGN noch, jedoch befindet sich das Spiel im Zustand " + actualState);
				}
			}
			case DRAW -> {
				if (actualState == GameState.RUNNING || actualState == GameState.PAUSED) {
					// Spiel läuft technisch noch, ist jedoch ein Unentschieden.
					// Die Spieler müssen sich also geeinigt haben.
					board.agreeToDraw();
					return;
				}

				if (actualState != GameState.END_STALEMATE && actualState != GameState.END_MATERIAL
						&& actualState != GameState.END_REPETITION && actualState != GameState.END_50MOVE) {
					throw new GameReconstructionException(
							"Das Spiel endet laut PGN in einem Unentschieden, jedoch befindet sich das Spiel im Zustand "
									+ actualState);
				}
			}
			case WHITE_WINS, BLACK_WINS -> {
				Player winner = board.getWinner();
				if (winner == null) {
					if (actualState != GameState.RUNNING && actualState != GameState.PAUSED) {
						throw new GameReconstructionException(
								"Das Spiel endet laut PGN durch Aufgeben, jedoch lief das Spiel zu diesem Zeitpunkt gar nicht mehr und befindet sich im Zustand "
										+ actualState);
					}

					// Es gibt einen Gewinner, obwohl es technisch nicht notwendig wäre.
					// Das heißt, ein Spieler muss aufgegeben haben
					board.resignPlayer(termination == GameTermination.WHITE_WINS ? Color.BLACK : Color.WHITE);
					return;
				}

				// Spiel ist tatsächlich vorbei, jetzt muss nur noch die Farbe des Gewinners
				// stimmen
				if ((winner.getColor() == Color.WHITE && termination != GameTermination.WHITE_WINS)
						|| (winner.getColor() == Color.BLACK && termination != GameTermination.BLACK_WINS)) {
					throw new GameReconstructionException(
							"Der angegebene Gewinner stimmt nicht mit dem tatsächlichen Gewinner überein");
				}
			}
		}
	}

	/**
	 * Bestimmt den in PGN angegebene End-Zustand konsistent mit dessen Tags ist.
	 * 
	 * @param game
	 *            das PGN Spiel
	 * @return der durch PGN spezifizierte End-Zustand
	 * @throws GameReconstructionException
	 *             falls der Result-TAG-Wert dem angegebene End-Zustand widerspricht
	 */
	private GameTermination determineTermination(PGNGame game) throws GameReconstructionException {
		String resultTagValue = expectTag(game.tags(), "Result");
		if (!resultTagValue.equals(game.termination().toString())) {
			throw new GameReconstructionException("Der Result-Tag widerspricht dem Endergebnis des Spiels. Result-Tag: "
					+ resultTagValue + " Endergebnis: " + game.termination());
		}

		return game.termination();
	}

	/**
	 * @param incomplete
	 *            der unvollständige PGN Zug
	 * @param move
	 *            der vom Simulationsbrett vorgeschlagene Zug
	 * @return ob die beiden Züge dieselben Promotionsangaben machen
	 */
	private boolean promotionCompatible(NormalPGNMove incomplete, Move move) {
		if (!incomplete.isPromotion() && move.getPromotionPiece() == null) {
			return true;
		}

		if (incomplete.isPromotion() && move.getPromotionPiece() != null) {
			return Objects.equals(incomplete.promotionPiece(), move.getPromotionPiece().getAlgebraicNotationSymbol());
		}

		return false;
	}

}
