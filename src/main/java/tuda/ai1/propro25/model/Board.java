/* (C) 2025-2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.model;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiFunction;
import tuda.ai1.propro25.fen.FENFormatException;
import tuda.ai1.propro25.fen.FENParser;
import tuda.ai1.propro25.fen.FENRecord;
import tuda.ai1.propro25.fen.FENSerializer;
import tuda.ai1.propro25.model.history.HistoricalBoardState;
import tuda.ai1.propro25.model.history.HistoricalGame;
import tuda.ai1.propro25.model.piece.*;
import tuda.ai1.propro25.pgn.AlgebraicNotationMoveGenerator;
import tuda.ai1.propro25.pgn.move.Checking;

/**
 * Das Spielbrett (Board) modelliert das Schachbrett selbst. Das Brett überwacht
 * dabei auch die Spielregeln und stellt somit das Zentrum des Schachspiels dar
 */
public class Board implements DeepCopyable {
	public static final int BOARD_SIZE = 8;
	private final Player[] players;
	private int currentPlayerIndex;
	private CastlingAvailability castlingAvailability;
	/**
	 * Speichert vorherige Zustände des Spielbrettes
	 */
	private final ArrayList<HistoricalBoardState> history;
	// Speichert den Spielstand durch ein 2d-Array aus Pieces. Ist an einer Position
	// kein Piece so ist in dem Array null gespeichert. Spaltenbasiert.
	private Piece[][] pieceGrid;
	private GameState gameState;
	private int winnerIndex = -1;
	// HalfMove und FullMove clocks zählen Züge seit dem letzten Schlagen und
	// ganze Spielzüge (siehe FEN)
	private int halfMoveClock = 0;
	private int fullMoveClock = 1;
	// Cached aktuell legale Züge damit sie weniger oft neu berechnet werden müssen
	private List<Move> currentlyLegalMoves;
	// Speichert die Zeit, die der aktuelle Spieler zu Beginn dieses Zuges noch
	// hatte
	private int playerTimeAtStartOfMove;
	// Speichert, ob gerade ein Spieler im Schach steht
	private Color colorInCheck;
	// falls wir nicht mit dem Standardbrett starten, müssen wir uns das merken
	private final FENRecord nonStandardStartState;
	private int initialTime;

	/**
	 * Erstellt ein neues Standardbrett mit 10m Spielzeit pro Seite
	 */
	public Board() {
		this(new Player[]{new Player("Weiß", Color.WHITE, 600), new Player("Schwarz", Color.BLACK, 600)});
	}

	/**
	 * Erstellt ein neues Standardbrett mit spezifizierten Spielern
	 * 
	 * @param players
	 *            Spieler, die an diesem Spiel teilnehmen (Es müssen 2 Spielende
	 *            sein, wobei der erste die weißen Figuren und der zweite die
	 *            schwarzen Figuren nutzt)
	 */
	public Board(Player[] players) {
		validatePlayerArray(players);
		this.pieceGrid = createBasicPieceGrid();
		this.players = players;
		this.currentPlayerIndex = 0;
		this.history = new ArrayList<>();
		this.castlingAvailability = new CastlingAvailability(true, true, true, true);
		this.gameState = GameState.PAUSED;
		this.halfMoveClock = 0;
		this.fullMoveClock = 1;
		this.currentlyLegalMoves = null;
		this.playerTimeAtStartOfMove = getCurrentPlayer().getRemainingTime();
		this.colorInCheck = null;
		this.nonStandardStartState = null; // ist Startposition
		this.initialTime = players[0].getRemainingTime();
	}

	/**
	 * Erstellt ein neues Brett basierend auf der FEN und gibt beiden Spielern 10
	 * min Zugzeit
	 * 
	 * @param fen
	 *            FEN String als Ausgangsposition
	 * @throws FENFormatException
	 *             Wenn der FEN String nicht geparsed werden konnte
	 */
	public Board(String fen) throws FENFormatException {
		this(fen, new Player[]{new Player("White", Color.WHITE, 600), new Player("Black", Color.BLACK, 600)});
	}

	/**
	 * Erstellt ein Board auf Basis eines FEN Strings. Dieser wird gelesen und dann
	 * werden alle Eigenschaften dieses Boards dementsprechend gesetzt. Das Ergebnis
	 * ist ein Spielbares Board mit den Eigenschaften des FEN-Strings
	 * 
	 * @param fen
	 *            FEN String als Ausgangsposition
	 * @param players
	 *            Spieler array (Es müssen 2 Spielende sein, wobei der erste die
	 *            weißen Figuren und der zweite die schwarzen Figuren nutzt)
	 * @throws FENFormatException
	 *             Wenn der FEN String nicht geparsed werden konnte
	 */
	public Board(String fen, Player[] players) throws FENFormatException {
		if (BOARD_SIZE != 8) {
			throw new IllegalArgumentException("Aktuell sind nur Brettgrößen von 8x8 unterstützt!");
		}
		if (fen == null || fen.isEmpty()) {
			throw new IllegalArgumentException("Der FEN String darf weder null, noch leer sein!");
		}
		validatePlayerArray(players);
		FENParser fenParser = new FENParser();
		FENRecord parsedRecord = fenParser.parseRecord(fen);
		pieceGrid = parsedRecord.deepCopyBoard(); // wichtig: sonst wird nonStandardStartState auch modifiziert

		this.players = players;
		currentPlayerIndex = parsedRecord.activeColor() == Color.WHITE ? 0 : 1;
		history = new ArrayList<>();
		castlingAvailability = parsedRecord.castlingAvailability();
		this.halfMoveClock = parsedRecord.halfMoveClock();
		this.fullMoveClock = parsedRecord.fullMoveClock();

		// Damit enPassant ordentlich funktioniert, müssen wir aus dem target den
		// letzten DOUBLEPAWN move rekonstruieren
		if (parsedRecord.enPassantTarget() != null) {
			Coordinate c1 = new Coordinate(parsedRecord.enPassantTarget().getFile(),
					parsedRecord.enPassantTarget().getRank() + 1);
			Coordinate c2 = new Coordinate(parsedRecord.enPassantTarget().getFile(),
					parsedRecord.enPassantTarget().getRank() - 1);
			Move enPassant;
			// Je nachdem ob dieses Feld null ist wissen wir, wie herum der Bauer sich
			// bewegt hat
			if (pieceGrid[c1.getFile()][c1.getRank()] == null) {
				enPassant = new Move(getPiece(c2), c1, c2, MoveType.DOUBLEPAWN);
			} else {
				enPassant = new Move(getPiece(c1), c2, c1, MoveType.DOUBLEPAWN);
			}

			// currentPlayer ist Spieler, der jetzt dran ist.
			// Demnach wurde dieser Bauern-Zug von ANDEREN Spieler gemacht
			int doublePawnPlayer = (currentPlayerIndex + 1) % 2;

			// Hier können moveClocks einfach übernommen werden da man nicht weiter
			// zurückspulen kann

			// Zu En Passant: wir müssen eine Liste an möglichen Moves angeben,
			// also geben wir null. (Wir wissen nicht, wie es davor aussah)
			history.add(new HistoricalBoardState(this, doublePawnPlayer, null, enPassant));
		}

		gameState = isMatePossible() ? GameState.PAUSED : GameState.END_MATERIAL;
		this.currentlyLegalMoves = null;
		this.playerTimeAtStartOfMove = getCurrentPlayer().getRemainingTime();
		this.colorInCheck = null;
		this.nonStandardStartState = parsedRecord;
		this.initialTime = players[0].getRemainingTime();
	}

