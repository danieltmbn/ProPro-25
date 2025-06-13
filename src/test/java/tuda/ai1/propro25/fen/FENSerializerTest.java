/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.fen;

import static org.junit.jupiter.api.Assertions.*;
import static tuda.ai1.propro25.ChessTestUtil.*;

import org.junit.jupiter.api.Test;
import tuda.ai1.propro25.model.CastlingAvailability;
import tuda.ai1.propro25.model.Color;
import tuda.ai1.propro25.model.piece.*;

class FENSerializerTest {

	private final FENSerializer serializer = new FENSerializer();

	@Test
	void testSerializeRecord() {
		Piece[][] board = initialBoard();

		FENRecord record = new FENRecord(board, Color.WHITE, new CastlingAvailability(true, true, true, true), null, 0,
				1);

		assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", serializer.serializeRecord(record));
	}

}
