/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.view;

import java.util.Objects;
import java.util.function.Consumer;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import tuda.ai1.propro25.model.Color;
import tuda.ai1.propro25.model.Coordinate;
import tuda.ai1.propro25.model.piece.Piece;
import tuda.ai1.propro25.view.renderer.BoardRenderer;
import tuda.ai1.propro25.view.renderer.ClockRenderer;

/**
 * Diese Klasse ist für das Bauen des Spiels zuständig
 */
public class GameSceneBuilder extends StackPane {
	private final BoardRenderer boardRenderer;
	private final ClockRenderer clockRenderer;
	private StackPane centerPane;

	public GameSceneBuilder(BoardRenderer boardRenderer, ClockRenderer clockRenderer) {
		this.boardRenderer = boardRenderer;
		this.clockRenderer = clockRenderer;
	}

	/**
	 *
	 * @return die Spielszene
	 */
	public Scene buildScene() {
		BorderPane borderPane = new BorderPane();

		Image image = new Image(
				Objects.requireNonNull(getClass().getResource("/backdrop/backdrop_wide_blur.png")).toExternalForm());

		BackgroundSize backgroundSize = new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true,
				true);
		BackgroundImage backgroundImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT,
				BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize);

		setBackground(new Background(backgroundImage));
		centerPane = new StackPane(boardRenderer.getCanvas());
		bindCanvasSize(borderPane);
		centerPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

		// Bind canvas size to StackPane size
		bindCanvasSize(centerPane);

		// Create side-by-side layout for board + clock
		HBox centerLayout = new HBox();
		centerLayout.setSpacing(20);
		centerLayout.setAlignment(Pos.CENTER_LEFT);
		centerLayout.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

		// Clock panel setup
		VBox clockPanel = createClockPanel();
		clockPanel.prefHeightProperty().bind(centerPane.heightProperty()); // Match board height

		borderPane.setCenter(centerPane);
		borderPane.setRight(clockPanel);
		centerLayout.getChildren().addAll(centerPane, clockPanel);
		HBox.setHgrow(centerPane, Priority.ALWAYS);

		borderPane.setCenter(centerLayout);

		BorderPane.setAlignment(clockRenderer.getView(), Pos.CENTER);
		BorderPane.setAlignment(boardRenderer.getCanvas(), Pos.CENTER);

		getChildren().add(borderPane);
		Scene scene = new Scene(this);
		scene.getStylesheets()
				.add(Objects.requireNonNull(getClass().getResource("/styles/style.css")).toExternalForm());
		boardRenderer.getCanvas().requestFocus();

		return scene;
	}

	/**
	 * Erstellt ClockPanel
	 *
	 * @return ClockPanel
	 */
	private VBox createClockPanel() {
		VBox clockPanel = clockRenderer.getView();
		clockPanel.setMinWidth(160);
		return clockPanel;
	}

	/**
	 * Setzt canvas größe auf 80 % des parent layouts
	 *
	 * @param root
	 *            parent layout
	 */
	private void bindCanvasSize(Pane root) {
		DoubleBinding roundedWidth = Bindings.createDoubleBinding(() -> Math.floor(root.getWidth() * 0.8),
				root.widthProperty());

		DoubleBinding roundedHeight = Bindings.createDoubleBinding(() -> Math.floor(root.getHeight() * 0.8),
				root.heightProperty());

		boardRenderer.getCanvas().widthProperty().bind(roundedWidth);
		boardRenderer.getCanvas().heightProperty().bind(roundedHeight);
	}

	/**
	 * schließt PromotionDialogue
	 */
	protected void closeDialogue() {
		boolean removed = centerPane.getChildren().removeIf(node -> node instanceof PromotionDialogPane);
		if (removed) { // only enable undoButton if dialog was actually shown
			disableUndoButton(false);
		}
		boardRenderer.getCanvas().requestFocus();
	}

	/**
	 * Zeigt promotion Dialogue
	 */
	protected void showPromotionDialogAsync(Coordinate position, Color color, Consumer<Piece> onSelected) {
		double[] px = transformCanvasPxToPanePx(boardRenderer.toPixels(position.getFile(), position.getRank()));
		PromotionDialogPane dialog = new PromotionDialogPane(color, boardRenderer.getSquareSize(), px,
				() -> onSelected.accept(null), onSelected);
		centerPane.getChildren().add(dialog);
		disableUndoButton(true);
	}

	/**
	 * Deaktiviert den undoButton
	 */
	protected void disableUndoButton(boolean disable) {
		clockRenderer.getMoveHistory().getUndoButton().setDisable(disable);
	}

	/**
	 * @param canvasPx
	 *            die Pixel koordinaten auf den Canvas
	 * @return pixel Koordinten auf dem parent layout
	 */
	private double[] transformCanvasPxToPanePx(double[] canvasPx) {
		Bounds canvasBounds = boardRenderer.getCanvas().localToScene(boardRenderer.getCanvas().getBoundsInLocal());
		Bounds centerBounds = centerPane.localToScene(centerPane.getBoundsInLocal());

		double offsetX = canvasBounds.getMinX() - centerBounds.getMinX();
		double offsetY = canvasBounds.getMinY() - centerBounds.getMinY();

		return new double[]{offsetX + canvasPx[0], offsetY + canvasPx[1]};
	}
}
