/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.view.renderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import tuda.ai1.propro25.view.GUIManager;
import tuda.ai1.propro25.view.controller.SettingsMenuController;
import tuda.ai1.propro25.view.util.MenuLoader;

/**
 * Diese Klasse rendert ein pausierbares Menü als Pop-up-Overlay im Schachspiel.
 *
 * Es unterstützt verschiedene Zustände, z. B. das Hauptmenü und ein
 * Bestätigungsmenü für das Beenden des Spiels. Buttons wie "Resume", "Main
 * Menu", "Settings" und "Quit" werden angezeigt und verwaltet.
 *
 * Das Menü kann programmatisch ein- und ausgeblendet werden.
 */
public class PauseMenuRenderer extends StackPane {
	private final VBox layout;
	private final Label titleLabel;
	private final GUIManager guiManager;
	private final StackPane parent;
	private final Stage stage;
	boolean shown = false;
	private Parent settingsMenu;

	private final List<Button> mainMenuButtons = new ArrayList<>();
	private final List<Button> confirmButtons = new ArrayList<>();

	/**
	 * Konstruktor: Erzeugt ein neues pausierbares Menü.
	 *
	 * @param owner
	 *            Das zugehörige JavaFX-Stage-Fenster.
	 * @param parent
	 *            Der übergeordnete StackPane, in den das Menü eingeblendet wird.
	 * @param guiManager
	 *            Der GUIManager zur Verwaltung von Menüübergängen.
	 */
	public PauseMenuRenderer(Stage owner, StackPane parent, GUIManager guiManager) {
		this.guiManager = guiManager;
		stage = owner;

		this.parent = parent;

		// Layout and Buttons
		layout = new VBox(0);
		buttonLayoutProperties(layout);

		// shown text on the menu
		titleLabel = new Label();
		labelProperties();

		getStyleClass().add("popup-box");
		// add JavaFX-Buttons (with desired properties)
		Button resumeButton = resumeButtonProperties();
		Button quitButton = quitButtonProperties();
		Button mainMenuButton = mainMenuButtonProperties();
		Button settingsButton = settingsButtonProperties();

		Button backButton = backButtonProperties();
		Button exitButton = exitButtonProperties();

		// add all buttons to the menus
		mainMenuButtons.addAll(List.of(resumeButton, settingsButton, mainMenuButton, quitButton));
		confirmButtons.addAll(List.of(backButton, exitButton));

		layout.getChildren().addAll(titleLabel, resumeButton, settingsButton, mainMenuButton, quitButton);
		getChildren().add(layout);

		Platform.runLater(() -> {
			double stageHeight = stage.getHeight() > 0 ? stage.getHeight() : 720;
			double width = stageHeight / 2;
			double height = stageHeight / 2;

			setMaxSize(width, height);
		});

		parent.setOnMouseClicked(event -> {
			// Only go back if click is outside the menu
			if (!this.localToScene(this.getBoundsInLocal()).contains(event.getSceneX(), event.getSceneY())) {
				this.close(); // or whatever method loads your start screen
			}
		});
	}

	/**
	 * Konfiguriert den Titel-Text ("Menu", "Quit Game?", etc.) des Pop-ups.
	 */
	public void labelProperties() {
		titleLabel.setText("Menü"); // Default
		titleLabel.setStyle("-fx-font-size: 36px;" + "-fx-font-weight: bold;" + "-fx-text-fill: #d6c488;");
		titleLabel.setAlignment(Pos.CENTER);
		titleLabel.setTextAlignment(TextAlignment.CENTER);
		titleLabel.setWrapText(true);

	}

	/**
	 * Wendet generelle visuelle Eigenschaften auf einen Menübutton an, z.B.
	 * Styleklasse und Fokusverhalten.
	 *
	 * @param button
	 *            Der zu formatierende Button.
	 */
	public void menuButtonProperties(Button button) {
		button.setFocusTraversable(false);
		button.getStyleClass().add("main-button");
	}

	/**
	 * Konfiguriert die Ausrichtung und den Abstand der enthaltenen Buttons
	 * innerhalb des Layout-VBox-Containers.
	 *
	 * @param layout
	 *            Die VBox, die die Buttons enthält.
	 */
	public void buttonLayoutProperties(VBox layout) {
		layout.setAlignment(Pos.CENTER);
		layout.setPadding(new Insets(20));
		layout.spacingProperty().bind(layout.heightProperty().multiply(0.05));
	}

	/**
	 * Erstellt den "Resume"-Button. Blendet das Menü aus, wenn er angeklickt wird.
	 *
	 * @return Ein formatierter Resume-Button.
	 */
	public Button resumeButtonProperties() {
		Button resumeButton = new Button("Weiter");
		resumeButton.setOnAction(e -> close());
		menuButtonProperties(resumeButton);
		return resumeButton;
	}

	/**
	 * Erstellt den "Quit"-Button. Öffnet ein Bestätigungsmenü zum Beenden des
	 * Spiels.
	 *
	 * @return Ein formatierter Quit-Button.
	 */
	public Button quitButtonProperties() {
		Button quitButton = new Button("Verlassen");
		quitButton.setOnAction(e -> {
			showQuitMenu(confirmed -> {
				if (confirmed) {
					guiManager.clear();
					stage.close();// Only go to main menu if confirmed
					System.exit(0);
				} else {
					titleLabel.setText("Hauptmenü");
					layout.getChildren().clear();
					layout.getChildren().add(titleLabel);
					layout.getChildren().addAll(mainMenuButtons);

				}

			});
		});
		menuButtonProperties(quitButton);
		return quitButton;
	}

