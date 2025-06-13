/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.ai;

import java.util.List;
import java.util.Random;
import tuda.ai1.propro25.model.Board;
import tuda.ai1.propro25.model.Color;
import tuda.ai1.propro25.model.Move;
import tuda.ai1.propro25.model.Player;
/**
 * Diese Klasse erweitert die `AIOpponent`-Elternklasse und stellt einen
 * **echten zufälligen** Gegner (AI) dar. Der Gegner trifft Züge, indem er
 * zufällig eine Spielfigur mit möglichen Zügen auswählt und dann einen
 * zufälligen Zug mit dieser Figur ausführt.
 */
public class TrueRandomOpponent extends AIOpponent {

	/**
	 * Konstruktor für einen echten zufälligen Gegner.
	 *
	 */
	public TrueRandomOpponent(String name, Color color, int remainingTime) {
		super(name, color, remainingTime);
	}

	/**
	 * Konstruktor für einen echten zufälligen Gegner.
	 *
	 */
	public TrueRandomOpponent(Player player) {
		this(player == null ? null : player.getName(), player == null ? null : player.getColor(),
				player == null ? -1 : player.getRemainingTime());
	}

	/**
	 * Berechnet den nächsten Zug des Gegners. Der Gegner wählt zufällig eine
	 * Spielfigur und einen gültigen Zug aus, solange noch Zeit übrig ist.
	 * 
	 * @param board
	 *            Das aktuelle Spielbrett, das die Position der Spielfiguren und
	 *            deren mögliche Züge enthält.
	 * @return Der nächste gültige Zug des Gegners oder `null`, falls keine gültigen
	 *         Züge verfügbar sind oder die Zeit abgelaufen ist.
	 */
	@Override
	public Move calculateNextMove(Board board) {
		if (getRemainingTime() <= 0) {
			return null;
		}
		try {
			List<Move> allMoves = board.findAllLegalMoves();
			if (allMoves.isEmpty()) {
				return null;
			}
			// Füge eine kurze Verzögerung hinzu, um das Verhalten angenehmer zu gestalten
			Thread.sleep(1);
			Random rand = new Random();
			return allMoves.get(rand.nextInt(allMoves.size()));
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
		}
		// Gibt null wieder falls der try-Block nicht durchläuft
		return null;

	}

	@Override
	public String getAIConfigString() {
		return "TrueRandomOpponent{}";
	}

	@Override
	public String toString() {
		return "TrueRandomOpponent{" + getColor() + "}";
	}

}
