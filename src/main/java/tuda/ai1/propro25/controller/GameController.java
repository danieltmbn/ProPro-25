/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.scene.Parent;
import tuda.ai1.propro25.ai.AIOpponent;
import tuda.ai1.propro25.model.*;
import tuda.ai1.propro25.model.history.HistoricalGame;
import tuda.ai1.propro25.model.piece.Piece;
import tuda.ai1.propro25.view.GUIManager;
import tuda.ai1.propro25.view.controller.ExportViewController;
import tuda.ai1.propro25.view.util.Sound;
import tuda.ai1.propro25.view.util.ViewLoader;

public class GameController {
	private final Board board;
	private final GUIManager view;
	/** Coordinate of currently selected move */
	private Coordinate selectedFrom = null;
	Color humanColor = null;
	private List<Move> allowedMoves;
	private final ExecutorService aiExecutor = Executors.newSingleThreadExecutor();
	private final int increment;
	private boolean timerStarted = false;
	Random random = new Random();

	/**
	 * Future für den nächsten Zug der AI. Objektvariable, um die Berechnung global
	 * abbrechen zu können
	 */
	Future<Move> aiMoveFuture;

	/**
	 * Erstellt einen neuen GameController mit bereits erstelltem Board
	 *
	 * @param board
	 *            das Board, auf dem gespielt wird
	 * @param view
	 *            der {@link GUIManager}
	 * @param increment
	 *            Inkrement
	 */
	public GameController(Board board, GUIManager view, int increment) {
		this.board = board;
		this.view = view;
		this.increment = increment;

		Player[] players = board.getPlayers();

		if (players[0].getClass() == Player.class && players[1].getClass() != Player.class) {
			humanColor = players[0].getColor();
		} else if (players[1].getClass() == Player.class && players[0].getClass() != Player.class) {
			humanColor = players[1].getColor();
		}

		if (board.getCurrentPlayer() instanceof AIOpponent) {
			// AI muss ersten Zug machen
			askAIForMove((AIOpponent) board.getCurrentPlayer());
		}
	}

	public void onGameStop() {
		aiExecutor.shutdownNow();
	}

	/**
	 * Gibt die Figur an der angegebenen Koordinate zurück.
	 * 
	 * @param coordinate
	 *            Die Koordinate, an der die Figur gesucht wird
	 * @return die Figur an der angegebenen Koordinate oder {@code null}, wenn keine
	 *         Figur an dieser Stelle vorhanden ist
	 */
	public Piece getPieceAt(Coordinate coordinate) {
		return board.getPiece(coordinate.getFile(), coordinate.getRank());
	}

	/**
	 * Gibt das aktuelle Schachbrett als 2D-Array von Figuren zurück.
	 *
	 * @return Ein 2D-Array von {@link Piece}-Objekten, das das Schachbrett
	 *         darstellt
	 **/
	public Piece[][] getBoard() {
		return board.getUnmodifiablePieceGrid();
	}

