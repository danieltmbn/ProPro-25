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
	
	public String getAlgebraicNotation() {
		String [] spalten = {"a", "b", "c", "d", "e", "f", "g", "h"}; //Speichern die Buchstaben der Spalte
		
		//Uberprueft ob das Argument valid ist
		if(file < 0 || file > 7 || rank < 0 || rank > 7) {
			throw new IllegalArgumentException("Invalid Koordinat");
		}
		
		String spaltenBuchstaben = spalten [file]; //Umwandel file um Buchstaben
		int zeileNummer = rank + 1; //Rank fang von 0 an. wir addieren 1, sodass es von 0 bis 8 laeuft
		
		return spaltenBuchstaben + zeileNummer; //z.B file = 1; rank = 2. liefert "b3" als String zurueck
	}

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
