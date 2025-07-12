/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.model;

import java.util.Objects;

/**
 * Player repräsentiert die Spieler, welche Schach spielen. Ein Spieler muss
 * dabei nicht unbedingt menschlich sein
 */
public class Player {

	// TODO: Aufgabe 1.5
	private final String name;
	private final Color color;
	private int remainingMilliseconds;
	
	public Player(String name, Color color, int remainingMilliseconds) {
		
		if(name == null || name =="" || color == null || remainingMilliseconds < 0) {
			throw new IllegalArgumentException("Name und color kann nicht null sein und remainingMilliseconds muss groesser als 0!");
		}
		
		this.name = name;
		this.color = color;
		this.remainingMilliseconds = remainingMilliseconds;
	}
	
	/**
	 * Ein Method, um den Namen eines Players zu erhalten
	 * @return liefert Name zurueck
	 */
	
	// TODO: Aufgabe 1.6
	public String getName() {
		return name;
	}
	
	/**
	 * Ein Method, um den Spielerseite eines Players zu erhalten
	 * @return liefert Color zurueck
	 */
	
	public Color getColor() {
		return color;
	}
	
	/**
	 * Ein Method, um die uebrige Zeit eines Players zu erhalten
	 * @return liefert Zeit in Millisekunden zurueck
	 */
	
	public int getRemainingTime() {
		return remainingMilliseconds;
	}

	/**
	 * Setzen die uebrige Zeit von Player.
	 * @param time Zeit in Millisekunden
	 */
	
	// TODO: Aufgabe 1.7
	public void setRemainingTime(int time) {
		if(time < 0) {
			throw new IllegalArgumentException("Parameter time darf nicht negativ sein!");
		}
		this.remainingMilliseconds = time;
	}

	/**
	 * Verringert die verbleibende Zeit des Spielers um die angegebene Dauer in
	 * Millisekunden.
	 *
	 * @param duration
	 *            Die zu subtrahierende Zeit in Millisekunden.
	 * @throws IllegalArgumentException
	 *             wenn die verbleibende Zeit dadurch negativ würde.
	 */
	public void reduceRemainingMilliseconds(int duration) {
		if (remainingMilliseconds < duration) {
			throw new IllegalArgumentException("Die verbleibende Zeit darf nicht negativ werden!");
		}
		this.remainingMilliseconds -= duration;
	}

	/**
	 * Vergleichen zwei Objekte ob sie sind gleich. Zwei Objekte können nur dann gleich sein, 
	 * wenn sie derselben Klasse angehören. Es ist ein Override von Method equals von Klasse Objects
	 * @return liefert ob zwei Objekte gleich sind.
	 */
	
	@Override
	public boolean equals(Object o) {
		// TODO: Aufgabe 1.8
		if(o == null || getClass() != o.getClass()) {
			return false;
		}
		
		Player other = (Player) o;
		
		return Objects.equals(name, other.getName()) 
				&& Objects.equals(color, other.getColor()) 
				&& Objects.equals(remainingMilliseconds, other.getRemainingTime());
	}

	@Override
	public int hashCode() {
		int result = Objects.hashCode(name);
		result = 31 * result + Objects.hashCode(color);
		result = 31 * result + remainingMilliseconds;
		return result;
	}

	@Override
	public String toString() {
		return "Player{" + "name='" + name + '\'' + ", color=" + color + ", remainingMilliseconds="
				+ remainingMilliseconds + '}';
	}
}
