/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import tuda.ai1.propro25.model.Color;
import tuda.ai1.propro25.model.piece.*;
import tuda.ai1.propro25.view.util.PieceImageLoader;

/**
 * Eine benutzerdefinierte JavaFX-Pane, die ein Auswahlfenster zur
 * Bauernumwandlung im Schachspiel darstellt.
 * <p>
 * Der Dialog zeigt vier Buttons mit den möglichen Umwandlungsfiguren (Dame,
 * Turm, Läufer, Springer) für die angegebene Spielerfarbe. Zusätzlich gibt es
 * einen Button zum Abbrechen der Auswahl.
 * </p>
 * <p>
 * Die Reihenfolge der Figuren hängt von der Spielerfarbe ab: Für Weiß werden
 * die stärksten Figuren zuerst angezeigt, für Schwarz in umgekehrter
 * Reihenfolge.
 * </p>
 */
public class PromotionDialogPane extends Pane {

	public PromotionDialogPane(Color color, double squareSize, double[] px, Runnable onCancel,
			java.util.function.Consumer<Piece> onPieceSelected) {
		double xOffset = px[0];
		double yOffset = px[1];

		VBox vbox = new VBox();
		vbox.setMaxWidth(squareSize);

		vbox.setLayoutX(xOffset);
		vbox.setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.LIGHTGRAY, // Background color
				CornerRadii.EMPTY, // No rounded corners
				Insets.EMPTY // No insets/padding
		)));

		Piece[] pieces = (color == Color.WHITE)
				? new Piece[]{new Queen(color), new Rook(color), new Bishop(color), new Knight(color)}
				: new Piece[]{new Knight(color), new Bishop(color), new Rook(color), new Queen(color)};

		for (Piece piece : pieces) {
			Image img = PieceImageLoader.get(piece.getFenSymbol());
			if (img != null) {
				ImageView view = new ImageView(img);
				view.setFitWidth(squareSize);
				view.setFitHeight(squareSize);

				Button btn = new Button();
				btn.setPrefSize(squareSize, squareSize);
				btn.setMaxSize(squareSize, squareSize);

				btn.setGraphic(view);
				btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				btn.getStyleClass().add("flat-piece-button");

				btn.setOnAction(e -> onPieceSelected.accept(piece));

				vbox.getChildren().add(btn);
			}
		}

		Button cancelButton = new Button("X");
		cancelButton.setPrefWidth(squareSize);
		cancelButton.setPrefHeight(squareSize / 2.0);
		cancelButton.setMaxHeight(squareSize / 2);
		cancelButton.getStyleClass().add("flat-button");
		cancelButton.setOnAction(e -> onCancel.run());

		Platform.runLater(() -> {
			double y;
			double dialogHeight = 3 * squareSize + squareSize / 2;
			if (color == Color.BLACK) {
				vbox.getChildren().add(0, cancelButton);
				y = yOffset - dialogHeight;
			} else {
				vbox.getChildren().add(cancelButton);
				y = yOffset;
			}
			vbox.setLayoutY(y);
		});

		getChildren().add(vbox);

	}
}
