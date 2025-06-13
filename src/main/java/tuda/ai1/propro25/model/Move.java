/* (C) 2025-2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.model;

import java.util.Objects;
import tuda.ai1.propro25.model.piece.Piece;

/**
 * Ein Move ist ein Schachzug, also die Bewegung einer Figur von einem Feld zu
 * einem Anderen
 */
public class Move {
	private final Coordinate from;
	private final Coordinate to;
	private final MoveType type;
	private final Piece piece;

	// Bei Schlägen die geschlagene Figur, bei Rochaden der Turm
	private final Piece involvedPiece;

	// Für Umwandlung die neue Figur
	private final Piece promotionPiece;

	/**
	 * Konstruktor für simple Moves (Typ NORMAL)
	 *
	 * @param piece
	 *            Die Figur, die diesen Zug ausführen möchte (sollte auf dem Feld
	 *            "from" stehen)
	 * @param from
	 *            Startfeld
	 * @param to
	 *            Zielfeld
	 */
	public Move(Piece piece, Coordinate from, Coordinate to) {
		this(piece, from, to, MoveType.NORMAL, null, null);
	}

	/**
	 * Konstruktor, welcher erlaubt den Zugtyp zu wählen. Zugtyp kann danach
	 * validiert werden und wenn der Zugtyp in der Regel weitere Informationen
	 * erfordert, kann eine IllegalArgumentException geworfen werden
	 *
	 * @param piece
	 *            Die Figur, die diesen Zug ausführen möchte (sollte auf dem Feld
	 *            "from" stehen)
	 * @param from
	 *            Startfeld
	 * @param to
	 *            Zielfeld
	 * @param type
	 *            Zugtyp (MoveType)
	 */
	public Move(Piece piece, Coordinate from, Coordinate to, MoveType type) {
		this(piece, from, to, type, null, null);
	}

	/**
	 * Konstruktor für schlagende Züge wie z.B. {@link MoveType#EN_PASSANT} oder
	 * {@link MoveType#CAPTURE} und für Rochaden, z.B:
	 * {@link MoveType#CASTLING_KINGSIDE}. Zugtyp kann danach validiert werden und
	 * wenn der Zugtyp in der Regel weitere Informationen erfordert, kann eine
	 * IllegalArgumentException geworfen werden.
	 *
	 * @param piece
	 *            Die Figur, die diesen Zug ausführen möchte (sollte auf dem Feld
	 *            "from" stehen)
	 * 
	 * @param from
	 *            Startfeld
	 * @param to
	 *            Zielfeld
	 * @param type
	 *            Typ des Zuges
	 * @param involvedPiece
	 *            Die Figur, welche geschlagen wurde, oder der Turm bei der Rochade
	 */
	public Move(Piece piece, Coordinate from, Coordinate to, MoveType type, Piece involvedPiece) {
		this(piece, from, to, type, involvedPiece, null);
	}

	/**
	 * Konstruktor für Promotionen von Bauern ({@link MoveType#PROMOTION})
	 *
	 * @param piece
	 *            Die Bauer, der diesen Zug ausführen möchte (sollte auf dem Feld
	 *            "from" stehen)
	 * 
	 * @param from
	 *            Startfeld
	 * @param to
	 *            Zielfeld
	 * @param promotionPiece
	 *            Neue Figur, in die der Bauer umgewandelt wurde
	 */
	public Move(Piece piece, Coordinate from, Coordinate to, Piece promotionPiece) {
		this(piece, from, to, MoveType.PROMOTION, null, promotionPiece);
	}

	/**
	 * Konstruktor der es erlaubt, alle Felder zu setzen. Im Regelfall sollte dieser
	 * Konstruktor nur für Bauern genutzt werden, die gleichzeitig eine Figur
	 * schlagen und sich umwandeln ({@link MoveType#CAPTURE_PROMOTION}), da es für
	 * andere Fälle simplere Konstruktoren gibt. Zugtyp kann danach validiert werden
	 * und wenn der Zugtyp in der Regel weitere Informationen erfordert, kann eine
	 * IllegalArgumentException geworfen werden
	 *
	 * @param piece
	 *            Die Figur, die diesen Zug ausführen möchte (sollte auf dem Feld
	 *            "from" stehen)
	 * @param from
	 *            Startfeld
	 * @param to
	 *            Zielfeld
	 * @param type
	 *            Art des Zuges
	 * @param involvedPiece
	 *            Geschlagene Figur die sich danach nicht mehr auf dem Brett
	 *            befindet
	 * @param promotionPiece
	 *            Neue Figur, in die der Bauer umgewandelt wurde
	 */
	public Move(Piece piece, Coordinate from, Coordinate to, MoveType type, Piece involvedPiece, Piece promotionPiece) {
		if (piece == null) {
			throw new IllegalArgumentException("Piece darf nicht null sein!");
		}
		if (from == null || to == null) {
			throw new IllegalArgumentException(
					"Start- und Zielkoordinaten müssen beide angegeben sein und dürfen nicht null sein!");
		}
		if (type == null) {
			throw new IllegalArgumentException("MoveType darf nicht null sein!");
		}
		if ((type == MoveType.CAPTURE || type == MoveType.EN_PASSANT || type == MoveType.CAPTURE_PROMOTION
				|| type == MoveType.CASTLING_KINGSIDE || type == MoveType.CASTLING_QUEENSIDE)
				&& involvedPiece == null) {
			throw new IllegalArgumentException(
					"Für die MoveTypes CAPTURE, CAPTURE_PROMOTION, CASTLING_[KING,QUEEN]SIDE und EN_PASSANT darf involvedPiece nicht null sein!");
		}
		if ((type == MoveType.PROMOTION || type == MoveType.CAPTURE_PROMOTION) && promotionPiece == null) {
			throw new IllegalArgumentException(
					"Für die MoveTypes PROMOTION und CAPTURE_PROMOTION dürfen promotedPawn und promotionPiece nicht null sein!");
		}
		if (piece == involvedPiece || piece == promotionPiece) {
			// Wir testen für gleichheit mittels "==" da es hier wirklich um die
			// Objektreferenz der Figur geht
			throw new IllegalArgumentException(
					"Figuren können in einem Zug nur einmal referenziert werden! piece darf also nicht gleich involvedPiece oder promotionPiece sein!");
		}
		if (promotionPiece != null && promotionPiece.equals(involvedPiece)) {
			throw new IllegalArgumentException("PromotionPiece und involvedPiece dürfen nicht die gleiche Figur sein!");
		}
		this.from = from;
		this.to = to;
		this.type = type;
		this.piece = piece;
		this.involvedPiece = involvedPiece;
		this.promotionPiece = promotionPiece;
	}

	public Coordinate getFrom() {
		return from;
	}

	public Coordinate getTo() {
		return to;
	}

	public MoveType getType() {
		return type;
	}

	public Piece getPiece() {
		return piece;
	}

	public Piece getInvolvedPiece() {
		return involvedPiece;
	}

	public Piece getPromotionPiece() {
		return promotionPiece;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Move move = (Move) o;
		return Objects.equals(from, move.from) && Objects.equals(to, move.to) && type == move.type
				&& Objects.equals(piece, move.piece) && Objects.equals(involvedPiece, move.involvedPiece)
				&& Objects.equals(promotionPiece, move.promotionPiece);
	}

	@Override
	public int hashCode() {
		return Objects.hash(from, to, type, piece, involvedPiece, promotionPiece);
	}

	@Override
	public String toString() {
		return "Move (" + type + ") " + piece + ": " + from + "->" + to + ", x:" + involvedPiece + ", prom:"
				+ promotionPiece;
	}
}