	/**
	 * Erstellt den "Main Menu"-Button. Öffnet ein Bestätigungsmenü zur Rückkehr zum
	 * Hauptmenü.
	 *
	 * @return Ein formatierter Main Menu-Button.
	 */
	public Button mainMenuButtonProperties() {
		Button mainMenuButton = new Button("Hauptmenü");
		mainMenuButton.setOnAction(e -> {
			showConfirmationMenu(confirmed -> {
				if (confirmed) {
					guiManager.clear();
					guiManager.showStartMenu(stage); // Only go to main menu if confirmed
				} else {
					titleLabel.setText("Hauptmenü");
					layout.getChildren().clear();
					layout.getChildren().add(titleLabel);
					layout.getChildren().addAll(mainMenuButtons);
				}

			});
		});
		menuButtonProperties(mainMenuButton);
		return mainMenuButton;
	}

	/**
	 * Erstellt den "Settings"-Button. Lädt das Einstellungsmenü (im Spielmodus).
	 *
	 * @return Ein formatierter Settings-Button.
	 */
	public Button settingsButtonProperties() {
		Button settingsButton = new Button("Einstellungen");
		settingsButton.setOnAction(e -> {
			try {
				var settingsMenu1 = MenuLoader.loadSettingsMenu(guiManager, stage,
						SettingsMenuController.SettingsSource.IN_GAME);
				settingsMenu = settingsMenu1.root();
				parent.getChildren().add(settingsMenu);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		});
		menuButtonProperties(settingsButton);
		return settingsButton;
	}

	/**
	 * Erstellt den "Back"-Button im Bestätigungsmenü. Wechselt zurück zum
	 * Hauptmenü-Zustand.
	 *
	 * @return Ein formatierter Back-Button.
	 */
	public Button backButtonProperties() {
		Button backButton = new Button("Zurück");
		backButton.setOnAction(e -> layout.getChildren().addAll(mainMenuButtons));
		menuButtonProperties(backButton);
		return backButton;
	}

	/**
	 * Erstellt den "Exit"-Button im Bestätigungsmenü. Beendet die Anwendung.
	 *
	 * @return Ein formatierter Exit-Button.
	 */
	public Button exitButtonProperties() {
		Button exitButton = new Button("Beenden");
		exitButton.setOnAction(e -> {
			Platform.exit();
			System.exit(0); // ensures JVM exits even with non-daemon threads;
		});
		menuButtonProperties(exitButton);
		return exitButton;
	}

	/**
	 * Zeigt den endscreen
	 * 
	 * @param onClose
	 *            Aktion die bie close ausgeführt werden soll
	 */
	public void showQuitMenu(Consumer<Boolean> onClose) {

		titleLabel.setText("Spiel beenden?");

		// Remove old buttons if necessary
		confirmButtons.clear();

		Button backButton = new Button("Weiterspielen");
		backButton.setOnAction(e -> {
			onClose.accept(false); // User canceled
		});

		Button exitButton = new Button("Beenden");
		exitButton.setOnAction(e -> {
			onClose.accept(true); // User confirmed
		});

		menuButtonProperties(backButton);
		menuButtonProperties(exitButton);
		confirmButtons.addAll(List.of(backButton, exitButton));

		layout.getChildren().setAll(titleLabel);
		layout.getChildren().addAll(confirmButtons);
	}

	/**
	 * Blendet das Menü ein.
	 */
	public void show() {
		shown = true;
		if (!parent.getChildren().contains(this)) {
			parent.getChildren().add(this);
		}
	}

	/**
	 * Blendet das Menü aus.
	 */
	public void close() {
		shown = false;
		closeSettings();
		parent.getChildren().remove(this);
	}

	/**
	 * Gibt zurück, ob das Menü aktuell angezeigt wird.
	 *
	 * @return true, wenn sichtbar.
	 */
	public boolean isShown() {
		return shown;
	}

	/**
	 * Entfernt das Einstellungsmenü, falls es angezeigt wird.
	 */
	public void closeSettings() {
		parent.getChildren().remove(settingsMenu);
	}

	/**
	 * Zeigt ein Bestätigungsmenü mit Rückfrage an, ob zum Hauptmenü gewechselt
	 * werden soll.
	 *
	 * @param onClose
	 *            Callback, das true liefert, wenn der Benutzer bestätigt, und false
	 *            bei Abbruch.
	 */
	public void showConfirmationMenu(Consumer<Boolean> onClose) {

		titleLabel.setText("Zurück zum Hauptmenü?");

		// Remove old buttons if necessary
		confirmButtons.clear();

		Button backButton = new Button("Weiterspielen");
		backButton.setOnAction(e -> {
			onClose.accept(false); // User canceled
		});

		Button exitButton = new Button("Hauptmenü");
		exitButton.setOnAction(e -> {
			onClose.accept(true); // User confirmed
		});

		menuButtonProperties(backButton);
		menuButtonProperties(exitButton);
		confirmButtons.addAll(List.of(backButton, exitButton));

		layout.getChildren().setAll(titleLabel);
		layout.getChildren().addAll(confirmButtons);
	}

}
