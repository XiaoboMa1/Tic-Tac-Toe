package com.example.oxo.service;

import com.example.oxo.model.GameModel;
import com.example.oxo.model.Player;
import com.example.oxo.model.MoveException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import static java.lang.Character.toLowerCase;
import static java.lang.Math.max;
import static java.lang.Math.min;

@Service
public class GameService {

	private final GameModel gameModel;

	public GameService() {
		// 初始默认构造时，给3x3, 阈值=3, 先无玩家 (或可默认加2玩家)
		gameModel = new GameModel(3, 3, 3);
		// 如果想在启动时默认有2个玩家，可在此添加:
		// gameModel.addPlayer(new Player('X'));
		// gameModel.addPlayer(new Player('O'));
	}

	/**
	 * 返回当前游戏状态，用于前端显示。
	 * 包括：棋盘尺寸、棋盘cells、当前玩家、赢家、是否平局等。
	 */
	public Object getGameState() {
		var response = new java.util.HashMap<String, Object>();
		response.put("rows", gameModel.getNumberOfRows());
		response.put("cols", gameModel.getNumberOfColumns());
		response.put("winThreshold", gameModel.getWinThreshold());

		// 若玩家数组为空或某些空位还没填充，则避免NullPointer
		response.put("playerCount", gameModel.getNumberOfPlayers());
		int currentIndex = gameModel.getCurrentPlayerNumber();
		if (gameModel.getNumberOfPlayers() > 0 && currentIndex < gameModel.getNumberOfPlayers()) {
			response.put("currentPlayer", gameModel.getPlayerByNumber(currentIndex).getPlayingLetter());
		} else {
			response.put("currentPlayer", null);
		}

		response.put("winner", gameModel.getWinner() == null ? null : gameModel.getWinner().getPlayingLetter());
		response.put("drawn", gameModel.isGameDrawn());

		// 转换棋盘：若单元格为空，则用 ' '
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

	/** 设置玩家数量。例如传入4则会创建4个玩家：'A','B','C','D'，并重置游戏。 */
	public void setPlayers(int count) {
		if (count < 1) count = 1;
		// 限制到 26 以内
		// if (count > 26) count = 26;

		// 【新逻辑】如果玩家数大于当前行/列 -> 自动扩展棋盘
		int rows = gameModel.getNumberOfRows();
		int cols = gameModel.getNumberOfColumns();
		if (count > rows || count > cols) {
			setBoardSize(Math.max(rows, count), Math.max(cols, count));
		}

		// 重置玩家数组
		gameModel.resetPlayers(count);

		// 分配字母
		for (int i = 0; i < count; i++) {
			char letter = (char) ('A' + i);
			gameModel.setPlayer(i, new Player(letter));
		}

		resetGame(); // 重置对局
	}


	/**
	 * 设置棋盘大小。这里采用一次性改变行列数的逻辑：
	 * - 如果 newRows < 3 则置为 3；大于 9 则置为 9
	 * - 如果 newCols < 3 则置为 3；大于 9 则置为 9
	 * - 如果游戏已经结束，可选择允许或禁止修改，这里我们允许修改，但会重置对局
	 *   （你可以根据需求决定是否保留已有落子，也可以做“部分扩展”逻辑）
	 */
	public void setBoardSize(int newRows, int newCols) {
		// 合理化
		if (newRows < 3) newRows = 3;
		if (newRows > 9) newRows = 9;
		if (newCols < 3) newCols = 3;
		if (newCols > 9) newCols = 9;

		// 重新构建一个新的棋盘并复制回去，也可以使用 addRow,removeRow等方法来部分扩展
		gameModel.resizeBoard(newRows, newCols);

		// 重置对局
		resetGame();
	}

	/**
	 * 处理玩家的文本输入命令：如 "a1", "b2"
	 */
	public void handleIncomingCommand(String command) throws MoveException {
		// 如果游戏已结束，直接忽略
		if (gameModel.isGameDrawn() || gameModel.getWinner() != null) {
			return;
		}

		// 命令长度应为2
		if (command.length() != 2) {
			throw new MoveException.InvalidIdentifierLengthException(command.length());
		}

		char rowChar = toLowerCase(command.charAt(0));
		// 校验行字符是否在[a-z]
		if (!Character.isLowerCase(rowChar)) {
			throw new MoveException.InvalidIdentifierCharacterException(
					MoveException.InvalidIdentifierCharacterException.CharacterType.ROW, rowChar
			);
		}
		int row = rowChar - 'a'; // 'a'->0, 'b'->1, ...

		char colChar = command.charAt(1);
		if (!Character.isDigit(colChar)) {
			throw new MoveException.InvalidIdentifierCharacterException(
					MoveException.InvalidIdentifierCharacterException.CharacterType.COLUMN, colChar
			);
		}
		int col = Character.getNumericValue(colChar) - 1; // '1'->0, '2'->1, etc.

		// 检查越界
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

		// 检查是否已被占用
		if (gameModel.getCellOwner(row, col) != null) {
			throw new MoveException.CellAlreadyTakenException(row, col);
		}

		// 落子
		int currPlayer = gameModel.getCurrentPlayerNumber();
		gameModel.setCellOwner(row, col, gameModel.getPlayerByNumber(currPlayer));

		// 检查胜利
		if (checkForWinner(row, col)) {
			gameModel.setWinner(gameModel.getPlayerByNumber(currPlayer));
			return;
		}

		// 检查平局
		if (checkForDraw()) {
			gameModel.setGameDrawn(true);
		}

		// 轮转下一个玩家
		int newPlayer = (currPlayer + 1) % gameModel.getNumberOfPlayers();
		gameModel.setCurrentPlayerNumber(newPlayer);
	}

	// 重置游戏：清空盘面、移除胜者、移除平局状态
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

	private boolean checkForWinner(int row, int col) {
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
		return maxCount == gameModel.getWinThreshold();
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
		return maxCount == gameModel.getWinThreshold();
	}

	private boolean checkDiagonalWin(int row, int col) {
		char letter = getCurrentPlayerLetter();

		// 左上-右下
		int i = row, j = col, count = 0;
		while (!gameModel.isOutOfBounds(i, j)) {
			var cell = gameModel.getCellOwner(i, j);
			if (cell != null && cell.getPlayingLetter() == letter) {
				count++;
			} else break;
			i--; j--;
		}
		i = row + 1; j = col + 1;
		while (!gameModel.isOutOfBounds(i, j)) {
			var cell = gameModel.getCellOwner(i, j);
			if (cell != null && cell.getPlayingLetter() == letter) {
				count++;
			} else break;
			i++; j++;
		}
		if (count == gameModel.getWinThreshold()) return true;

		// 右上-左下
		i = row; j = col; count = 0;
		while (!gameModel.isOutOfBounds(i, j)) {
			var cell = gameModel.getCellOwner(i, j);
			if (cell != null && cell.getPlayingLetter() == letter) {
				count++;
			} else break;
			i--; j++;
		}
		i = row + 1; j = col - 1;
		while (!gameModel.isOutOfBounds(i, j)) {
			var cell = gameModel.getCellOwner(i, j);
			if (cell != null && cell.getPlayingLetter() == letter) {
				count++;
			} else break;
			i++; j--;
		}
		return count == gameModel.getWinThreshold();
	}

	private char getCurrentPlayerLetter() {
		int curr = gameModel.getCurrentPlayerNumber();
		return gameModel.getPlayerByNumber(curr).getPlayingLetter();
	}
}
