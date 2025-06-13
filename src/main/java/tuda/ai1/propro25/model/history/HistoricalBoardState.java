/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.model.history;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import tuda.ai1.propro25.model.*;
import tuda.ai1.propro25.model.piece.Piece;

/**
 * HistoricalBoardStates sind bereits gesehene Spielzustände mit zusätzlichen
 * Informationen
 */
public class HistoricalBoardState {

	private final int playerIndex;
	private final int playerRemainingTime;
	private final CastlingAvailability castlingAvailability;
	private final int halfMoveClock;
	private final int fullMoveClock;
	private final GameState gameState;
	private final Color colorInCheck;
	private final int winnerIndex;

	private final String stateString;

	private final Piece[][] pieceGrid;
	private final Move moveToNextState;
	private final List<Move> legalMovesInThisState;

	/**
	 * Ein HistoricalMove ist ein Zug, der bereits geschehen ist. Er erweitert Move
	 * mit extra Informationen, um das Spielgeschehen im Nachhinein einfacher
	 * rekonstruieren zu können.
	 * 
	 * @param board
	 *            Das Brett, welches hier repräsentiert werden soll. Es wird
	 *            {@link Board#getUnmodifiablePieceGrid()} aufgerufen, um den
	 *            Zustand der Figuren zu speichern
	 * @param playerIndex
	 *            Der Index des Spielers, welcher in diesem Zustand am Zug war
	 * @param legalMovesInThisState
	 *            Cache für legale Züge, die es in diesem Zustand gab. Kann null
	 *            sein wenn ungewünscht.
	 * @param moveToNextState
	 *            Der Zug, welcher in den nächsten Zustand geführt hat. Kann null
	 *            sein, z.B. wenn dies der letzte bekannte Brettzustand ist.
	 */
	public HistoricalBoardState(Board board, int playerIndex, List<Move> legalMovesInThisState, Move moveToNextState) {
		if (board == null) {
			throw new NullPointerException("Board and CastlingAvailability dürfen nicht null sein!");
		}
		// Für alle anderen Felder akzeptieren wir beliebige Werte da wir nur ein
		// Speicher sind und nicht das Brett
		this.playerRemainingTime = board.getCurrentPlayer().getRemainingTime();
		this.castlingAvailability = board.getCastlingAvailability();
		this.halfMoveClock = board.getHalfMoveClock();
		this.fullMoveClock = board.getFullMoveClock();
		this.pieceGrid = board.getUnmodifiablePieceGrid();
		this.playerIndex = playerIndex;
		this.legalMovesInThisState = legalMovesInThisState;
		this.moveToNextState = moveToNextState;
		this.stateString = board.getStateString();
		this.gameState = board.getGameState();
		this.colorInCheck = board.getColorInCheck();
		this.winnerIndex = board.getWinnerIndex();
	}

	public int getPlayerIndex() {
		return playerIndex;
	}

	public int getPlayerRemainingTime() {
		return playerRemainingTime;
	}

	public CastlingAvailability getCastlingAvailability() {
		return castlingAvailability;
	}

	public int getHalfMoveClock() {
		return halfMoveClock;
	}

	public int getFullMoveClock() {
		return fullMoveClock;
	}

	public Move getMoveToNextState() {
		return moveToNextState;
	}

	public Piece[][] getPieceGrid() {
		return pieceGrid;
	}

	public List<Move> getLegalMovesInThisState() {
		return legalMovesInThisState;
	}

	public String getStateString() {
		return stateString;
	}

	public GameState getGameState() {
		return gameState;
	}

	public Color getColorInCheck() {
		return colorInCheck;
	}

	public int getWinnerIndex() {
		return winnerIndex;
	}

	/**
	 * Gibt zurück, ob dieser Zustand unvollständig ist. Unvollständige Zustände
	 * entstehen, wenn der Zug bekannt ist, der diesen Zustand erzeugt, jedoch
	 * nicht, welche Züge in diesem Zustand legal waren.
	 * 
	 * @return true genau dann, wenn keine legalen Züge bekannt sind
	 */
	public boolean isIncomplete() {
		return legalMovesInThisState == null;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		HistoricalBoardState that = (HistoricalBoardState) o;
		return playerIndex == that.playerIndex && playerRemainingTime == that.playerRemainingTime
				&& halfMoveClock == that.halfMoveClock && fullMoveClock == that.fullMoveClock
				&& winnerIndex == that.winnerIndex && Objects.equals(castlingAvailability, that.castlingAvailability)
				&& gameState == that.gameState && colorInCheck == that.colorInCheck
				&& Objects.equals(stateString, that.stateString) && Objects.deepEquals(pieceGrid, that.pieceGrid)
				&& Objects.equals(moveToNextState, that.moveToNextState)
				&& Objects.equals(legalMovesInThisState, that.legalMovesInThisState);
	}

	@Override
	public int hashCode() {
		return Objects.hash(playerIndex, playerRemainingTime, castlingAvailability, halfMoveClock, fullMoveClock,
				gameState, colorInCheck, winnerIndex, stateString, Arrays.deepHashCode(pieceGrid), moveToNextState,
				legalMovesInThisState);
	}
}
