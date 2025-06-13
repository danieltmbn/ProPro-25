/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.view.input;

import java.util.function.Consumer;
import tuda.ai1.propro25.model.Coordinate;
import tuda.ai1.propro25.view.renderer.BoardRenderer;

/**
 * Diese Klasse verwaltet die Eingaben des Benutzers auf dem Schachbrett,
 * insbesondere Maus-Klicks auf die Felder des Schachbretts. Sie übersetzt die
 * Klicks auf das Schachbrett in Koordinaten und ermöglicht es, dass eine
 * externe Methode aufgerufen wird, wenn ein Feld angeklickt wird.
 */
public class BoardInputHandler {

	private final BoardRenderer boardRenderer;
	private Consumer<Coordinate> onTileClicked;

	public BoardInputHandler(BoardRenderer boardRenderer) {
		this.boardRenderer = boardRenderer;
		setupMouseHandler();
	}

	/**
	 * Setzt den Handler für Klicks auf die Felder des Schachbretts.
	 *
	 * @param handler
	 *            Der Handler, der aufgerufen wird, wenn ein Feld geklickt wird.
	 */
	public void setOnTileClicked(Consumer<Coordinate> handler) {
		this.onTileClicked = handler;
	}

	/**
	 * Initialisiert den Maus-Handler und verknüpft ihn mit dem Canvas des
	 * BoardRenderers. Der Handler übersetzt die Klickposition in Board-Koordinaten
	 * und ruft den entsprechenden Handler auf.
	 */
	public void setupMouseHandler() {
		boardRenderer.getCanvas().setOnMouseClicked(event -> {
			int[] c = boardRenderer.translateClickToCoordinate(event.getX(), event.getY());
			int file = c[0];
			int rank = c[1];
			Coordinate clicked = new Coordinate(file, rank);
			if (onTileClicked != null) {
				onTileClicked.accept(clicked);
			}
		});
	}
}