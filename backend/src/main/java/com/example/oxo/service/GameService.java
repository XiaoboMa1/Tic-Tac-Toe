// src/main/java/com/example/oxo/service/GameService.java
package com.example.oxo.service;

import com.example.oxo.model.GameModel;
import com.example.oxo.model.Player;
import com.example.oxo.model.MoveException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import static java.lang.Character.toLowerCase;
import static java.lang.Math.max;

@Service
public class GameService {

	protected final GameModel gameModel;

	public GameService() {
		gameModel = new GameModel(3, 3, 3);
		gameModel.resetPlayers(2);
		gameModel.setPlayer(0,new Player('X'));
		gameModel.setPlayer(1,new Player('O'));
		for (int i = 0; i < gameModel.getNumberOfRows(); i++) {
			for (int j = 0; j < gameModel.getNumberOfColumns(); j++) {
				gameModel.setCellOwner(i, j, null);
			}
		}
		gameModel.setWinner(null);
		gameModel.setGameDrawn(false);
		gameModel.setCurrentPlayerNumber(0);
	}

	public Object getGameState() {
		var response = new java.util.HashMap<String, Object>();
		response.put("rows", gameModel.getNumberOfRows());
		response.put("cols", gameModel.getNumberOfColumns());
		response.put("winThreshold", gameModel.getWinThreshold());
		response.put("playerCount", gameModel.getNumberOfPlayers());
		int currentIndex = gameModel.getCurrentPlayerNumber();
		if (gameModel.getNumberOfPlayers() > 0 && currentIndex < gameModel.getNumberOfPlayers()) {
			response.put("currentPlayer", gameModel.getPlayerByNumber(currentIndex).getPlayingLetter());
		} else {
			response.put("currentPlayer", null);
		}
		response.put("winner", gameModel.getWinner() == null ? null : gameModel.getWinner().getPlayingLetter());
		response.put("drawn", gameModel.isGameDrawn());
		var board = new ArrayList<ArrayList<Character>>();
		for (int i = 0; i < gameModel.getNumberOfRows(); i++) {
			board.add(new ArrayList<>());
			for (int j = 0; j < gameModel.getNumberOfColumns(); j++) {
				Player cellOwner = gameModel.getCellOwner(i, j);
				board.get(i).add(cellOwner == null ? ' ' : cellOwner.getPlayingLetter());
			}
		}
		response.put("board", board);
		return response;
	}

	public void setPlayers(int count) {
		if (count < 1) count = 1;
		int rows = gameModel.getNumberOfRows();
		int cols = gameModel.getNumberOfColumns();
		if (count > rows || count > cols) {
			setBoardSize(Math.max(rows, count), Math.max(cols, count));
		}
		gameModel.resetPlayers(count);
		for (int i = 0; i < count; i++) {
			char letter = (char) ('A' + i);
			gameModel.setPlayer(i, new Player(letter));
		}
		resetGame();
	}

	/**
	 * [FIX #1] 设置棋盘大小。移除了9x9的硬编码上限，以支持基准测试。
	 * 保留了最小尺寸为3x3的限制。
	 */
	public void setBoardSize(int newRows, int newCols) {
		if (newRows < 3) newRows = 3;
		// if (newRows > 9) newRows = 9; // REMOVED
		if (newCols < 3) newCols = 3;
		// if (newCols > 9) newCols = 9; // REMOVED

		gameModel.resizeBoard(newRows, newCols);
		resetGame();
	}

	public void handleIncomingCommand(String command) throws MoveException {
		if (gameModel.getNumberOfPlayers() == 0) {
			throw new MoveException("No players set. Please set players first.");
		}
		if (gameModel.isGameDrawn() || gameModel.getWinner() != null) {
			return;
		}
		if (command.length() != 2) {
			throw new MoveException.InvalidIdentifierLengthException(command.length());
		}
		char rowChar = toLowerCase(command.charAt(0));
		if (!Character.isLowerCase(rowChar)) {
			throw new MoveException.InvalidIdentifierCharacterException(
					MoveException.InvalidIdentifierCharacterException.CharacterType.ROW, rowChar
			);
		}
		int row = rowChar - 'a';
		char colChar = command.charAt(1);
		if (!Character.isDigit(colChar)) {
			throw new MoveException.InvalidIdentifierCharacterException(
					MoveException.InvalidIdentifierCharacterException.CharacterType.COLUMN, colChar
			);
		}
		int col = Character.getNumericValue(colChar) - 1;
		if (row < 0 || row >= gameModel.getNumberOfRows()) {
			throw new MoveException.OutsideCellRangeException(
					MoveException.OutsideCellRangeException.CellInfo.ROW, row
			);
		}
		if (col < 0 || col >= gameModel.getNumberOfColumns()) {
			throw new MoveException.OutsideCellRangeException(
					MoveException.OutsideCellRangeException.CellInfo.COLUMN, col
			);
		}
		if (gameModel.getCellOwner(row, col) != null) {
			throw new MoveException.CellAlreadyTakenException(row, col);
		}
		int currPlayer = gameModel.getCurrentPlayerNumber();
		gameModel.setCellOwner(row, col, gameModel.getPlayerByNumber(currPlayer));
		if (checkForWinner(row, col)) {
			gameModel.setWinner(gameModel.getPlayerByNumber(currPlayer));
			return;
		}
		if (checkForDraw()) {
			gameModel.setGameDrawn(true);
		}
		int newPlayer = (currPlayer + 1) % gameModel.getNumberOfPlayers();
		gameModel.setCurrentPlayerNumber(newPlayer);
	}

