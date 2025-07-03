// backend/src/main/java/com/example/oxo/benchmark/BenchmarkDemonstrator.java
package com.example.oxo.benchmark;

import java.util.HashMap;
import java.util.Map;

import com.example.oxo.model.MoveException;
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
     * [最终修复版] 演示算法优化效果。
     * 此版本在一个预先填充了数据的、有意义的棋盘上进行测试，以测量算法在真实负载下的性能。
     * 场景：在一个19x19的棋盘上，一条线上已经有4颗连续的棋子，我们测量落下第5颗致胜棋子时的检查效率。
     */
    private static Map<String, Object> runAlgorithmBenchmark() {
        final int boardSize = 19;
        final int winThreshold = 5;
        final int samples = 5000; // 增加样本量以获得更稳定的平均值

        // --- 准备一个有意义的测试场景 ---
        final int testRow = boardSize / 2;
        final int testCol = boardSize / 2;

        // 1. 原始算法测试 (GameService)
        GameService originalService = new GameService();
        // 必须先设置玩家，否则无法落子
        originalService.setPlayers(2); 
        originalService.setBoardSize(boardSize, boardSize);
        originalService.getGameModel().setWinThreshold(winThreshold);
        // 预先填充棋盘，在(testRow, testCol)左侧形成一条水平的4连子
        for (int i = 1; i < winThreshold; i++) {
            originalService.getGameModel().setCellOwner(testRow, testCol - i, originalService.getGameModel().getPlayerByNumber(0));
        }

        long originalTotalTime = 0;
        // JIT预热：在正式计时前，先运行几轮让Java虚拟机优化代码
        for (int i = 0; i < 10000; i++) {
            originalService.checkForWinner(testRow, testCol);
        }
        // 正式测量
        for (int i = 0; i < samples; i++) {
            long start = System.nanoTime();
            originalService.checkForWinner(testRow, testCol);
            originalTotalTime += (System.nanoTime() - start);
        }
        long originalAvg = originalTotalTime / samples;

        // 2. 优化算法测试 (OptimizedGameService)
        OptimizedGameService optimizedService = new OptimizedGameService();
        optimizedService.setPlayers(2);
        optimizedService.setBoardSize(boardSize, boardSize);
        optimizedService.getGameModel().setWinThreshold(winThreshold);
        // 同样预先填充棋盘
        for (int i = 1; i < winThreshold; i++) {
            optimizedService.getGameModel().setCellOwner(testRow, testCol - i, optimizedService.getGameModel().getPlayerByNumber(0));
        }

        long optimizedTotalTime = 0;
        // JIT预热
        for (int i = 0; i < 10000; i++) {
            optimizedService.checkForWinner(testRow, testCol);
        }
        // 正式测量
        for (int i = 0; i < samples; i++) {
            long start = System.nanoTime();
            optimizedService.checkForWinner(testRow, testCol);
            optimizedTotalTime += (System.nanoTime() - start);
        }
        long optimizedAvg = optimizedTotalTime / samples;

        // 3. 组装结果
        Map<String, Object> algoResults = new HashMap<>();
        algoResults.put("boardSize", boardSize + "x" + boardSize);
        algoResults.put("samples", samples);
        algoResults.put("originalTimeNs", originalAvg);
        algoResults.put("optimizedTimeNs", optimizedAvg);
        // 确保 originalAvg 不为0，避免除零错误
        double improvement = (originalAvg > 0) ? (100.0 * (originalAvg - optimizedAvg) / originalAvg) : 0;
        algoResults.put("improvementPercent", improvement);

        return algoResults;
    }

    /**
     * 演示缓存优化效果。
     * 这个测试的逻辑是正确的，因为它模拟了“读多写少”的场景。
     * 为确保健壮性，我们添加了必要的 setPlayers 调用。
     */
    private static Map<String, Object> runCacheBenchmark() throws MoveException {
        final int readIterations = 1000;

        // 1. 无缓存服务 (使用 OptimizedGameService 作为基线)
        OptimizedGameService noCacheService = new OptimizedGameService();
        noCacheService.setPlayers(2);
        noCacheService.setBoardSize(5, 5);
        noCacheService.handleIncomingCommand("a1"); // 一次写操作

        long noCacheTotalTime = 0;
        for (int i = 0; i < readIterations; i++) {
            long start = System.nanoTime();
            noCacheService.getGameState(); // 每次都重新计算
            noCacheTotalTime += (System.nanoTime() - start);
        }

        // 2. 有缓存服务
        CachedGameService cacheService = new CachedGameService();
        cacheService.setPlayers(2);
        cacheService.setBoardSize(5, 5);
        cacheService.handleIncomingCommand("a1"); // 写操作，使缓存失效并准备下一次读取

        long cacheTotalTime = 0;
        // 第一次读，是 cache miss，但会填充缓存
        long firstReadStart = System.nanoTime();
        cacheService.getGameState();
        cacheTotalTime += (System.nanoTime() - firstReadStart);

        // 后续的读，都应该是 cache hit
        for (int i = 1; i < readIterations; i++) {
            long start = System.nanoTime();
            cacheService.getGameState();
            cacheTotalTime += (System.nanoTime() - start);
        }

        // 3. 组装结果
        Map<String, Object> cacheResults = new HashMap<>();
        cacheResults.put("readIterations", readIterations);
        cacheResults.put("noCacheTimeNs", noCacheTotalTime);
        cacheResults.put("withCacheTimeNs", cacheTotalTime);
        cacheResults.put("cacheStats", cacheService.getCacheStats());
        double improvement = (noCacheTotalTime > 0) ? (100.0 * (noCacheTotalTime - cacheTotalTime) / noCacheTotalTime) : 0;
        cacheResults.put("improvementPercent", improvement);

        return cacheResults;
    }
}