	/**
	 * Behandelt das Ereignis, wenn ein Feld auf dem Schachbrett angeklickt wird.
	 * <p>
	 * Wenn noch keine Figur ausgewählt ist und das angeklickte Feld eine Figur des
	 * aktuellen Spielers enthält, wird diese Figur ausgewählt und alle legalen
	 * Zielpositionen werden hervorgehoben.
	 * <p>
	 * Wenn bereits eine Figur ausgewählt ist und das angeklickte Feld ein gültiges
	 * Ziel ist, wird versucht, den Zug auszuführen. Führt der Zug zu einem
	 * Spielende (z.B. Schachmatt), wird das Spiel beendet.
	 * <p>
	 * Führt der Klick zu keiner Aktion, wird die Auswahl zurückgesetzt.
	 *
	 * @param coordinate
	 *            Die angeklickte Koordinate auf dem Spielbrett
	 */
	public void onTileClicked(Coordinate coordinate) {
		if (!coordinate.isOnBoard())
			return;

		if (humanColor != null && board.getCurrentPlayer().getColor() != humanColor) {
			return;
		}
		Piece clickedPiece = getPieceAt(coordinate);
		Color currentPlayer = board.getCurrentPlayer().getColor();
		boolean isOwnPiece = clickedPiece != null && clickedPiece.getColor() == currentPlayer;

		// If nothing selected yet
		if (selectedFrom == null) {
			if (isOwnPiece) {
				selectPiece(coordinate);
			}
			return;
		}

		// Clicked the same piece again → deselect
		if (coordinate.equals(selectedFrom)) {
			resetSelection();
			view.clearHighlights();
			view.highlightMove(board.getLastMove());
			selectedFrom = null;
			return;
		}

		// Clicked another own piece → reselect
		if (isOwnPiece) {
			selectPiece(coordinate);
			return;
		}

		// Try move
		List<Move> possibleMoves = allowedMoves.stream().filter(m -> m.getTo().equals(coordinate)).toList();

		if (!possibleMoves.isEmpty()) {
			selectMoveFromOptions(possibleMoves, move -> {
				view.closeDialogue();
				if (move != null) {
					handleMove(move);
					resetSelection();
				} else {
					view.clearHighlights();
					view.highlightMove(board.getLastMove());
					resetSelection();
				}
			});
		} else {
			view.clearHighlights();
			view.highlightMove(board.getLastMove());
			resetSelection();
		}
	}

	private void selectPiece(Coordinate coordinate) {
		selectedFrom = coordinate;
		allowedMoves = new ArrayList<>(board.getAllAllowedMovesForPieceOnSquare(selectedFrom));
		view.clearHighlights();
		view.highlightMove(board.getLastMove());
		view.highlightTiles(selectedFrom, allowedMoves);
	}
	/**
	 * Wählt einen Zug aus einer Liste von möglichen Zügen aus, die auf dasselbe
	 * Ziel-Feld abzielen.
	 * <p>
	 * Wenn nur ein möglicher Zug vorhanden ist, wird dieser automatisch ausgewählt.
	 * Wenn mehrere Züge möglich sind (z. B. bei einer Bauernumwandlung), wird der
	 * Benutzer aufgefordert, eine Schachfigur auszuwählen, in die der Bauer
	 * umgewandelt werden kann.
	 *
	 * @param possibleMovesToCoordinate
	 *            Eine Liste von möglichen {@link Move}-Objekten, die zulässige Züge
	 *            auf das gleiche Ziel-Feld darstellen.
	 */
	private void selectMoveFromOptions(List<Move> possibleMovesToCoordinate, Consumer<Move> callback) {
		if (possibleMovesToCoordinate.size() == 1) {
			callback.accept(possibleMovesToCoordinate.get(0));
			return;
		}

		// Öffne Promotion-Dialog (asynchron)
		view.showPromotionDialogAsync(possibleMovesToCoordinate.get(0).getTo(), getPieceAt(selectedFrom).getColor(),
				promotionPiece -> {
					if (promotionPiece == null) {
						callback.accept(null);
						return;
					}

					Move selected = possibleMovesToCoordinate.stream()
							.filter(m -> promotionPiece.equals(m.getPromotionPiece())).findFirst().orElse(null);
					callback.accept(selected);
				});
	}