	public void resetGame() {
		for (int i = 0; i < gameModel.getNumberOfRows(); i++) {
			for (int j = 0; j < gameModel.getNumberOfColumns(); j++) {
				gameModel.setCellOwner(i, j, null);
			}
		}
		gameModel.setWinner(null);
		gameModel.setGameDrawn(false);
		gameModel.setCurrentPlayerNumber(0);
	}

	private boolean checkForDraw() {
		for (int i = 0; i < gameModel.getNumberOfRows(); i++) {
			for (int j = 0; j < gameModel.getNumberOfColumns(); j++) {
				if (gameModel.getCellOwner(i, j) == null) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean checkForWinner(int row, int col) {
		if (checkVerticalWin(col)) return true;
		if (checkHorizontalWin(row)) return true;
		return checkDiagonalWin(row, col);
	}

	private boolean checkVerticalWin(int col) {
		int count = 0, maxCount = 0;
		char letter = getCurrentPlayerLetter();
		for (int i = 0; i < gameModel.getNumberOfRows(); i++) {
			var cell = gameModel.getCellOwner(i, col);
			if (cell != null && cell.getPlayingLetter() == letter) {
				count++;
			} else {
				count = 0;
			}
			maxCount = max(count, maxCount);
		}
		return maxCount >= gameModel.getWinThreshold(); // [FIX #2] Changed == to >=
	}

	private boolean checkHorizontalWin(int row) {
		int count = 0, maxCount = 0;
		char letter = getCurrentPlayerLetter();
		for (int j = 0; j < gameModel.getNumberOfColumns(); j++) {
			var cell = gameModel.getCellOwner(row, j);
			if (cell != null && cell.getPlayingLetter() == letter) {
				count++;
			} else {
				count = 0;
			}
			maxCount = max(count, maxCount);
		}
		return maxCount >= gameModel.getWinThreshold(); // [FIX #2] Changed == to >=
	}

	private boolean checkDiagonalWin(int row, int col) {
		char letter = getCurrentPlayerLetter();
		int winThreshold = gameModel.getWinThreshold();

		// This is a more robust way to check diagonals from the last move
		// It counts in 4 directions from the placed piece.
		
		// Direction 1: Top-Left to Bottom-Right
		int count1 = 1;
		for(int i=1; i<winThreshold; i++) { // Check towards top-left
			if(gameModel.isOutOfBounds(row-i, col-i) || gameModel.getCellOwner(row-i, col-i) == null || gameModel.getCellOwner(row-i, col-i).getPlayingLetter() != letter) break;
			count1++;
		}
		for(int i=1; i<winThreshold; i++) { // Check towards bottom-right
			if(gameModel.isOutOfBounds(row+i, col+i) || gameModel.getCellOwner(row+i, col+i) == null || gameModel.getCellOwner(row+i, col+i).getPlayingLetter() != letter) break;
			count1++;
		}
		if (count1 >= winThreshold) return true; // [FIX #2] Changed == to >=

		// Direction 2: Top-Right to Bottom-Left
		int count2 = 1;
		for(int i=1; i<winThreshold; i++) { // Check towards top-right
			if(gameModel.isOutOfBounds(row-i, col+i) || gameModel.getCellOwner(row-i, col+i) == null || gameModel.getCellOwner(row-i, col+i).getPlayingLetter() != letter) break;
			count2++;
		}
		for(int i=1; i<winThreshold; i++) { // Check towards bottom-left
			if(gameModel.isOutOfBounds(row+i, col-i) || gameModel.getCellOwner(row+i, col-i) == null || gameModel.getCellOwner(row+i, col-i).getPlayingLetter() != letter) break;
			count2++;
		}
		return count2 >= winThreshold; // [FIX #2] Changed == to >=
	}

	protected char getCurrentPlayerLetter() {
		int curr = gameModel.getCurrentPlayerNumber();
		return gameModel.getPlayerByNumber(curr).getPlayingLetter();
	}

	public GameModel getGameModel() {
		return gameModel;
	}
}