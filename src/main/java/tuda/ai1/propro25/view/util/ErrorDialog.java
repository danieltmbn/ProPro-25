/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.view.util;

import javafx.scene.control.Alert;

public class ErrorDialog {

	/**
	 * Zeigt einen Error-Dialog mit der angegebenen Nachricht.
	 * 
	 * @param message
	 *            die Nachricht
	 */
	public static void show(String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR, message);
		alert.showAndWait();
	}

}
