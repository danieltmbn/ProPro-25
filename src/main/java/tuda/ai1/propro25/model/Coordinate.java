/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.model;

import java.util.Objects;

/**
 * Coordinates bzw. Koordinaten sind ein einfaches Datenpaar aus rank (Zeile)
 * und File (Linien)
 */
public final class Coordinate {
	private final int file;
	private final int rank;

	/**
	 * @param file
	 *            Spalte/Linie
	 * @param rank
	 *            Reihe/Zeile
	 */
	public Coordinate(int file, int rank) {
		this.file = file;
		this.rank = rank;
	}
	public int getFile() {
		return file;
	}

	public int getRank() {
		return rank;
	}

	// TODO: Aufgabe 1.2

	// TODO: Aufgabe 1.3

	// TODO: Aufgabe 1.4

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null || obj.getClass() != this.getClass())
			return false;
		var that = (Coordinate) obj;
		return this.file == that.file && this.rank == that.rank;
	}

	@Override
	public int hashCode() {
		return Objects.hash(file, rank);
	}

}
