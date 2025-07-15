/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.fen;



import tuda.ai1.propro25.model.Board;
import tuda.ai1.propro25.model.CastlingAvailability;
import tuda.ai1.propro25.model.Coordinate;
import tuda.ai1.propro25.model.piece.Piece;
import tuda.ai1.propro25.model.Color;


/**
 * Instanzen dieser Klasse sind dafür verantwortlich, einen {@link FENRecord} in
 * einen FEN-String zu serialisieren.
 *
 * @see FENSerializer#serializeRecord(FENRecord)
 */
public class FENSerializer {

	/**
	 * Serialisiert den übergebenen {@link FENRecord} zu einem vollständigen
	 * FEN-String. Es werden nur Bretter mit den Abmessungen
	 * {@link Board#BOARD_SIZE} x {@link Board#BOARD_SIZE} unterstützt.
	 *
	 * @param record der zu serialisierende Record
	 * @return den Record in FEN
	 */
	public String serializeRecord(FENRecord record) {
		// TODO: Aufgabe 3.5
		
		return  serializeBoard(record.board()) + " " + serializeActiveColor(record.activeColor())
				+ " " + serializeCastlingAvailability(record.castlingAvailability()) + " " + 
				serializeEnPassantTarget(record.enPassantTarget()) + " " + record.halfMoveClock() + 
				" " + record.fullMoveClock();
	}

	// TODO: Aufgabe 3.1
	
	/**
	 * This converts a 2D array of Piece objects into the FEN string representation
	 * of the board's current state(first field of FEN).
	 * 
	 * @param board 8x8 array where it holds a piece or null.
	 * @return String representation of the board in FEN notation
	 * @throws IllegalArgumentException if board is null or not 8x8.
	 */
	String serializeBoard(Piece[][] board) {
		
		
		StringBuilder fen = new StringBuilder();

	   
	    for (int rank = 7; rank >= 0; rank--) {
	        int empty = 0;

	       
	        for (int file = 0; file < 8; file++) {
	            Piece piece = board[file][rank];

	            if (piece == null) {
	                empty++; 
	            } else {
	                if (empty > 0) {
	                    fen.append(empty); 
	                    empty = 0;
	                }
	                fen.append(piece.getFenSymbol()); 
	            }
	        }

	        if (empty > 0) {
	            fen.append(empty); 
	        }

	        if (rank > 0) {
	            fen.append('/'); 
	        }
	    }

	    return fen.toString();
	}
	

	// TODO: Aufgabe 3.2
	
	/**
	 * Converts the current active player colour to FEN format.
	 * 
	 * @param The color that is currently to move
	 * @return "w" if white, "b" if black
	 */
	
	String serializeActiveColor(Color color) {
		return color == Color.WHITE ? "w" : "b";
		}

	// TODO: Aufgabe 3.3
    
    /**
     * Converts the castling rights into FEN 
     * @param is the c which is thw castling rights object
     * @return It returns the castling string
     */

    String serializeCastlingAvailability(CastlingAvailability castlingAvailability) {
    	
    	String fen = "";
    	
    	if(castlingAvailability.whiteCastleKingSide()) {
    		fen += "K";
    	}
    	if(castlingAvailability.whiteCastleQueenSide()) {
    		fen += "Q";
    	}
    	if(castlingAvailability.blackCastleKingSide()) {
    		fen += "k";
    	}
    	if(castlingAvailability.blackCastleQueenSide()) {
    		fen += 'q';
    	}
    	
    	if(fen.equals("")) {
    		return "-";
    	}
    	
    	return fen;
    	
    }


	// TODO: Aufgabe 3.4
    
    /**
     * It converts the En passant target coordinate to its FEN.
     * @param the enPassantTarget the target coordinate, or null if that's not available.
     * @return the notation as a string or - if not possible.
     */
    
    String serializeEnPassantTarget(Coordinate enPassantTarget) {
    	if(enPassantTarget == null) {
    		return "-";
    	} else {
    		return enPassantTarget.getAlgebraicNotation();
    	}
    	
    }
    
}
