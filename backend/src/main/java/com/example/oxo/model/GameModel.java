package com.example.oxo.model;

import java.util.ArrayList;
import java.util.Arrays;

public class GameModel {
	private ArrayList<ArrayList<Player>> cells;
	private Player[] players;
	private int currentPlayerNumber;
	private Player winner;
	private boolean gameDrawn;
	private int winThreshold;  // 达到多少连线即可获胜

	public GameModel(int numberOfRows, int numberOfColumns, int winThresh) {
		this.winThreshold = winThresh;
		this.cells = new ArrayList<>(numberOfRows);
		for (int i = 0; i < numberOfRows; i++) {
			var row = new ArrayList<Player>(numberOfColumns);
			for (int j = 0; j < numberOfColumns; j++) {
				row.add(null);
			}
			cells.add(row);
		}
		this.players = new Player[0]; // 默认无玩家
		this.winner = null;
		this.gameDrawn = false;
		this.currentPlayerNumber = 0;
	}

	// --- 棋盘相关 ---
	public int getNumberOfRows() {
		return cells.size();
	}
	public int getNumberOfColumns() {
		if (cells.isEmpty()) return 0;
		return cells.get(0).size();
	}
	public boolean isOutOfBounds(int row, int col) {
		return row < 0 || row >= getNumberOfRows()
				|| col < 0 || col >= getNumberOfColumns();
	}

	// 读取/设置棋盘某格的拥有者
	public Player getCellOwner(int rowNumber, int colNumber) {
		return cells.get(rowNumber).get(colNumber);
	}
	public void setCellOwner(int rowNumber, int colNumber, Player player) {
		cells.get(rowNumber).set(colNumber, player);
	}

	/**
	 * 直接将游戏棋盘修改为 newRows x newCols 的大小。
	 * 可用于一次性调整大小。此处简单地清空旧cells，重建新board。
	 * 如果想保留老数据，需额外写逻辑进行拷贝对齐。
	 */
	public void resizeBoard(int newRows, int newCols) {
		var newCells = new ArrayList<ArrayList<Player>>(newRows);
		for (int i = 0; i < newRows; i++) {
			var row = new ArrayList<Player>(newCols);
			for (int j = 0; j < newCols; j++) {
				row.add(null);
			}
			newCells.add(row);
		}
		this.cells = newCells;
	}

	// --- 玩家相关 ---
	public int getNumberOfPlayers() {
		return players.length;
	}
	public Player getPlayerByNumber(int number) {
		return players[number];
	}

	/**
	 * 在 setPlayers() 时，会按数量重新构造 players，
	 * 这里专门做一个 resetPlayers(count) 方法来改大小。
	 */
	public void resetPlayers(int count) {
		players = new Player[count];
	}

	/** 设置某个玩家位于 players[] 数组的 i 位置 */
	public void setPlayer(int index, Player player) {
		players[index] = player;
	}

	// --- 当前回合、赢家、平局等 ---
	public int getCurrentPlayerNumber() {
		return currentPlayerNumber;
	}
	public void setCurrentPlayerNumber(int playerNumber) {
		currentPlayerNumber = playerNumber;
	}

	public Player getWinner() {
		return winner;
	}
	public void setWinner(Player player) {
		winner = player;
	}

	public boolean isGameDrawn() {
		return gameDrawn;
	}
	public void setGameDrawn(boolean isDrawn) {
		gameDrawn = isDrawn;
	}

	public int getWinThreshold() {
		return winThreshold;
	}
	public void setWinThreshold(int winThresh) {
		winThreshold = winThresh;
	}
}
