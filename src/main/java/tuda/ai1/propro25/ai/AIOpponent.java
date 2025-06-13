/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.ai;

import tuda.ai1.propro25.model.*;

/**
 * Elternklasse für Computergegner
 */
public abstract class AIOpponent extends Player {

	/**
	 * Neuer Computergegner (AI), welcher auch ein Spieler ist
	 * 
	 * @param name
	 *            Name des Spielers
	 * @param color
	 *            Figurfarbe
	 * @param remainingTime
	 *            übrige Spielzeit
	 */
	public AIOpponent(String name, Color color, int remainingTime) {
		super(name, color, remainingTime);
	}

	/**
	 * Gibt den Zug zurück, den die KI mit ihrer jeweiligen Methode berechnet hat.
	 * Die KI kopiert dabei das Brett, um sicherzustellen, dass es nicht
	 * versehentlich modifiziert wird und ruft dann erst die interne Methode zum
	 * Berechnen auf.
	 * 
	 * @param deepCopyable
	 *            Das derzeitige Brett
	 * @return Bevorzugter Zug der KI
	 */
	public Move getNextMove(DeepCopyable deepCopyable) {
		return calculateNextMove(deepCopyable.getBoardDeepCopy());
	}

	/**
	 * Berechnet den nächsten Zug, den diese KI gerne ausführen möchte.
	 *
	 * @param board
	 *            Das derzeitige Brett, muss frei zu bearbeiten sein
	 * @return Bevorzugter Zug
	 */
	abstract Move calculateNextMove(Board board);

	/**
	 * @return String, der die Einstellungen dieser AI repräsentiert, aber auslässt,
	 *         mit welcher Figurenfarbe die AI gespielt hat.
	 */
	public abstract String getAIConfigString();

}
