/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tuda.ai1.propro25.ChessTestUtil.getCoordinate;

import org.junit.jupiter.api.Test;
import tuda.ai1.propro25.model.Color;
import tuda.ai1.propro25.model.Move;
import tuda.ai1.propro25.model.MoveType;
import tuda.ai1.propro25.model.piece.*;

class MoveComparatorTest {

	private final MoveComparator comparator = new MoveComparator();

	@Test
	void testCompareLessThan() {
		assertTrue(comparator.compare(new Move(new Pawn(Color.WHITE), getCoordinate("a1"), getCoordinate("a2")),
				new Move(new Pawn(Color.WHITE), getCoordinate("a1"), getCoordinate("a2"), MoveType.CAPTURE,
						new Rook(Color.BLACK))) < 0);

		assertTrue(comparator.compare(
				new Move(new Pawn(Color.WHITE), getCoordinate("a1"), getCoordinate("a2"), MoveType.CAPTURE,
						new Rook(Color.BLACK)),
				new Move(new Pawn(Color.WHITE), getCoordinate("a1"), getCoordinate("a2"), MoveType.CAPTURE,
						new Queen(Color.BLACK))) < 0);

		assertTrue(comparator.compare(
				new Move(new Pawn(Color.WHITE), getCoordinate("a1"), getCoordinate("a2"), MoveType.CAPTURE,
						new Queen(Color.BLACK)),
				new Move(new Pawn(Color.WHITE), getCoordinate("a1"), getCoordinate("a2"), MoveType.CAPTURE_PROMOTION,
						new Queen(Color.BLACK), new Bishop(Color.WHITE))) < 0);
	}

	@Test
	void testCompareGreaterThan() {
		assertTrue(comparator.compare(
				new Move(new Pawn(Color.WHITE), getCoordinate("a1"), getCoordinate("a2"), MoveType.CAPTURE,
						new Rook(Color.BLACK)),
				new Move(new Pawn(Color.WHITE), getCoordinate("a1"), getCoordinate("a2"))) > 0);

		assertTrue(comparator.compare(
				new Move(new Pawn(Color.WHITE), getCoordinate("a1"), getCoordinate("a2"), MoveType.CAPTURE,
						new Queen(Color.BLACK)),
				new Move(new Pawn(Color.WHITE), getCoordinate("a1"), getCoordinate("a2"), MoveType.CAPTURE,
						new Rook(Color.BLACK))) > 0);

		assertTrue(comparator.compare(
				new Move(new Pawn(Color.WHITE), getCoordinate("a1"), getCoordinate("a2"), MoveType.CAPTURE_PROMOTION,
						new Queen(Color.BLACK), new Bishop(Color.WHITE)),
				new Move(new Pawn(Color.WHITE), getCoordinate("a1"), getCoordinate("a2"), MoveType.CAPTURE,
						new Queen(Color.BLACK))) > 0);
	}

	@Test
	void testCompareEquals() {
		assertEquals(0, comparator.compare(new Move(new Bishop(Color.BLACK), getCoordinate("e3"), getCoordinate("d4")),
				new Move(new Queen(Color.BLACK), getCoordinate("e3"), getCoordinate("d4"))));

		assertEquals(0,
				comparator.compare(
						new Move(new Bishop(Color.BLACK), getCoordinate("e3"), getCoordinate("d4"), MoveType.CAPTURE,
								new Bishop(Color.WHITE)),
						new Move(new Queen(Color.BLACK), getCoordinate("e3"), getCoordinate("d4"), MoveType.CAPTURE,
								new Knight(Color.WHITE))));

		assertEquals(0,
				comparator.compare(
						new Move(new Pawn(Color.BLACK), getCoordinate("c7"), getCoordinate("c8"),
								MoveType.CAPTURE_PROMOTION, new Rook(Color.BLACK), new Bishop(Color.WHITE)),
						new Move(new Pawn(Color.BLACK), getCoordinate("c7"), getCoordinate("c8"),
								MoveType.CAPTURE_PROMOTION, new Bishop(Color.BLACK), new Rook(Color.WHITE))));
	}

}