	/**
	 * Konstruktor, um alle wichtigen Felder des Boardes manuell zu bestimmen. Kann
	 * zum Beispiel gut genutzt werden, um ein unterbrochenes Spiel
	 * wiederherzustellen
	 *
	 * @param pieceGrid
	 *            Das Figuren array
	 * @param players
	 *            Die Spieler, welche am Spielen sind (Es müssen 2 Spielende sein,
	 *            wobei der erste die weißen Figuren und der zweite die schwarzen
	 *            Figuren nutzt)
	 * @param currentPlayerIndex
	 *            Index des Spielers im players array, welcher gerade am Zug ist
	 * @param history
	 *            Bisherige Zustände im Spielverlauf. Der aktuelle Zustand darf
	 *            darin nicht enthalten sein!
	 * @param castlingAvailability
	 *            Rochadenoptionen
	 * @param gameState
	 *            Aktueller Spielzustand
	 * @param halfMoveClock
	 *            Anzahl an Half-moves (Züge, seitdem ein Bauer bewegt oder eine
	 *            Figur geschlagen wurde)
	 * @param fullMoveClock
	 *            Anzahl an Full-moves (effektiv Runde)
	 */
	public Board(Piece[][] pieceGrid, Player[] players, int currentPlayerIndex, ArrayList<HistoricalBoardState> history,
			CastlingAvailability castlingAvailability, GameState gameState, int halfMoveClock, int fullMoveClock) {
		if (pieceGrid == null) {
			throw new IllegalArgumentException("PieceGrid darf nicht null sein!");
		}
		if (BOARD_SIZE != 8 || pieceGrid.length != BOARD_SIZE || pieceGrid[0].length != BOARD_SIZE) {
			throw new IllegalArgumentException("Aktuell sind nur Brettgrößen von 8x8 unterstützt!");
		}
		validatePlayerArray(players);
		if (currentPlayerIndex < 0 || currentPlayerIndex >= players.length) {
			throw new IllegalArgumentException("CurrentPlayerIndex hat einen ungültigen Wert! (0 oder 1 erlaubt)");
		}
		if (history == null) {
			throw new IllegalArgumentException(
					"Bisherige MoveHistory darf nicht null sein! Leere Liste wäre aber erlaubt!");
		}
		if (castlingAvailability == null || gameState == null) {
			throw new IllegalArgumentException("CastlingAvailability und gameState dürfen nicht null sein!");
		}
		if (halfMoveClock < 0 || fullMoveClock < 1) {
			throw new IllegalArgumentException("HalfMoveClock oder FullMoveClock hat unerlaubten Wert!");
		}
		this.pieceGrid = pieceGrid;
		this.players = players;
		this.currentPlayerIndex = currentPlayerIndex;
		this.history = history;
		this.castlingAvailability = castlingAvailability;
		this.gameState = gameState;
		this.halfMoveClock = halfMoveClock;
		this.fullMoveClock = fullMoveClock;
		this.currentlyLegalMoves = null;
		this.playerTimeAtStartOfMove = getCurrentPlayer().getRemainingTime();
		this.colorInCheck = null;
		this.winnerIndex = -1;
		this.nonStandardStartState = new FENRecord(getUnmodifiablePieceGrid(), getCurrentPlayer().getColor(),
				getCastlingAvailability(), null, halfMoveClock, fullMoveClock);
		this.initialTime = players[0].getRemainingTime();
	}

	/**
	 * Verifiziert, dass das Array an Spielenden auch den Anforderungen entspricht,
	 * indem eine Exception geworfen wird, wenn dies nicht der Fall sein sollte. Es
	 * müssen genau 2 Spielende sein, wobei player[0] die weißen Figuren und
	 * player[1] die schwarzen Figuren nutzen muss. Zusätzlich darf die übrige Zeit
	 * nicht negativ sein. Da viele Konstruktoren diese Anforderungen haben, ist die
	 * Prüfung in diese Methode ausgelagert worden.
	 * 
	 * @param players
	 *            Spielenden-array
	 */
	private void validatePlayerArray(Player[] players) {
		if (players == null || players.length != 2 || players[0] == null || players[1] == null) {
			throw new IllegalArgumentException(
					"Das Spielerarray darf nicht null sein und muss genau 2 Spieler enthalten");
		}
		if (players[0].getColor() != Color.WHITE || players[1].getColor() != Color.BLACK) {
			throw new IllegalArgumentException(
					"Es wird davon ausgegangen, dass Spieler0 mit weißen Figuren spielt und Spieler1 mit schwarzen!");
		}
	}

	/**
	 * Pseudolegale Züge sind Züge, die zwar den Bewegungsmustern der Figuren
	 * folgen, aber gerade eigentlich verboten sein könnten, weil z.B. der König im
	 * Schach steht
	 * 
	 * @return Menge aller pseudolegaler Züge, die der aktuelle Spieler ausführen
	 *         könnte.
	 */
	private ArrayList<Move> findAllPseudoLegalMovesForColor(Color color) {
		ArrayList<Move> pseudoLegalMoves = new ArrayList<>();
		for (int file = 0; file < BOARD_SIZE; file++) {
			for (int rank = 0; rank < BOARD_SIZE; rank++) {
				Piece piece = getPiece(file, rank);
				if (piece != null && piece.getColor() == color) {
					var pseudoLegalForPiece = piece.getPseudolegalMoves(new Coordinate(file, rank), this);
					// Aus unerklärlichen Gründen ist "addAll()" hier deutlich langsamer?
					for (Move move : pseudoLegalForPiece) {
						pseudoLegalMoves.add(move);
					}
				}
			}
		}
		return pseudoLegalMoves;
	}

