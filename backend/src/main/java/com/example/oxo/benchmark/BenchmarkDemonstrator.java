// backend/src/main/java/com/example/oxo/benchmark/BenchmarkDemonstrator.java
package com.example.oxo.benchmark;

import java.util.HashMap;
import java.util.Map;

import com.example.oxo.model.MoveException;
import com.example.oxo.model.Player;
import com.example.oxo.service.CachedGameService;
import com.example.oxo.service.GameService;
import com.example.oxo.service.OptimizedGameService;

public class BenchmarkDemonstrator {

    public static Map<String, Object> run() {
        Map<String, Object> results = new HashMap<>();
        try {
            // 运行两个基准测试，并将结果放入Map
            results.put("algorithmBenchmark", runAlgorithmBenchmark());
            results.put("cacheBenchmark", runCacheBenchmark());
        } catch (Exception e) {
            // 如果在准备或运行基准测试时发生任何错误，将其包装成运行时异常抛出
            // GameController会捕获这个异常并返回500错误
            throw new RuntimeException("Failed to execute benchmark demonstration", e);
        }
        return results;
    }

    /**
     * [修复版] 演示算法优化效果。
     * 使用更复杂的测试场景来真实测量算法性能差异。
     */
    private static Map<String, Object> runAlgorithmBenchmark() {
        final int boardSize = 19;
        final int winThreshold = 5;
        final int samples = 1000; // 减少样本量但增加场景复杂度

        // --- 准备一个更复杂的测试场景 ---
        final int testRow = boardSize / 2;
        final int testCol = boardSize / 2;

        // 1. 原始算法测试 (GameService)
        GameService originalService = new GameService();
        originalService.setPlayers(2); 
        originalService.setBoardSize(boardSize, boardSize);
        originalService.getGameModel().setWinThreshold(winThreshold);
        
        // 创建更复杂的棋盘布局 - 多条近似获胜线
        Player player1 = originalService.getGameModel().getPlayerByNumber(0);
        Player player2 = originalService.getGameModel().getPlayerByNumber(1);
        
        // 在棋盘上放置更多棋子形成复杂的检查场景
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((i + j) % 3 == 0) {
                    originalService.getGameModel().setCellOwner(i + 5, j + 5, player1);
                } else if ((i + j) % 5 == 0) {
                    originalService.getGameModel().setCellOwner(i + 5, j + 5, player2);
                }
            }
        }

        long originalTotalTime = 0;
        // JIT预热
        for (int i = 0; i < 1000; i++) {
            originalService.checkForWinner(testRow, testCol);
        }
        // 正式测量 - 测试不同位置
        for (int i = 0; i < samples; i++) {
            int testR = (testRow + i % 5) % boardSize;
            int testC = (testCol + i % 7) % boardSize;
            long start = System.nanoTime();
            originalService.checkForWinner(testR, testC);
            originalTotalTime += (System.nanoTime() - start);
        }
        long originalAvg = originalTotalTime / samples;

        // 2. 优化算法测试 (OptimizedGameService)
        OptimizedGameService optimizedService = new OptimizedGameService();
        optimizedService.setPlayers(2);
        optimizedService.setBoardSize(boardSize, boardSize);
        optimizedService.getGameModel().setWinThreshold(winThreshold);
        
        // 同样的复杂棋盘布局
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((i + j) % 3 == 0) {
                    optimizedService.getGameModel().setCellOwner(i + 5, j + 5, 
                        optimizedService.getGameModel().getPlayerByNumber(0));
                } else if ((i + j) % 5 == 0) {
                    optimizedService.getGameModel().setCellOwner(i + 5, j + 5, 
                        optimizedService.getGameModel().getPlayerByNumber(1));
                }
            }
        }

        long optimizedTotalTime = 0;
        // JIT预热
        for (int i = 0; i < 1000; i++) {
            optimizedService.checkForWinner(testRow, testCol);
        }
        // 正式测量 - 相同的测试位置
        for (int i = 0; i < samples; i++) {
            int testR = (testRow + i % 5) % boardSize;
            int testC = (testCol + i % 7) % boardSize;
            long start = System.nanoTime();
            optimizedService.checkForWinner(testR, testC);
            optimizedTotalTime += (System.nanoTime() - start);
        }
        long optimizedAvg = optimizedTotalTime / samples;

        // 3. 组装结果
        Map<String, Object> algoResults = new HashMap<>();
        algoResults.put("boardSize", boardSize + "x" + boardSize);
        algoResults.put("samples", samples);
        algoResults.put("originalTimeNs", originalAvg);
        algoResults.put("optimizedTimeNs", optimizedAvg);
        
        // 修复性能提升计算，确保结果合理
        double improvement;
        if (originalAvg > 0 && optimizedAvg > 0) {
            improvement = 100.0 * (originalAvg - optimizedAvg) / originalAvg;
        } else {
            improvement = 0.0;
        }
        algoResults.put("improvementPercent", improvement);

        return algoResults;
    }

    /**
     * [修复版] 演示缓存优化效果。
     * 修改测试模式为真正的"读多写少"场景。
     */
    private static Map<String, Object> runCacheBenchmark() throws MoveException {
        final int readIterations = 1000;
        final int writeOperations = 10; // 增加多次写操作来模拟真实游戏

        // 1. 无缓存服务 (使用 OptimizedGameService 作为基线)
        OptimizedGameService noCacheService = new OptimizedGameService();
        noCacheService.setPlayers(2);
        noCacheService.setBoardSize(5, 5);
        
        long noCacheTotalTime = 0;
        // 模拟真实游戏场景：多次写操作，每次写操作后多次读操作
        for (int w = 0; w < writeOperations; w++) {
            // 写操作
            String command = (char)('a' + w % 5) + String.valueOf((w % 5) + 1);
            try {
                noCacheService.handleIncomingCommand(command);
            } catch (Exception e) {
                // 忽略无效移动
            }
            
            // 多次读操作
            for (int r = 0; r < readIterations / writeOperations; r++) {
                long start = System.nanoTime();
                noCacheService.getGameState();
                noCacheTotalTime += (System.nanoTime() - start);
            }
        }

        // 2. 有缓存服务
        CachedGameService cacheService = new CachedGameService();
        cacheService.setPlayers(2);
        cacheService.setBoardSize(5, 5);

        long cacheTotalTime = 0;
        // 相同的测试模式
        for (int w = 0; w < writeOperations; w++) {
            // 写操作（导致缓存失效）
            String command = (char)('a' + w % 5) + String.valueOf((w % 5) + 1);
            try {
                cacheService.handleIncomingCommand(command);
            } catch (Exception e) {
                // 忽略无效移动
            }
            
            // 多次读操作（第一次是cache miss，后续是cache hit）
            for (int r = 0; r < readIterations / writeOperations; r++) {
                long start = System.nanoTime();
                cacheService.getGameState();
                cacheTotalTime += (System.nanoTime() - start);
            }
        }

        // 3. 组装结果
        Map<String, Object> cacheResults = new HashMap<>();
        cacheResults.put("readIterations", readIterations);
        cacheResults.put("writeOperations", writeOperations);
        cacheResults.put("noCacheTimeNs", noCacheTotalTime);
        cacheResults.put("withCacheTimeNs", cacheTotalTime);
        cacheResults.put("cacheStats", cacheService.getCacheStats());
        
        double improvement = (noCacheTotalTime > 0) ? 
            (100.0 * (noCacheTotalTime - cacheTotalTime) / noCacheTotalTime) : 0;
        cacheResults.put("improvementPercent", improvement);

        return cacheResults;
    }
}