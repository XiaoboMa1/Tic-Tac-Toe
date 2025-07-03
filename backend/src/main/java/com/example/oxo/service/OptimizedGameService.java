package com.example.oxo.service;

import com.example.oxo.model.GameModel;
import com.example.oxo.model.Player;

public class OptimizedGameService extends GameService {

    // 方向向量：水平、垂直、主对角线、副对角线
    private static final int[][] DIRECTIONS = {
        {0, 1}, {1, 0}, {1, 1}, {1, -1}
    };
    
    @Override
    public boolean checkForWinner(int row, int col) {
        // 缓存常用对象和值，减少方法调用开销
        GameModel gameModel = super.getGameModel();
        char targetLetter = super.getCurrentPlayerLetter();
        int winThreshold = gameModel.getWinThreshold();
        int rows = gameModel.getNumberOfRows();
        int cols = gameModel.getNumberOfColumns();
        
        // 检查四个方向
        for (int[] dir : DIRECTIONS) {
            int count = 1; // 包含当前落子点
            
            // 正向检查
            count += countInDirection(gameModel, row, col, dir[0], dir[1], targetLetter, rows, cols);
            // 反向检查
            count += countInDirection(gameModel, row, col, -dir[0], -dir[1], targetLetter, rows, cols);
            
            if (count >= winThreshold) {
                return true;
            }
        }
        
        return false;
    }
    
    // 优化版本：传入缓存的值，减少方法调用
    private int countInDirection(GameModel gameModel, int startRow, int startCol, 
                                int rowDelta, int colDelta, char targetLetter, int maxRows, int maxCols) {
        int count = 0;
        int r = startRow + rowDelta;
        int c = startCol + colDelta;
        
        // 内联边界检查，避免方法调用
        while (r >= 0 && r < maxRows && c >= 0 && c < maxCols) {
            Player cell = gameModel.getCellOwner(r, c);
            if (cell != null && cell.getPlayingLetter() == targetLetter) {
                count++;
                r += rowDelta;
                c += colDelta;
            } else {
                break;
            }
        }
        
        return count;
    }
}