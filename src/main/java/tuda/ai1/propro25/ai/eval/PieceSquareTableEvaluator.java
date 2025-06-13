/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.ai.eval;

import tuda.ai1.propro25.model.Board;
import tuda.ai1.propro25.model.piece.Piece;

public class PieceSquareTableEvaluator implements BoardEvaluator {

	// Diese Werte stammen von
	// https://www.chessprogramming.org/Simplified_Evaluation_Function und sind
	// zeilenbasiert
	private final int[][] pawn = {{0, 0, 0, 0, 0, 0, 0, 0}, {50, 50, 50, 50, 50, 50, 50, 50},
			{10, 10, 20, 30, 30, 20, 10, 10}, {5, 5, 10, 25, 25, 10, 5, 5}, {0, 0, 0, 20, 20, 0, 0, 0},
			{5, -5, -10, 0, 0, -10, -5, 5}, {5, 10, 10, -20, -20, 10, 10, 5}, {0, 0, 0, 0, 0, 0, 0, 0}};
	private final int[][] knight = {{-50, -40, -30, -30, -30, -30, -40, -50}, {-40, -20, 0, 0, 0, 0, -20, -40},
			{-30, 0, 10, 15, 15, 10, 0, -30}, {-30, 5, 15, 20, 20, 15, 5, -30}, {-30, 0, 15, 20, 20, 15, 0, -30},
			{-30, 5, 10, 15, 15, 10, 5, -30}, {-40, -20, 0, 5, 5, 0, -20, -40},
			{-50, -40, -30, -30, -30, -30, -40, -50}};
	private final int[][] bishop = {{-20, -10, -10, -10, -10, -10, -10, -20}, {-10, 0, 0, 0, 0, 0, 0, -10},
			{-10, 0, 5, 10, 10, 5, 0, -10}, {-10, 5, 5, 10, 10, 5, 5, -10}, {-10, 0, 10, 10, 10, 10, 0, -10},
			{-10, 10, 10, 10, 10, 10, 10, -10}, {-10, 5, 0, 0, 0, 0, 5, -10}, {-20, -10, -10, -10, -10, -10, -10, -20}};
	private final int[][] rook = {{0, 0, 0, 0, 0, 0, 0, 0}, {5, 10, 10, 10, 10, 10, 10, 5}, {-5, 0, 0, 0, 0, 0, 0, -5},
			{-5, 0, 0, 0, 0, 0, 0, -5}, {-5, 0, 0, 0, 0, 0, 0, -5}, {-5, 0, 0, 0, 0, 0, 0, -5},
			{-5, 0, 0, 0, 0, 0, 0, -5}, {0, 0, 0, 5, 5, 0, 0, 0}};
	private final int[][] queen = {{-20, -10, -10, -5, -5, -10, -10, -20}, {-10, 0, 0, 0, 0, 0, 0, -10},
			{-10, 0, 5, 5, 5, 5, 0, -10}, {-5, 0, 5, 5, 5, 5, 0, -5}, {0, 0, 5, 5, 5, 5, 0, -5},
			{-10, 5, 5, 5, 5, 5, 0, -10}, {-10, 0, 5, 0, 0, 0, 0, -10}, {-20, -10, -10, -5, -5, -10, -10, -20}};
	private final int[][] king = {{-30, -40, -40, -50, -50, -40, -40, -30}, {-30, -40, -40, -50, -50, -40, -40, -30},
			{-30, -40, -40, -50, -50, -40, -40, -30}, {-30, -40, -40, -50, -50, -40, -40, -30},
			{-20, -30, -30, -40, -40, -30, -30, -20}, {-10, -20, -20, -20, -20, -20, -20, -10},
			{20, 20, 0, 0, 0, 0, 20, 20}, {20, 30, 10, 0, 0, 10, 30, 20}};
	private final int[][] kingEnd = {{-50, -40, -30, -20, -20, -30, -40, -50}, {-30, -20, -10, 0, 0, -10, -20, -30},
			{-30, -10, 20, 30, 30, 20, -10, -30}, {-30, -10, 30, 40, 40, 30, -10, -30},
			{-30, -10, 30, 40, 40, 30, -10, -30}, {-30, -10, 20, 30, 30, 20, -10, -30},
			{-30, -30, 0, 0, 0, 0, -30, -30}, {-50, -30, -30, -30, -30, -30, -30, -50}};

	/**
	 * Evaluiert die aktuelle Spielposition, indem die Felder, auf denen sich
	 * Figuren befinden nach einer Tabelle bewertet werden. Ein negativer Wert
	 * bedeutet also, dass die Figuren des Gegners vermutlich auf besseren Feldern
	 * stehen als die eigenen. Bewertet Material nur indirekt, indem der Verlust von
	 * "positiven" Figuren bestraft wird (Wert schlagartig deutlich negativer)
	 *
	 * @param board
	 *            Board, auf dem der aktuelle Zustand evaluiert werden soll
	 * @return Bewertung der Figurenanordnung aus Sicht des aktuellen Spielers
	 */
	@Override
	public double evaluate(Board board) {
		boolean isEndgame = board.getHistory().size() > 50;
		int ownSum = 0;
		int enemySum = 0;
		for (int file = 0; file < Board.BOARD_SIZE; file++) {
			for (int rank = 0; rank < Board.BOARD_SIZE; rank++) {
				Piece piece = board.getPiece(file, rank);
				if (piece == null) {
					continue;
				}
				int[][] pieceTable = switch (piece.getAlgebraicNotationSymbol()) {
					case 'P' -> pawn;
					case 'K' -> (isEndgame ? kingEnd : king);
					case 'B' -> bishop;
					case 'R' -> rook;
					case 'Q' -> queen;
					case 'N' -> knight;
					default ->
						throw new IllegalArgumentException("Unsupported symbol: " + piece.getAlgebraicNotationSymbol());
				};
				int lookupRank = rank;
				if (Character.isUpperCase(piece.getFenSymbol())) {
					// Ist weiß -> umgekehrt schauen
					lookupRank = 7 - rank;
				}
				int pieceEval = pieceTable[lookupRank][file];
				if (piece.getColor() == board.getCurrentPlayer().getColor()) {
					ownSum += pieceEval;
				} else {
					enemySum += pieceEval;
				}
			}
		}
		return (ownSum - enemySum) * 0.01;
	}
}
