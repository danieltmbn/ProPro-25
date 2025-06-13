/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.view.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javafx.scene.image.Image;

/**
 * Diese Klasse ist verantwortlich für das Laden von Schachfiguren-Bildern. Sie
 * ist eine statische Hilfsklasse, die Bilddateien einmal lädt und eine Map von
 * Schachfiguren-Bildern bereitstellt.
 */
public class PieceImageLoader {

	private static final Map<Character, Image> pieceImages = new HashMap<>();

	static {
		// Laden der Schachfiguren-Bilder
		pieceImages.put('P', loadImage("images/white_pawn.png"));
		pieceImages.put('R', loadImage("images/white_rook.png"));
		pieceImages.put('N', loadImage("images/white_knight.png"));
		pieceImages.put('B', loadImage("images/white_bishop.png"));
		pieceImages.put('Q', loadImage("images/white_queen.png"));
		pieceImages.put('K', loadImage("images/white_king.png"));

		pieceImages.put('p', loadImage("images/black_pawn.png"));
		pieceImages.put('r', loadImage("images/black_rook.png"));
		pieceImages.put('n', loadImage("images/black_knight.png"));
		pieceImages.put('b', loadImage("images/black_bishop.png"));
		pieceImages.put('q', loadImage("images/black_queen.png"));
		pieceImages.put('k', loadImage("images/black_king.png"));
	}

	/**
	 * Lädt ein Bild von einem angegebenen Pfad und gibt es als {@link Image}
	 * zurück.
	 *
	 * @param path
	 *            Der Pfad zur Bilddatei im Ressourcen-Verzeichnis.
	 * @return Das geladene {@link Image}, oder {@code null} im Falle eines Fehlers
	 *         beim Laden des Bildes.
	 */
	private static Image loadImage(String path) {
		try {
			return new Image(Objects.requireNonNull(PieceImageLoader.class.getClassLoader().getResourceAsStream(path)));
		} catch (Exception e) {
			System.err.println("Failed to load image: " + path);
			throw new RuntimeException(e); // Wir brechen hier einfach ab, Bilder sollten immer geladen werden können
		}
	}

	/**
	 * Gibt die geladenen Schachfiguren-Bilder als Map zurück.
	 *
	 * @return Map mit den geladenen Schachfiguren-Bildern
	 */
	public static Image get(char piece) {
		return pieceImages.get(piece);
	}

}
