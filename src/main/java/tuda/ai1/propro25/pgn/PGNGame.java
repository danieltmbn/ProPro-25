/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.pgn;

import java.util.List;
import java.util.Map;
import tuda.ai1.propro25.pgn.move.PGNMove;

/**
 * Ein aus PGN geparstes Spiel. Die Züge sind noch nicht vollständig und es gibt
 * keine Garantie, dass alle Informationen konsistent sind bzw. ob die
 * angegebenen Züge so zulässig sind
 *
 * @param tags
 *            die Tags, wobei die Keys die Tag-Namen und die Werte die Tag-Werte
 *            sind
 * @param moves
 *            alle bisherigen Züge des Spiels
 * @param termination
 *            wie das Spiel nach dem letzten angegebenen Zug verblieben ist
 */
public record PGNGame(Map<String, String> tags, List<PGNMove> moves, GameTermination termination) {
}