	/**
	 * Gibt eine Liste an allen im aktuellen Zustand erlaubten Spielzügen zurück.
	 * Dabei wird auf einen cache zugegriffen, der vorher bereits gebaut wurde oder
	 * schnell noch erstellt wird.
	 * 
	 * @return Liste an allen aktuell legalen Zügen
	 */
	public List<Move> findAllLegalMoves() {
		if (currentlyLegalMoves == null) {
			currentlyLegalMoves = calculateAllLegalMoves();
		}
		return currentlyLegalMoves;
	}

	/**
	 * Berechnet alle im aktuellen Zustand erlaubten Züge. Zuerst werden alle
	 * pseudolegalen Moves angefragt
	 * ({@link #findAllPseudoLegalMovesForColor(Color)}). Danach wird ein Zug als
	 * legal/illegal kategorisiert, indem geschaut wird ob der Gegner damit
	 * antworten könnte, den König des aktiven Spielers zu schlagen. Natürlich kann
	 * man den König nie wirklich schlagen, wenn der Gegner es also könnte, heißt
	 * das, dass der untersuchte Zug gegen eine Regel verstoßen hat
	 *
	 * @return Liste an allen legalen Zügen im aktuellen Spielzustand
	 */
	private List<Move> calculateAllLegalMoves() {
		ArrayList<Move> legalMoves = new ArrayList<>();
		if (gameState != GameState.RUNNING && gameState != GameState.PAUSED) {
			// Ein beendetes Spiel hat keine legalen Züge mehr!
			return legalMoves;
		}
		var allPseudoLegal = findAllPseudoLegalMovesForColor(getCurrentPlayer().getColor());
		for (var move : allPseudoLegal) {
			movePiece(move); // Wir tun so, als würden wir diesen Zug machen
			var enemyPseudolegalResponses = findAllPseudoLegalMovesForColor(getNextPlayer().getColor());
			// Für die Rochaden muss auch geprüft werden, dass selbst die Zwischenfelder
			// und die Ausgangsposition nicht bedroht waren
			if (move.getType() == MoveType.CASTLING_QUEENSIDE || move.getType() == MoveType.CASTLING_KINGSIDE) {
				Coordinate kingMovesOver = new Coordinate(
						move.getTo().getFile() + (move.getType() == MoveType.CASTLING_QUEENSIDE ? 1 : -1),
						move.getTo().getRank());
				var enemyTargetsCastlingJourney = enemyPseudolegalResponses.stream()
						.anyMatch(enemyResponse -> enemyResponse.getTo().equals(move.getFrom())
								|| enemyResponse.getTo().equals(kingMovesOver));
				if (enemyTargetsCastlingJourney) {
					// Dieser Zug kann nicht legal sein, die weiteren Überprüfungen werden also eh
					// nicht mehr gebraucht
					unMovePiece();
					continue;
				}
			}
			// Wenn wir jetzt im Schach stehen, war der Zug illegal
			boolean enemyCouldCaptureKing = movesContainCapturingKing(enemyPseudolegalResponses);
			if (!enemyCouldCaptureKing) {
				// Der Zug zuvor war also legal und wir können ihn zulassen
				legalMoves.add(move);
			}
			unMovePiece(); // Jetzt schieben wir wieder alles zurück wie es war
		}
		// Wir speichern diese Liste als unmodifiable damit sie von extern definitiv
		// nicht bearbeitet werden kann
		return Collections.unmodifiableList(legalMoves);
	}

