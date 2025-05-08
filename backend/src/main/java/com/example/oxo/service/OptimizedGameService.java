package com.example.oxo.service;

import com.example.oxo.model.Player; // Added import

public class OptimizedGameService extends GameService {

    // 方向向量：水平、垂直、主对角线、副对角线
    private static final int[][] DIRECTIONS = { // Made static final
        {0, 1}, {1, 0}, {1, 1}, {1, -1}
    };
    
    @Override
    protected boolean checkForWinner(int row, int col) {
        char targetLetter = super.getCurrentPlayerLetter(); // Use super or this, ensure visibility
        int winThreshold = super.getGameModel().getWinThreshold(); // Use getter or ensure visibility
        
        // 检查四个方向
        for (int[] dir : DIRECTIONS) {
            int count = 1; // 包含当前落子点
            
            // 正向检查
            count += countInDirection(row, col, dir[0], dir[1], targetLetter);
            // 反向检查
            count += countInDirection(row, col, -dir[0], -dir[1], targetLetter);
            
            if (count >= winThreshold) {
                return true;
            }
        }
        
        return false;
    }
    
    // 计算某方向上连续相同棋子的数量
    private int countInDirection(int startRow, int startCol, int rowDelta, int colDelta, char targetLetter) {
        int count = 0;
        int r = startRow + rowDelta;
        int c = startCol + colDelta;
        
        while (!super.getGameModel().isOutOfBounds(r, c)) { // Use getter or ensure visibility
            Player cell = super.getGameModel().getCellOwner(r, c); // Use getter or ensure visibility
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