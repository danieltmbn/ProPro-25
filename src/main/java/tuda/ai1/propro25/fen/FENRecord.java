/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.fen;

import java.util.Arrays;
import java.util.Objects;
import tuda.ai1.propro25.model.CastlingAvailability;
import tuda.ai1.propro25.model.Color;
import tuda.ai1.propro25.model.Coordinate;
import tuda.ai1.propro25.model.piece.Piece;

/**
 * Ein {@link FENRecord} beschreibt einen vollständigen Spielzustand, sodass
 * jedes Spiel vollständig rekonstruiert und fortgesetzt werden kann.
 * <p>
 * Für das Laden eines Records, siehe {@link FENParser}
 * <p>
 */
public final class FENRecord {
	private final Piece[][] board;
	private final Color activeColor;
	private final CastlingAvailability castlingAvailability;
	private final Coordinate enPassantTarget;
	private final int halfMoveClock;
	private final int fullMoveClock;

	/**
	 * Erstellt einen neuen {@link FENRecord}.
	 * 
	 * @param board
	 *            die Platzierung der Figuren, leere Felder sind mit null zu
	 *            befüllen. Das Array speichert die Spalten als Sub-Arrays.
	 * @param activeColor
	 *            die Farbe des Teams, das aktuell am Zug ist
	 * @param castlingAvailability
	 *            die Rochadenrechte der Spieler, siehe {@link CastlingAvailability}
	 * @param enPassantTarget
	 *            das Feld, das im letzten Zug von einem Bauern übersprungen wurde
	 *            oder null, falls der letzte Zug nicht von einem Bauern war. Auch
	 *            wenn dieses Attribut nicht null ist, kann trotzdem ein
	 *            En-Passant-Angriff temporär blockiert sein.
	 * @param halfMoveClock
	 *            die Anzahl der Halbzüge, die seit dem letzten Bauernzug oder
	 *            Schlagen einer Figur vergangen sind
	 * @param fullMoveClock
	 *            die Anzahl der vollständigen Züge seit Spielbeginn der nächsten
	 *            Runde, also 1 in der Startstellung. Wird erhöht, wenn Schwarz
	 *            seinen Zug beendet hat
	 */
	public FENRecord(Piece[][] board, Color activeColor, CastlingAvailability castlingAvailability,
			Coordinate enPassantTarget, int halfMoveClock, int fullMoveClock) {
		if (board == null) {
			throw new IllegalArgumentException("board darf nicht null sein");
		}

		if (activeColor == null) {
			throw new IllegalArgumentException("activeColor darf nicht null sein");
		}

		if (castlingAvailability == null) {
			throw new IllegalArgumentException("castlingAvailability darf nicht null sein");
		}

		if (halfMoveClock < 0) {
			throw new IllegalArgumentException("halfMoveClock darf nicht negativ sein");
		}

		if (fullMoveClock < 1) {
			throw new IllegalArgumentException("fullMoveClock darf nicht negativ oder 0 sein");
		}

		this.board = board;
		this.activeColor = activeColor;
		this.castlingAvailability = castlingAvailability;
		this.enPassantTarget = enPassantTarget;
		this.halfMoveClock = halfMoveClock;
		this.fullMoveClock = fullMoveClock;
	}

	/**
	 * Erstellt eine tiefe Kopie des Brettzustands
	 *
	 * @return eine tiefe Kopie des Brettzustands
	 */
	public Piece[][] deepCopyBoard() {
		Piece[][] deeperCopy = new Piece[board.length][];
		for (int i = 0; i < board.length; i++) {
			deeperCopy[i] = board[i].clone();
		}
		return deeperCopy;
	}

	public Piece[][] board() {
		return board;
	}

	public Color activeColor() {
		return activeColor;
	}

	public CastlingAvailability castlingAvailability() {
		return castlingAvailability;
	}

	public Coordinate enPassantTarget() {
		return enPassantTarget;
	}

	public int halfMoveClock() {
		return halfMoveClock;
	}

	public int fullMoveClock() {
		return fullMoveClock;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;

		// muss überschrieben werden, um Arrays.deepEquals zu verwenden

		FENRecord fenRecord = (FENRecord) o;
		return halfMoveClock == fenRecord.halfMoveClock && fullMoveClock == fenRecord.fullMoveClock
				&& Arrays.deepEquals(board, fenRecord.board) && activeColor == fenRecord.activeColor
				&& Objects.equals(enPassantTarget, fenRecord.enPassantTarget)
				&& castlingAvailability.equals(fenRecord.castlingAvailability);
	}

	@Override
	public int hashCode() {
		// muss überschrieben werden, um Arrays.deepHashCode zu verwenden
		int result = Arrays.deepHashCode(board);
		result = 31 * result + activeColor.hashCode();
		result = 31 * result + castlingAvailability.hashCode();
		result = 31 * result + Objects.hashCode(enPassantTarget);
		result = 31 * result + halfMoveClock;
		result = 31 * result + fullMoveClock;
		return result;
	}

	@Override
	public String toString() {
		return "FENRecord[" + "board=" + Arrays.deepToString(board) + ", " + "activeColor=" + activeColor + ", "
				+ "castlingAvailability=" + castlingAvailability + ", " + "enPassantTarget=" + enPassantTarget + ", "
				+ "halfMoveClock=" + halfMoveClock + ", " + "fullMoveClock=" + fullMoveClock + ']';
	}

}
