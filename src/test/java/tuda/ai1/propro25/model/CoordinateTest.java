/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CoordinateTest {

	@Test
	void testAlgebraicNotationCorners() {
		assertEquals("a1", new Coordinate(0, 0).getAlgebraicNotation());
		assertEquals("a8", new Coordinate(0, 7).getAlgebraicNotation());
		assertEquals("h1", new Coordinate(7, 0).getAlgebraicNotation());
		assertEquals("h8", new Coordinate(7, 7).getAlgebraicNotation());
	}

	@Test
	void testAlgebraicNotation() {
		assertEquals("b2", new Coordinate(1, 1).getAlgebraicNotation());
		assertEquals("c4", new Coordinate(2, 3).getAlgebraicNotation());
		assertEquals("f3", new Coordinate(5, 2).getAlgebraicNotation());
		assertEquals("e7", new Coordinate(4, 6).getAlgebraicNotation());
	}

	@Test
	void testIsNotOnBoard() {
		assertFalse(new Coordinate(-1, 0).isOnBoard());
		assertFalse(new Coordinate(0, -1).isOnBoard());
		assertFalse(new Coordinate(7, 8).isOnBoard());
		assertFalse(new Coordinate(8, 0).isOnBoard());
		assertFalse(new Coordinate(10, 10).isOnBoard());
		assertFalse(new Coordinate(-20, 10).isOnBoard());
		assertFalse(new Coordinate(20, -10).isOnBoard());
	}

}