	/**
	 * Führt den angegebenen Zug aus und aktualisiert die Benutzeroberfläche.
	 * <p>
	 * Diese Methode spielt den Sound für einen Zug, führt den Zug auf dem
	 * Schachbrett aus, und entfernt etwaige Hervorhebungen auf dem Spielfeld. Falls
	 * ein KI-Gegner vorhanden ist, wird ein Move des KI-Gegners im Anschluss
	 * ausgeführt
	 *
	 * @param move
	 *            Der {@link Move}, der ausgeführt werden soll.
	 */
	public void handleMove(Move move) {
		if (board.getGameState() != GameState.RUNNING && board.getGameState() != GameState.PAUSED) {
			// Kann passieren durch race conditions mit Zeit, hier abfangen via check
			return;
		}
		if (board.getPiece(move.getFrom()).getColor() != board.getCurrentPlayer().getColor()) {
			// Verhindert in den edge cases durch verzögerte AI moves mit den falschen
			// Figuren zu ziehen. Die UI sollte das eigentlich gar nicht erst zulassen
			return;
		}
		view.disableUndoButton(false);
		try {
			view.playSound(move.getType());
			board.makeMove(move);
			view.highlightActivePlayer(getCurrentPlayer().getColor());
			if (board.getColorInCheck() != null) {
				view.playSound(Sound.CHECK);
			}

			Platform.runLater(() -> {
				String moveStr = board.getLastMoveAlgebraicNotation();
				view.updateMoveHistoryUI(moveStr);
			});

		} catch (Exception e) {
			e.printStackTrace();
			view.showErrorScreen();
		}
		view.clearHighlights();
		view.highlightMove(move);

		if (timerStarted) {
			incrementTime();

		}
		if (!timerStarted && board.getHistory().size() >= 2) {
			view.startTimer();
			timerStarted = true;
		}
		if (board.getGameState() != GameState.RUNNING && board.getGameState() != GameState.PAUSED
				&& board.getGameState() != null) {
			endGame(board.getGameState());
			return;
		}
		if (board.getCurrentPlayer() instanceof AIOpponent) {
			askAIForMove((AIOpponent) board.getCurrentPlayer());
		}
	}

	/**
	 * Setzt die Auswahl zurück, indem das "ausgewählte Feld" und die Liste der
	 * erlaubten Züge gelöscht werden.
	 * <p>
	 * Diese Methode wird aufgerufen, um die Auswahl zu entfernen, wenn ein Zug
	 * abgeschlossen ist oder wenn der Benutzer eine neue Auswahl treffen möchte.
	 */
	private void resetSelection() {
		selectedFrom = null;
		allowedMoves.clear();
	}

	/**
	 * Wird aufgerufen, wenn der Benutzer den Undo-Button betätigt.
	 * <p>
	 * Diese Methode macht den letzten Zug rückgängig, aktualisiert das Schachbrett
	 * und zeigt den aktuellen Zustand des Schachbretts an.
	 */
	public void onUndoClicked() {
		if (humanColor != null && aiMoveFuture != null) {
			// Wenn die AI gerade noch am Rechnen ist, schnell abbrechen.
			aiMoveFuture.cancel(true);
		}
		view.playSound(Sound.UNDO);
		board.restoreTimeForUndo();
		board.undoLastMove();
		view.clearHighlights();
		view.getMoveHistoryRenderer().removeMove(getCurrentColor());
		Move lastMove = board.getLastMove();
		if (lastMove == null) {
			view.disableUndoButton(true);
		}

		view.highlightMove(lastMove);
		view.highlightActivePlayer(getCurrentPlayer().getColor());
		if (board.getCurrentPlayer() instanceof AIOpponent) {
			// AI berechnet Zug direkt neu, als Spieler muss man notfalls sehr schnell
			// nochmal klicken
			askAIForMove((AIOpponent) board.getCurrentPlayer());
		}
	}

	public boolean canUndo() {
		return board.getLastMove() != null;
	}

	public Player getCurrentPlayer() {
		return board.getCurrentPlayer();
	}

	public Color getCurrentColor() {
		return getCurrentPlayer().getColor();
	}

	public Player getNextPlayer() {
		return board.getNextPlayer();
	}

	public void onExportClicked() {
		if (board == null) {
			// darf nur geklickt werden, wenn gerade ein Spiel zu sehen ist
			return;
		}

		HistoricalGame game = board.exportGame();
		ExportViewController exportController = new ExportViewController(game);
		Parent dialogRoot = ViewLoader.loadView("/fxml/ExportView.fxml", exportController);
		view.openDialogWindow(dialogRoot, "Exportieren");
	}

