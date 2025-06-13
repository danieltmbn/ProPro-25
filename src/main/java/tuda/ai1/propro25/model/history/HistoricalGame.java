/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.model.history;

import java.time.LocalDateTime;
import tuda.ai1.propro25.model.Board;

/**
 * HistoricalGame stellt ein bereits gespieltes Spiel dar. Es beinhaltet ein
 * aktuelles Abbild des Boards und zusätzliche Informationen wie z.B. die Zeit
 * zu der das Spiel gestartet wurde und eine ID. Ein Historical Game muss dabei
 * nicht unbedingt ein abgeschlossenes Spiel sein - der Zustand des Boards ist
 * beliebig!
 *
 * @param gameName
 *            Name dieser Partie
 * @param time
 *            Start- oder Endzeit dieser Partie
 * @param board
 *            Spielbrett mit aktuellem, eventuell fertigem Spielzustand und
 *            History
 */
public record HistoricalGame(String gameName, LocalDateTime time, Board board) {

	public HistoricalGame {
		if (gameName == null) {
			throw new IllegalArgumentException("gameName darf nicht null sein");
		}

		if (time == null) {
			throw new IllegalArgumentException("time darf nicht null sein");
		}

		if (board == null) {
			throw new IllegalArgumentException("board darf nicht null sein");
		}
	}

}
