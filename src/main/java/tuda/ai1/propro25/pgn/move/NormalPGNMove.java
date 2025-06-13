/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.pgn.move;

import tuda.ai1.propro25.model.Coordinate;

/**
 * Ein unvollständiger nicht-Rochaden-Zug mit den Informationen wie sie auch in
 * der PGN standen.
 * 
 * @param piece
 *            das Symbol der Figur, die den Zug durchführt
 * @param fromFile
 *            die Spalte des Startfelds oder -1, falls nicht angegeben
 * @param fromRank
 *            die Reihe des Startfelds oder -1, falls nicht angegeben
 * @param to
 *            das Zielfeld
 * @param capture
 *            true genau dann, wenn dieser Zug ein schlagender Zug ist
 * @param promotionPiece
 *            das Symbol der Figur, zu welcher sich ein Bauer in diesem Zug
 *            promotet oder null, falls keine Promotion stattfindet
 * @param checking
 *            Information darüber, ob dieser Zug den König ins Schach stellt
 */
public record NormalPGNMove(char piece, int fromFile, int fromRank, Coordinate to, boolean capture,
		Character promotionPiece, Checking checking) implements PGNMove {

	public NormalPGNMove {
		if (to == null) {
			throw new IllegalArgumentException("to darf nicht null sein");
		}

		if (checking == null) {
			throw new IllegalArgumentException("checking darf nicht null sein");
		}

		if (promotionPiece != null && piece != 'P') {
			throw new IllegalArgumentException(
					"Das promotionPiece darf nur gesetzt sein, wenn es sich um den Zug eines Bauern handelt");
		}
	}

	/**
	 * @return true genau dann, wenn dieser Zug eine Promotion enthält
	 */
	public boolean isPromotion() {
		return promotionPiece != null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (piece != 'P')
			sb.append(piece);
		if (fromFile != -1)
			sb.append((char) (fromFile + 'a'));
		if (fromRank != -1)
			sb.append((char) (fromRank + '1'));
		if (capture)
			sb.append('x');
		sb.append(to.getAlgebraicNotation());
		if (isPromotion())
			sb.append("=").append(promotionPiece);
		sb.append(checking);

		return sb.toString();
	}
}
