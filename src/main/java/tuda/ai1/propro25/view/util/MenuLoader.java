/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.view.util;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tuda.ai1.propro25.view.GUIManager;
import tuda.ai1.propro25.view.controller.*;

public class MenuLoader {
	public static Parent loadStartMenu(GUIManager guiManager, Stage stage) {
		FXMLLoader loader = new FXMLLoader(MenuLoader.class.getResource("/fxml/StartScreen.fxml"));
		Parent root;
		try {
			root = loader.load();
		} catch (IOException e) {
			throw new RuntimeException(e); // Das können wir nicht retten
		}
		StartMenuController controller = loader.getController();
		controller.init(guiManager, stage);
		return root;
	}

	public static Parent loadCreateGameMenu(GUIManager guiManager, Stage stage) {
		FXMLLoader loader = new FXMLLoader(MenuLoader.class.getResource("/fxml/CreateGameMenu.fxml"));
		StackPane root;
		try {
			root = loader.load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		CreateGameMenuController controller = loader.getController();
		controller.setContext(guiManager, stage);
		return root;
	}

	public static FxmlWithController<SettingsMenuController> loadSettingsMenu(GUIManager guiManager, Stage stage,
			SettingsMenuController.SettingsSource source) throws IOException {

		FXMLLoader loader = new FXMLLoader(MenuLoader.class.getResource("/fxml/SettingsMenu.fxml"));
		SettingsMenuController controller = SettingsMenuController.getInstance();
		loader.setController(controller);
		StackPane root = loader.load();
		controller.setContext(guiManager, stage, source);
		controller.setCachedColoros();
		return new FxmlWithController<>(root, controller);
	}

	public static Parent loadCreditsMenu(GUIManager guiManager, Stage stage) throws IOException {
		FXMLLoader loader = new FXMLLoader(MenuLoader.class.getResource("/fxml/CreditsMenu.fxml"));
		StackPane root = loader.load();
		CreditsMenuController controller = loader.getController();
		controller.setContext(guiManager, stage);
		return root;
	}

	public static FxmlWithController<EndScreenController> loadEndScreen() {
		FXMLLoader loader = new FXMLLoader(MenuLoader.class.getResource("/fxml/EndScreen.fxml"));
		StackPane root;
		try {
			root = loader.load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		EndScreenController controller = loader.getController();
		return new FxmlWithController<>(root, controller);
	}
}
