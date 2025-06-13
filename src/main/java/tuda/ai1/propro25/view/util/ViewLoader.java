/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.view.util;

import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class ViewLoader {

	/**
	 * Lädt ein Parent-Node aus dem angegebenen FXML-Pfad mit dem übergebenen
	 * Controller. Der übergebene Controller muss denselben Typen haben wie im
	 * FXML-Dokument spezifiziert.
	 *
	 * @param fxmlPath
	 *            der Pfad zum FXML-Dokument in den Ressourcen
	 * @param controller
	 *            das Controller-Object
	 * @return die durch FXML spezifizierte Parent-Node
	 * @throws IllegalArgumentException
	 *             falls die Ressource nicht existiert oder der Controller den
	 *             falschen Typen hat
	 */
	public static Parent loadView(String fxmlPath, Object controller) {
		URL url = ViewLoader.class.getResource(fxmlPath);
		if (url == null) {
			throw new IllegalArgumentException("Die Ressource: " + fxmlPath + " existiert nicht");
		}

		FXMLLoader loader = new FXMLLoader(url);
		loader.setControllerFactory(expectedControllerType -> {
			if (controller.getClass() != expectedControllerType) {
				throw new IllegalArgumentException("Für " + fxmlPath + " wurde ein Controller des Typs "
						+ expectedControllerType + " wurde erwartet");
			}

			return controller;
		});

		try {
			return loader.load();
		} catch (IOException e) {
			throw new RuntimeException("Laden von " + fxmlPath + " fehlgeschlagen", e);
		}
	}

}