	/**
	 * Beendet das Spiel und zeigt den entsprechenden Endzustand an.
	 * <p>
	 * Diese Methode wird aufgerufen, wenn das Spiel zu einem Ende kommt, z. B.
	 * durch Schachmatt, Patt oder anderes Endkriterium. Sie könnte in Zukunft um
	 * Logik erweitert werden, um z. B. den Gewinner anzuzeigen oder den
	 * Spielverlauf zu speichern.
	 *
	 * @param gameState
	 *            Der aktuelle Zustand des Spiels
	 */
	private void endGame(GameState gameState) {
		// handle end of game
		view.deleteTimer();
		String message;
		String winner;
		message = switch (gameState) {
			case END_CHECKMATE -> {
				winner = board.getWinner().getName();
				yield "Schachmatt! " + winner + " hat gewonnen!";
			}
			case END_TIMEOUT -> {
				winner = board.getWinner().getName();
				yield "Die Zeit ist ausgelaufen! " + winner + " hat gewonnen";
			}
			case END_STALEMATE -> "Remis, da ein König sich nicht mehr bewegen kann!";
			case END_MATERIAL -> "Remis, da kein Schachmatt erreicht werden kann!";
			case END_REPETITION -> "Remis, es wurde 3 mal gleich gezogen!";
			case END_50MOVE -> "Remis, da 50 Züge gemacht wurden!";
			case END_RESIGN -> {
				winner = board.getWinner().getName();
				yield winner + " hat durch Aufgabe des Gegners gewonnen!";
			}
			case END_AGREEMENT -> "Remis, unentschieden durch Einigung";
			default -> "Game ended.";
		};

		view.showEndScreen(message);
	}

	/**
	 * Ermittelt den nächsten Spielzug des KI-Gegners asynchron.
	 * <p>
	 * Die Methode übergibt die Spielfeld-Instanz an die KI-Logik und der Executor
	 * wartet im Hintergrund auf das Ergebnis. Die maximale Wartezeit entspricht der
	 * verbleibenden Zeit des KI-Spielers. Sollte innerhalb dieser Zeit kein Zug
	 * geliefert werden, gilt dies als Timeout, und der KI-Spieler gibt auf. Wenn
	 * die KI einen Zug berechnet hat, wird über den Executor die handleMove methode
	 * aufgerufen.
	 */
	private void askAIForMove(AIOpponent player) {
		if (aiMoveFuture != null) {
			aiMoveFuture.cancel(true);
		}

		aiMoveFuture = aiExecutor.submit(() -> {
			try {
				long startTime = System.currentTimeMillis();
				Move move = player.getNextMove(board);
				long endTime = System.currentTimeMillis();
				if ((endTime - startTime) / 1000000 < 200) {
					// Für weniger als 200 ms machen wir eine zusätzliche zufällige Verzögerung, das
					// wirkt dann natürlicher (max auf 2s gestreckt)
					try {
						Thread.sleep(100 + random.nextInt(1700));
					} catch (InterruptedException ignored) {
					}
				}
				if (!Thread.currentThread().isInterrupted()) {
					Platform.runLater(() -> {
						handleMove(move);
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			return null;
		});
	}

	/**
	 * Reduziert die verbleibende Zeit des aktuellen Spielers um 1 Sekunde. Diese
	 * Methode sollte exakt einmal pro Sekunde aufgerufen werden, um die Spielzeit
	 * korrekt zu aktualisieren. Wenn die verbleibende Zeit auf 0 fällt und das
	 * Spiel noch läuft, wird der Spieler automatisch zur Aufgabe gezwungen.
	 */
	public void decPlayerTimeBy1Second() {
		var currentPlayer = board.getCurrentPlayer();
		int remainingTime = currentPlayer.getRemainingTime();
		int newRemainingTime = Math.max(0, remainingTime - 1000);
		currentPlayer.setRemainingTime(newRemainingTime);

		// handle TimeOut
		if (newRemainingTime == 0 && board.getGameState() == GameState.RUNNING) {
			board.forfeitByTime(currentPlayer.getColor());
			endGame(board.getGameState());
		}
	}

	/**
	 * Fügt dem nächsten Spieler pro Zug zusätzliche Zeit hinzu, sofern Increment
	 * aktiviert (nach 2 Zügen) ist.
	 * <p>
	 * Die zusätzliche Zeit wird zur verbleibenden Zeit des nächsten Spielers
	 * addiert und das Uhren-Label entsprechend aktualisiert.
	 */
	private void incrementTime() {
		getNextPlayer().setRemainingTime(getNextPlayer().getRemainingTime() + increment);
	}

}