	/**
	 * Prüft, ob der gegnerische Spieler mit den angegebenen Zügen den König des
	 * aktuellen Spielers schlagen könnte. Dies geschieht, indem die übergebene
	 * Liste auf Züge untersucht wird, die den aktuellen König treffen.
	 * 
	 * @param enemyPseudoLegalMoves
	 *            Die Züge, die der Gegner eventuell machen könnte
	 * @return true, wenn ein Zug enthalten war, der den König des aktuellen
	 *         Spielers schlagen kann
	 */
	private boolean movesContainCapturingKing(List<Move> enemyPseudoLegalMoves) {
		var kingInCurrentColor = new King(getCurrentPlayer().getColor());
		for (Move enemyResponse : enemyPseudoLegalMoves) {
			if (enemyResponse.getType() == MoveType.CAPTURE || enemyResponse.getType() == MoveType.CAPTURE_PROMOTION) {
				if (getPiece(enemyResponse.getTo()).equals(kingInCurrentColor)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Findet heraus, ob der Spieler gerade im Schach steht. Dies kann unabhängig
	 * vom aktuellen Spielzustand geprüft werden
	 *
	 * @param player
	 *            Spieler, für den das geprüft werden soll
	 * @return true, wenn der Spieler gerade im Schach steht
	 */
	private boolean isPlayerInCheck(Player player) {
		var enemyMoves = findAllPseudoLegalMovesForColor(player.getColor() == Color.WHITE ? Color.BLACK : Color.WHITE);
		return movesContainCapturingKing(enemyMoves);
	}

	/**
	 * Gibt alle erlaubten Zielfelder für die gewählte Figur zurück. Diese Methode
	 * generiert dabei echte legale Züge, nicht pseudolegale. Vorgeschlagene Züge
	 * sind also tatsächlich ausführbar. Die GUI kann diese Methode nutzen, um den
	 * Spielenden mögliche Züge vorzuschlagen, wenn eine Figur angeklickt wurde.
	 * 
	 * @param coordinate
	 *            Koordinate, auf der sich die Figur befindet, für die alle
	 *            möglichen Züge gefragt sind
	 * @return Liste aller möglichen Zügen mit dieser Figur
	 */
	public List<Move> getAllAllowedMovesForPieceOnSquare(Coordinate coordinate) {
		Piece piece = getPiece(coordinate);
		if (piece == null) {
			throw new IllegalArgumentException("Auf diesem Feld hat sich keine Figur befunden!");
		}
		if (piece.getColor() != getCurrentPlayer().getColor()) {
			return List.of(); // Dieser Spieler ist gerade nicht dran
		}
		// Filtere alle möglichen Moves nach denen, die für die gefragte Figur gelten
		return findAllLegalMoves().stream().filter(move -> move.getFrom().equals(coordinate)).toList();
	}

	/**
	 * Diese Methode speichert den aktuellen Zustand in der history und schiebt dann
	 * eine Figur auf dem Brett umher. Es wird nicht geprüft, ob der Zug tatsächlich
	 * legal wäre, daher ist diese Methode nicht public. Spielzustände werden
	 * abseits der Figurpositionen und Rochadeoptionen nicht aktualisiert. Alles,
	 * was diese Methode macht, ist also, die Figuren umherzuschieben und sich zu
	 * merken, was verschoben wurde. Moves selbst müssen aber schlüssig sein: Wenn
	 * dabei eine Figur überschrieben wird, muss der Movetype auch CAPTURE oder
	 * CAPTURE_PROMOTION sein. Ähnliches gilt für Umwandlungen oder EN_PASSANT. Für
	 * die öffentliche Methode, welche den gesamten Spielzustand anpasst, siehe
	 * {@link #makeMove(Move)}
	 *
	 * @param move
	 *            Zug, der durchgeführt werden soll
	 * @throws IllegalStateException
	 *             Wenn ein Move dabei ist, eine Figur unbewusst zu überschreiben
	 */
	void movePiece(Move move) {
		if (move == null) {
			throw new IllegalArgumentException("Move darf nicht null sein!");
		}
		if (pieceGrid[move.getTo().getFile()][move.getTo().getRank()] != null
				&& !(move.getType() == MoveType.CAPTURE || move.getType() == MoveType.CAPTURE_PROMOTION)
				&& (move.getInvolvedPiece() == null || !move.getInvolvedPiece()
						.equals(pieceGrid[move.getTo().getFile()][move.getTo().getRank()]))) {
			throw new IllegalStateException(
					"Auf der Zielposition befindet sich eine Figur, aber der Zug scheint diese nicht zu beachten!");
		}
		history.add(new HistoricalBoardState(this, currentPlayerIndex, currentlyLegalMoves, move));
		Piece movingPiece = pieceGrid[move.getFrom().getFile()][move.getFrom().getRank()];
		pieceGrid[move.getFrom().getFile()][move.getFrom().getRank()] = null;
		pieceGrid[move.getTo().getFile()][move.getTo().getRank()] = movingPiece;
		if (move.getType() == MoveType.EN_PASSANT) {
			// Position des gegnerischen Bauern lässt sich aus neuer Linie und alter Zeile
			// rekonstruieren!
			pieceGrid[move.getTo().getFile()][move.getFrom().getRank()] = null;
		} else if (move.getType() == MoveType.CASTLING_KINGSIDE) {
			// Turm über König nach f Linie hüpfen lassen
			pieceGrid[5][move.getFrom().getRank()] = pieceGrid[7][move.getFrom().getRank()];
			pieceGrid[7][move.getFrom().getRank()] = null;
		} else if (move.getType() == MoveType.CASTLING_QUEENSIDE) {
			// Turm über König nach d Linie hüpfen lassen
			pieceGrid[3][move.getFrom().getRank()] = pieceGrid[0][move.getFrom().getRank()];
			pieceGrid[0][move.getFrom().getRank()] = null;
		} else if (move.getType() == MoveType.PROMOTION || move.getType() == MoveType.CAPTURE_PROMOTION) {
			pieceGrid[move.getTo().getFile()][move.getTo().getRank()] = move.getPromotionPiece();
		}
		updateCastlingAvailability(move);
	}

	/**
	 * Überprüft die aktuelle castlingAvailability und aktualisiert sie, wenn sie
	 * sich durch den Move geändert hat. Wenn sich die Verfügbarkeiten nicht ändern,
	 * ist die Objektinstanz von castlingAvailability auch immernoch die Gleiche.
	 * 
	 * @param move
	 *            Zug, welcher die castlingAvailability evtl updaten könnte
	 */
	private void updateCastlingAvailability(Move move) {
		boolean[] wCastleAvail = checkCastlingAvailForColor(castlingAvailability.whiteCastleKingSide(),
				castlingAvailability.whiteCastleQueenSide(), Color.WHITE, move);
		boolean[] bCastleAvail = checkCastlingAvailForColor(castlingAvailability.blackCastleKingSide(),
				castlingAvailability.blackCastleQueenSide(), Color.BLACK, move);

		CastlingAvailability newCastAv = new CastlingAvailability(wCastleAvail[0], wCastleAvail[1], bCastleAvail[0],
				bCastleAvail[1]);
		if (!newCastAv.equals(castlingAvailability)) {
			castlingAvailability = newCastAv;
		}
	}

	/**
	 * Hilfsmethode - Prüft neue Rochadeoptionen für eine Farbe (weiß oder schwarz)
	 * 
	 * @param qSide
	 *            aktuelle Verfügbarkeit für Damenseite
	 * @param kSide
	 *            aktuelle Verfügbarkeit für Königsseite
	 * @param color
	 *            Farbe für welche die Rochadeoptionen geprüft werden
	 * @param move
	 *            Aktueller Zug der evtl. etwas ändern könnte
	 * @return boolean[]{new_castle_kingside, new_castle_queenside}
	 */
	private boolean[] checkCastlingAvailForColor(boolean kSide, boolean qSide, Color color, Move move) {
		if (!qSide && !kSide) {
			// Wenn es vorher schon nicht möglich war, brauchen wir hier garnicht weiter
			// prüfen
			return new boolean[]{false, false};
		}

		// Beachte, dass das Brett schon den neuen Zustand widerspiegelt, wir können
		// also nicht das Startfeld abfragen!
		Piece movedPiece = getPiece(move.getTo());
		if (movedPiece instanceof King && movedPiece.getColor() == color) {
			// Wenn der König bewegt wurde, sind beide Optionen weg
			return new boolean[]{false, false};
		}

		int backRank = (color == Color.WHITE ? 0 : 7);
		if (movedPiece instanceof Rook && movedPiece.getColor() == color) {
			// Wenn der Turm bewegt wurde, ist die Seite weg auf der er stand
			Coordinate from = move.getFrom();
			if (from.equals(new Coordinate(0, backRank))) {
				qSide = false;
			} else if (from.equals(new Coordinate(7, backRank))) {
				kSide = false;
			}
		}

		if (move.getType() == MoveType.CAPTURE || move.getType() == MoveType.CAPTURE_PROMOTION) {
			// Wenn unser Turm geschlagen wird, verlieren wir unsere Option auf dieser Seite
			Coordinate to = move.getTo();
			if (to.equals(new Coordinate(0, backRank))) {
				qSide = false;
			} else if (to.equals(new Coordinate(7, backRank))) {
				kSide = false;
			}
		}

		return new boolean[]{kSide, qSide};
	}

	/**
	 * Macht den letzten ausgeführten Zug wieder rückgängig. Dazu wird der letzte
	 * Spielzustand aus der history entfernt und übernommen. Diese Methode updated
	 * nicht den Spieler der gerade am Zug ist etc., sondern ausschließlich die
	 * Figurpositionen und Rochadeoptionen. Sie ist damit als inverse Operation zu
	 * {@link #movePiece(Move)} zu betrachten. Für die öffentliche Methode, welche
	 * alle Spielzustände updated, siehe {@link #undoLastMove()}
	 */
	void unMovePiece() {
		if (history.isEmpty()) {
			throw new IllegalStateException("Kann Figur nicht zurück bewegen, da es keinen vorherigen Zustand gab!");
		}
		HistoricalBoardState boardState = history.remove(history.size() - 1);
		pieceGrid = boardState.getPieceGrid();
		castlingAvailability = boardState.getCastlingAvailability();
	}

	/**
	 * Prüft, dass der vorgeschlagene Zug (Move) im aktuellen Spielzustand auch
	 * erlaubt/legal wäre
	 * 
	 * @param move
	 *            Zug, der jetzt ausgeführt werden soll
	 * @return true, wenn der Zug erlaubt ist, false ansonsten
	 */
	private boolean isLegalMove(Move move) {
		return findAllLegalMoves().contains(move);
	}

	/**
	 * Diese Methode kann von extern aufgerufen werden, um im Namen des aktuellen
	 * Spielers einen Zug durchzuführen. Dabei werden alle Spielzustände
	 * aktualisiert.
	 * 
	 * @param move
	 *            Zug, der durchgeführt werden soll
	 */
	public void makeMove(Move move) {
		if (!isLegalMove(move)) {
			throw new IllegalArgumentException("Der vorgeschlagene Zug ist gerade nicht erlaubt: " + move);
		}
		if (gameState == GameState.PAUSED) {
			gameState = GameState.RUNNING;
		}
		if (gameState != GameState.RUNNING) {
			// Sollte eigentlich schon dadurch abgedeckt sein, dass ein beendetes Spiel
			// keine legalen Züge mehr hat, aber zur Sicherheit...
			throw new IllegalStateException("Das Spiel ist beendet. Es kann kein weiterer Zug mehr gemacht werden!");
		}
		movePiece(move);
		// Die alten legalen Züge stimmen jetzt nicht mehr
		currentlyLegalMoves = null;
		advancePlayer();
		if (isPlayerInCheck(getCurrentPlayer())) {
			// Der Spieler, der eben einen Zug ausgeführt hat, kann jetzt nicht mehr im
			// Schach sein: Das wäre ein illegaler Zug gewesen. Wir müssen also nur den
			// aktuellen Spieler testen
			colorInCheck = getCurrentPlayer().getColor();
		} else {
			colorInCheck = null;
		}
		if (move.getPiece().getColor() == Color.BLACK) {
			fullMoveClock++;
		}
		if (move.getType() != MoveType.CAPTURE && move.getType() != MoveType.CAPTURE_PROMOTION
				&& !(move.getPiece() instanceof Pawn)) {
			halfMoveClock++;
		} else {
			halfMoveClock = 0;
		}
		// Erst schauen, ob das Spiel beendet ist
		if (fullMoveClock >= 5 && howOftenHasThisPositionBeenSeen() >= 3) {
			gameState = GameState.END_REPETITION;
			winnerIndex = -1;
			return;
		}
		if (halfMoveClock >= 50) {
			gameState = GameState.END_50MOVE;
			winnerIndex = -1;
			return;
		}
		if (!isMatePossible()) {
			gameState = GameState.END_MATERIAL;
			winnerIndex = -1;
			return;
		}
		// Erst wenn wir wirklich hier ankommen berechnen wir legale Züge, um Zeit zu
		// sparen und nicht aus Versehen legale Züge vorzuschlagen, obwohl das Spiel
		// bereits beendet ist
		if (findAllLegalMoves().isEmpty()) {
			// Das Spiel ist definitiv zu Ende, aber es könnte Schachmatt oder Patt sein
			// Wir schauen also, ob wir gerade im Schach stehen, indem berechnet wird ob der
			// Gegner unseren König schlagen könnte
			if (movesContainCapturingKing(findAllPseudoLegalMovesForColor(getNextPlayer().getColor()))) {
				gameState = GameState.END_CHECKMATE;
				winnerIndex = (currentPlayerIndex + 1) % 2;
			} else {
				// König kann nicht geschlagen werden, ist also gerade nicht im Schach -> Patt
				gameState = GameState.END_STALEMATE;
				winnerIndex = -1;
			}
		}
	}

	/**
	 * Diese Methode kann von extern aufgerufen werden, um offiziell den letzten Zug
	 * zurückzunehmen. Dabei werden alle Spielzustände geupdated.
	 */
	public void undoLastMove() {
		if (history.isEmpty()) {
			throw new IllegalStateException("Es gibt keinen letzten Zug, der rückgängig gemacht werden kann!");
		}
		HistoricalBoardState boardState = history.get(history.size() - 1);
		fullMoveClock = boardState.getFullMoveClock();
		halfMoveClock = boardState.getHalfMoveClock();
		currentPlayerIndex = boardState.getPlayerIndex();
		currentlyLegalMoves = boardState.getLegalMovesInThisState();
		gameState = boardState.getGameState();
		colorInCheck = boardState.getColorInCheck();
		winnerIndex = boardState.getWinnerIndex();
		unMovePiece();
	}

	/**
	 * Gibt die CastlingAvailability zurück, also die Information, ob König/Türme
	 * schon bewegt wurden und daher evtl eine Rochade erlaubt wäre
	 *
	 * @param color
	 *            Farbe der zu überprüfenden Verfügbarkeit
	 * @param kingSide
	 *            Ob die kurze (king, true) oder lange (queen, false) Rochade
	 *            gefragt ist
	 * @return true wenn Rochade unter den Parametern erlaubt ist, false ansonsten
	 */
	public boolean hasCastlingAvailability(Color color, boolean kingSide) {
		if (color == Color.BLACK) {
			return kingSide ? castlingAvailability.blackCastleKingSide() : castlingAvailability.blackCastleQueenSide();
		} else {
			return kingSide ? castlingAvailability.whiteCastleKingSide() : castlingAvailability.whiteCastleQueenSide();
		}
	}

	/**
	 * Setzt den nächsten Spieler als aktiv - damit ist der Zug des bisherigen
	 * Spielers beendet
	 */
	private void advancePlayer() {
		currentPlayerIndex = (++currentPlayerIndex % 2);
	}

	/**
	 * Lässt den Spieler mit der übergebenen Farbe aufgeben, sofern das Spiel noch
	 * nicht beendet ist
	 * 
	 * @param color
	 *            Farbe des Spielers, der aufgeben möchte
	 */
	public void resignPlayer(Color color) {
		if (gameState != GameState.PAUSED && gameState != GameState.RUNNING) {
			throw new IllegalStateException("Das Spiel ist bereits beendet!");
		}
		gameState = GameState.END_RESIGN;
		winnerIndex = color == Color.WHITE ? 1 : 0;
	}

	/**
	 * Lässt den Spieler mit der übergebenen Farbe aufgrund von Zeitüberschreitung
	 * verlieren, sofern das Spiel noch nicht beendet ist
	 *
	 * @param color
	 *            Farbe des Spielers, dessen Zeit abgelaufen ist
	 */
	public void forfeitByTime(Color color) {
		if (gameState != GameState.PAUSED && gameState != GameState.RUNNING) {
			throw new IllegalStateException("Das Spiel ist bereits beendet!");
		}
		gameState = GameState.END_TIMEOUT;
		winnerIndex = color == Color.WHITE ? 1 : 0;
		currentlyLegalMoves = null;
	}

	/**
	 * Bekundet, dass sich beide Spieler auf unentschieden geeinigt haben und das
	 * Spiel damit beendet ist Das geht natürlich nur, wenn das Spiel noch nicht
	 * beendet war.
	 */
	public void agreeToDraw() {
		if (gameState != GameState.PAUSED && gameState != GameState.RUNNING) {
			throw new IllegalStateException("Das Spiel ist bereits beendet!");
		}
		gameState = GameState.END_AGREEMENT;
		winnerIndex = -1;
		currentlyLegalMoves = null;
	}

	/**
	 * Erstellt ein 2D Array an Pieces, welche das Brett repräsentieren. Die Figuren
	 * auf den Feldern sind dabei in Standard Startaufstellung.
	 * 
	 * @return 2D Array für das Spielfeld, {x, y} Koordinaten
	 */
	private Piece[][] createBasicPieceGrid() {
		Piece[][] pieceGrid = new Piece[BOARD_SIZE][BOARD_SIZE];
		for (int file = 0; file < 8; file++) {
			pieceGrid[file][1] = new Pawn(Color.WHITE);
			pieceGrid[file][6] = new Pawn(Color.BLACK);
			for (int rank = 0; rank < 8; rank += 7) {
				Color color = rank == 0 ? Color.WHITE : Color.BLACK;
				Piece piece = switch (file) {
					case 0, 7 -> new Rook(color);
					case 1, 6 -> new Knight(color);
					case 2, 5 -> new Bishop(color);
					case 3 -> new Queen(color);
					case 4 -> new King(color);
					default -> null;
				};
				pieceGrid[file][rank] = piece;
			}
		}
		return pieceGrid;
	}

	/**
	 * Genutzt für die Wiederholungsregel. Nutzt den StateString und die bisher
	 * gesehenen Spielzustände, um zu sagen, wie oft der aktuelle Spielzustand schon
	 * gesehen wurde
	 *
	 * @return Wie oft dieser Spielzustand schon gesehen wurde
	 */
	int howOftenHasThisPositionBeenSeen() {
		HashMap<String, Integer> stateMap = new HashMap<>();
		for (var state : history) {
			// Wir holen uns den String der diesen Zustand repräsentiert
			var stateString = state.getStateString();
			// Jetzt schauen wir, ob er schonmal besucht wurde, indem wir ihn in der
			// Tabelle/Map nachschlagen
			var visits = stateMap.computeIfAbsent(stateString, key -> 0);
			visits++;
			stateMap.put(stateString, visits); // Neuen Wert speichern
		}
		return stateMap.computeIfAbsent(getStateString(), key -> 0) + 1;
	}

	/**
	 * Gibt zurück, welche Seite(n) theoretisch noch mattsetzen kann/können. Eine
	 * Seite kann noch mattsetzen, wenn sie: - mindestens 2 Springer hat, - einen
	 * Läufer und einen Springer hat, - zwei Läufer auf unterschiedlichen Farben
	 * hat, - oder noch eine Schwerfigur oder einen Bauern besitzt.
	 *
	 * @return Liste der Farben (Color.WHITE, Color.BLACK), die noch mattsetzen
	 *         können.
	 */
	HashSet<Color> getSidesThatCanMate() {
		HashSet<Color> canMate = new HashSet<>();

		Boolean whiteBishopColor = null; // true = hell, false = dunkel
		Boolean blackBishopColor = null;

		boolean whiteBishopsSameColor = true;
		boolean blackBishopsSameColor = true;

		int whiteBishopCount = 0;
		int blackBishopCount = 0;
		int whiteKnightCount = 0;
		int blackKnightCount = 0;

		BiFunction<Integer, Integer, Boolean> isLightSquare = (f, r) -> (f + r) % 2 == 0;

		for (int file = 0; file < pieceGrid.length; file++) {
			for (int rank = 0; rank < pieceGrid[file].length; rank++) {
				Piece piece = pieceGrid[file][rank];
				if (piece == null)
					continue;

				char fen = piece.getFenSymbol();
				boolean color;

				switch (fen) {
					case 'B' :
						color = isLightSquare.apply(file, rank);
						if (whiteBishopColor == null) {
							whiteBishopColor = color;
						} else if (whiteBishopColor != color) {
							whiteBishopsSameColor = false;
						}
						whiteBishopCount++;
						break;
					case 'b' :
						color = isLightSquare.apply(file, rank);
						if (blackBishopColor == null) {
							blackBishopColor = color;
						} else if (blackBishopColor != color) {
							blackBishopsSameColor = false;
						}
						blackBishopCount++;
						break;
					case 'N' :
						whiteKnightCount++;
						break;
					case 'n' :
						blackKnightCount++;
						break;
					case 'Q' :
					case 'R' :
					case 'P' :
						canMate.add(Color.WHITE);
						break;
					case 'q' :
					case 'r' :
					case 'p' :
						canMate.add(Color.BLACK);
						break;
				}
			}
		}

		boolean whiteCanMate = whiteKnightCount >= 2 || (whiteBishopCount >= 2 && !whiteBishopsSameColor)
				|| (whiteBishopCount >= 1 && whiteKnightCount >= 1);
		boolean blackCanMate = blackKnightCount >= 2 || (blackBishopCount >= 2 && !blackBishopsSameColor)
				|| (blackBishopCount >= 1 && blackKnightCount >= 1);

		if (whiteCanMate) {
			canMate.add(Color.WHITE);
		}
		if (blackCanMate) {
			canMate.add(Color.BLACK);
		}

		return canMate;
	}

	/**
	 * Prüft, ob eine Stellung auf dem Schachbrett noch theoretisch mattfähig ist.
	 *
	 * @return {@code true}, wenn mindestens eine Seite theoretisch mattsetzen kann,
	 *         * {@code false}, wenn keine Seite über genügend Material für ein Matt
	 *         verfügt.
	 */
	boolean isMatePossible() {
		return !getSidesThatCanMate().isEmpty();
	}
	/**
	 * Zählt die aktuellen Werte aller Figuren auf dem Spielfeld für beide Spieler
	 * zusammen. (siehe Chess Piece Value)
	 * 
	 * @return {CPV weiß, CPV schwarz}
	 */
	public int[] getPieceValues() {
		int white = 0;
		int black = 0;
		for (Piece[] file : pieceGrid) {
			for (Piece piece : file) {
				if (piece == null)
					continue;
				if (piece.getColor() == Color.WHITE)
					white += piece.getValue();
				if (piece.getColor() == Color.BLACK)
					black += piece.getValue();
			}
		}
		return new int[]{white, black};
	}

	/**
	 * Das zurückgegebene HistoricalGame kann z.B. exportiert werden oder
	 * gespeichert werden, um es später weiterzuspielen.
	 *
	 * @return HistoricalGame vom aktuellen Spielzustand
	 */
	public HistoricalGame exportGame() {
		return new HistoricalGame(players[0].getName() + " vs. " + players[1].getName(), LocalDateTime.now(), this);
	}

	/**
	 * @return FEN String, der den aktuellen Spielzustand beschreibt
	 */
	public String exportToFEN() {
		Coordinate enPassantTarget = null;
		if (getLastMove() != null && getLastMove().getType() == MoveType.DOUBLEPAWN) {
			enPassantTarget = new Coordinate(getLastMove().getTo().getFile(),
					getLastMove().getTo().getRank() == 3 ? 2 : 5);
		}
		FENRecord fenRecord = new FENRecord(getUnmodifiablePieceGrid(), getCurrentPlayer().getColor(),
				getCastlingAvailability(), enPassantTarget, halfMoveClock, fullMoveClock);
		FENSerializer fenSerializer = new FENSerializer();
		return fenSerializer.serializeRecord(fenRecord);
	}

	///  Ab hier kommen nur noch Getter/Setter Methoden und überschriebene Methoden
	///  wie z.B. equals() oder hashCode()

	/**
	 * Gibt die Schachfigur an der angegebenen Position zurück
	 *
	 * @param file
	 *            Linie des gesuchten Feldes
	 * @param rank
	 *            Reihe des gesuchten Feldes
	 * @return Die Schachfigur an der spezifizierten Position, oder null, wenn sich
	 *         dort keine Figur befindet
	 */
	public Piece getPiece(int file, int rank) {
		if (rank < 0 || rank >= BOARD_SIZE) {
			throw new IllegalArgumentException("Reihe ist außerhalb des Bretts!");
		}
		if (file < 0 || file >= BOARD_SIZE) {
			throw new IllegalArgumentException("Linie ist außerhalb des Bretts!");
		}
		return pieceGrid[file][rank];
	}

	/**
	 * Gibt die Schachfigur an der angegebenen Position zurück. Ruft dazu einfach
	 * {@link #getPiece(int, int)} auf.
	 *
	 * @param coordinate
	 *            Koordinate des untersuchten Feldes
	 * @return Die Schachfigur an der spezifizierten Position, oder null, wenn sich
	 *         dort keine Figur befindet
	 */
	public Piece getPiece(Coordinate coordinate) {
		return getPiece(coordinate.getFile(), coordinate.getRank());
	}

	/**
	 * @return Eine Kopie des Spielbretts. Änderungen an den Figurenanordnungen
	 *         werden nicht auf das echte Spielbrett übertragen - aber die Figuren
	 *         sind die Echten!
	 */
	public Piece[][] getUnmodifiablePieceGrid() {
		Piece[][] deeperCopy = new Piece[pieceGrid.length][];
		for (int i = 0; i < pieceGrid.length; i++) {
			deeperCopy[i] = pieceGrid[i].clone();
		}
		return deeperCopy;
	}

	/**
	 * Gibt die CastlingAvailability zurück, also die Information, ob König/Türme
	 * schon bewegt wurden und daher evtl. eine Rochade erlaubt wäre
	 * {@link #hasCastlingAvailability(Color, boolean)} für einfachere Verwendung
	 *
	 * @return CastlingAvailability
	 */
	public CastlingAvailability getCastlingAvailability() {
		return castlingAvailability;
	}

	/**
	 * @return Liste aller Spielzustände, welche in diesem Spiel bisher bekannt
	 *         waren
	 */
	public List<HistoricalBoardState> getHistory() {
		return history;
	}

	/**
	 * @return den letzten Spielzustand
	 * @throws IllegalStateException
	 *             falls die Zustandshistorie noch leer ist
	 */
	public HistoricalBoardState getLastBoardState() {
		if (history.isEmpty()) {
			throw new IllegalStateException("Die Historie ist noch leer");
		}

		return history.get(history.size() - 1);
	}

	/**
	 * @return Der letzte ausgeführte Zug
	 */
	public Move getLastMove() {
		if (history == null || history.isEmpty()) {
			return null;
		}
		return history.get(history.size() - 1).getMoveToNextState();
	}

	/**
	 * @return Aktueller Zustand des Spiels
	 */
	public GameState getGameState() {
		return gameState;
	}

	/**
	 * Gibt den Gewinner der Partie zurück. Sollte erst aufgerufen werden, wenn der
	 * Zustand des Spiels auch einen Gewinner zulässt
	 *
	 * @return Gewinner oder null bei unentschieden/wenn das Spiel noch läuft
	 */
	public Player getWinner() {
		if (winnerIndex == -1) {
			return null;
		}
		return players[winnerIndex];
	}

	/**
	 * @return Der Spieler, der gerade am Zug ist
	 */
	public Player getCurrentPlayer() {
		return players[currentPlayerIndex];
	}

	/**
	 * @return Der Spieler, der als Nächstes am Zug ist
	 */
	public Player getNextPlayer() {
		int index = (currentPlayerIndex + 1) % 2;
		return players[index];
	}

	/**
	 * @return die beiden Spieler, wobei Weiß an Index 0 und Schwarz an Index 1 ist
	 */
	public Player[] getPlayers() {
		return new Player[]{players[0], players[1]};
	}

	/**
	 * @return Anzahl an Halbzügen (max Runde * 2 - 1)
	 */
	public int getHalfMoveClock() {
		return halfMoveClock;
	}

	/**
	 * @return Anzahl an ganzen Zügen (Runden)
	 */
	public int getFullMoveClock() {
		return fullMoveClock;
	}

	/**
	 * @return Die Zeit, die der aktuelle Spieler am Anfang dieses Zuges noch hatte
	 */
	public int getPlayerTimeAtStartOfMove() {
		return playerTimeAtStartOfMove;
	}

	/**
	 * @return Farbe des Spielers, der gerade im Schach steht. Null wenn niemand im
	 *         Schach steht.
	 */
	public Color getColorInCheck() {
		return colorInCheck;
	}

	/**
	 * @return Index des Spielers der gewonnen hat im Spielerarray
	 */
	public int getWinnerIndex() {
		return winnerIndex;
	}

	/**
	 * Generiert eine Zeichenkette, die die Figurpositionen, Rochadeoptionen und
	 * aktuellen Spieler eindeutig repräsentiert. Zwei verschiedene Zustände haben
	 * auch verschiedene Zeichenketten. Dies ist nützlich um herauszufinden, wie oft
	 * ein Zustand schon erreicht wurde.
	 *
	 * @return String der diesen Zustand gut repräsentiert
	 */
	public String getStateString() {
		StringBuilder stringBuilder = new StringBuilder();
		for (Piece[] pieces : pieceGrid) {
			for (Piece piece : pieces) {
				stringBuilder.append(piece == null ? "x" : piece.getFenSymbol());
			}
		}
		stringBuilder.append(castlingAvailability);
		stringBuilder.append(currentPlayerIndex);
		return stringBuilder.toString();
	}

	/**
	 * @return den Startzustand des Spiels, falls es nicht das Standardbrett war. In
	 *         diesem Fall null
	 */
	public FENRecord getNonStandardStartState() {
		return nonStandardStartState;
	}

	/**
	 * Konstruiert den letzten Zug in der Standard Algebraic Notation.
	 *
	 * @return den letzten Zug in SAN
	 * @throws IllegalStateException
	 *             falls noch kein Zug ausgeführt wurde
	 */
	public String getLastMoveAlgebraicNotation() {
		if (history.isEmpty()) {
			throw new IllegalStateException("Es wurde noch kein Zug ausgeführt");
		}

		StringWriter san = new StringWriter();
		Checking checking = gameState == GameState.END_CHECKMATE
				? Checking.CHECKMATE
				: colorInCheck != null ? Checking.CHECK : Checking.NONE;
		AlgebraicNotationMoveGenerator.writeHalfMove(getLastBoardState(), checking, new PrintWriter(san));
		return san.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		Board board = (Board) o;
		return currentPlayerIndex == board.currentPlayerIndex && winnerIndex == board.winnerIndex
				&& Objects.deepEquals(players, board.players)
				&& Objects.equals(castlingAvailability, board.castlingAvailability)
				&& Objects.deepEquals(history, board.history) && Objects.deepEquals(pieceGrid, board.pieceGrid)
				&& gameState == board.gameState;
	}

	@Override
	public int hashCode() {
		return Objects.hash(Arrays.hashCode(players), currentPlayerIndex, castlingAvailability, history,
				Arrays.deepHashCode(pieceGrid), gameState, winnerIndex);
	}

	/**
	 * @return Tiefe Kopie des Spieler arrays. Zur Sicherheit damit die AI nicht die
	 *         Spielerzeit zerstören kann
	 */
	private Player[] getPlayerArrayCopy() {
		Player[] newPlayers = new Player[players.length];
		for (int i = 0; i < players.length; i++) {
			newPlayers[i] = new Player(players[i].getName(), players[i].getColor(), players[i].getRemainingTime());
		}
		return newPlayers;
	}

	/**
	 * @return Kopie des Boards mit separatem Piecegrid und Movehistory, sodass sich
	 *         Änderungen nicht auf das echte Spiel übertragen, wenn die AI faxen
	 *         macht
	 */
	public Board getBoardDeepCopy() {
		return new Board(getUnmodifiablePieceGrid(), getPlayerArrayCopy(), currentPlayerIndex,
				new ArrayList<HistoricalBoardState>(history), castlingAvailability, gameState, halfMoveClock,
				fullMoveClock);
	}

	/**
	 * Stellt die verbleibende Zeit der Spieler nach einem Undo wieder her.
	 * <p>
	 * Wenn die Historie leer ist oder nur einen Eintrag enthält, werden beide
	 * Spielerzeiten auf die Anfangszeit zurückgesetzt. Andernfalls werden die
	 * Zeiten aus den letzten beiden Einträgen der Historie wiederhergestellt.
	 */
	public void restoreTimeForUndo() {
		if (history.isEmpty()) {
			getCurrentPlayer().setRemainingTime(initialTime);
			getNextPlayer().setRemainingTime(initialTime);
		} else if (history.size() == 1) {
			getCurrentPlayer().setRemainingTime(initialTime);
			getNextPlayer().setRemainingTime(history.get(0).getPlayerRemainingTime());
		} else {
			getNextPlayer().setRemainingTime(history.get(history.size() - 1).getPlayerRemainingTime());
			getCurrentPlayer().setRemainingTime(history.get(history.size() - 2).getPlayerRemainingTime());
		}
	}
}
