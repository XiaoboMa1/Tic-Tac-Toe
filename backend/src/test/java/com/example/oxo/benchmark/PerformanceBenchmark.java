package com.example.oxo.benchmark;

import com.example.oxo.model.GameModel;
import com.example.oxo.model.MoveException;
import com.example.oxo.service.GameService;
import com.example.oxo.service.OptimizedGameService;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PerformanceBenchmark {

    public static Map<String, Object> runBenchmark(int iterations) {
        Map<String, Object> results = new HashMap<>();
        int[] boardSizes = {3, 5}; // 考虑是否需要减少数组大小或迭代次数以避免超时
        // int[] boardSizes = {3}; // 例如，临时只测试3x3

        Map<String, Long> originalTimes = new HashMap<>();
        Map<String, Long> optimizedTimes = new HashMap<>();

        for (int size : boardSizes) {
            GameService originalService = new GameService();
            originalService.setBoardSize(size, size);
            originalService.setPlayers(2);
            long originalTime = benchmarkService(originalService, iterations, "Original");
            originalTimes.put(size + "x" + size, originalTime);

            OptimizedGameService optimizedService = new OptimizedGameService();
            optimizedService.setBoardSize(size, size);
            optimizedService.setPlayers(2);
            long optimizedTime = benchmarkService(optimizedService, iterations, "Optimized");
            optimizedTimes.put(size + "x" + size, optimizedTime);
        }

        results.put("original", originalTimes);
        results.put("optimized", optimizedTimes);

        Map<String, Double> improvements = new HashMap<>();
        for (String key : originalTimes.keySet()) {
            long original = originalTimes.get(key);
            long optimized = optimizedTimes.get(key);

            if (original == 0) {
                improvements.put(key, (optimized == 0) ? 0.0 : -100.0); // 表示优化后反而有耗时或不变
            } else {
                double improvement = 100.0 * (original - optimized) / (double) original; // 确保浮点除法
                improvements.put(key, improvement);
            }
        }
        results.put("improvements", improvements);
        return results;
    }
    
    /**
     * Runs detailed benchmark specifically for win-checking algorithms on a 19x19 board
     * Collects 1000 samples of execution times and calculates statistics
     */
    public static Map<String, Object> runWinCheckingBenchmark() {
        // Default to 1000 samples as specified in requirements
        return WinCheckBenchmark.runBenchmark(1000);
    }

    private static long benchmarkService(GameService service, int iterations, String serviceType) {
        long totalTimeNs = 0;
        Random random = new Random();

        if (iterations <= 0) {
            System.err.println("PerformanceBenchmark: Iterations must be positive. Received: " + iterations + " for " + serviceType);
            return 0;
        }

        for (int i = 0; i < iterations; i++) {
            service.resetGame();
            GameModel currentModel = service.getGameModel();
            int currentRows = currentModel.getNumberOfRows();
            int currentCols = currentModel.getNumberOfColumns();
            
            if (currentRows <= 0 || currentCols <= 0) {
                System.err.println("PerformanceBenchmark: Invalid board dimensions (" + currentRows + "x" + currentCols + ") for service " +
                                   serviceType + " at iteration " + (i + 1) + ". Skipping this game iteration.");
                continue; // 跳过当前外层迭代，进行下一次
            }

            int maxMovesThisIteration = currentRows * currentCols;
            int moveCount = 0;
            long iterationStartTimeNs = System.nanoTime(); // 记录单次迭代的总时间，包括所有内部操作

            while (moveCount < maxMovesThisIteration) {
                int row = random.nextInt(currentRows);
                int col = random.nextInt(currentCols);
                String command = (char) ('a' + row) + String.valueOf(col + 1);
                
                try {
                    service.handleIncomingCommand(command);
                } catch (MoveException e) {
                    // Log or ignore, this is part of the service's behavior under test.
                } catch (Exception e) {
                    System.err.println("PerformanceBenchmark (" + serviceType + "): Unexpected exception during handleIncomingCommand with command '" +
                                       command + "' on " + currentRows + "x" + currentCols + " board (Iteration " + (i + 1) + "). Error: " + e.getMessage());
                    e.printStackTrace();
                }

                moveCount++; // Increment move attempts

                GameModel modelAfterMove = service.getGameModel(); // Re-fetch model state
                if (modelAfterMove.getWinner() != null || modelAfterMove.isGameDrawn()) {
                    break;
                }
            }
            long iterationEndTimeNs = System.nanoTime();
            totalTimeNs += (iterationEndTimeNs - iterationStartTimeNs); // 累加的是整个迭代（一局游戏模拟）的时间

            if (moveCount == 0 && maxMovesThisIteration > 0) {
                System.err.println("PerformanceBenchmark (" + serviceType + "): No moves were made in iteration " + (i + 1) +
                                   " on " + currentRows + "x" + currentCols + " board.");
            }
        }
        // 返回的是多次迭代（多局游戏）的平均每局游戏模拟时间
        return totalTimeNs / iterations;
    }
}