//src/main/java/com/example/oxo/benchmark/BenchmarkDemonstrator.java
package com.example.oxo.benchmark;

import java.util.HashMap;
import java.util.Map;

import com.example.oxo.service.CachedGameService;
import com.example.oxo.service.GameService;
import com.example.oxo.service.OptimizedGameService;

public class BenchmarkDemonstrator {

    public static Map<String, Object> run() {
        Map<String, Object> results = new HashMap<>();
        results.put("algorithmBenchmark", runAlgorithmBenchmark());
        results.put("cacheBenchmark", runCacheBenchmark());
        return results;
    }

    /**
     * 演示算法优化效果。
     * 使用大棋盘（19x19）来凸显优化算法的优势。
     * 基于 WinCheckBenchmark 的精确测量思想。
     */
    private static Map<String, Object> runAlgorithmBenchmark() {
        // 使用 WinCheckBenchmark 的内部逻辑，但简化于此
        // 注意：实际项目中，可以重用或重构 WinCheckBenchmark 的代码
        final int boardSize = 19;
        final int winThreshold = 5;
        final int samples = 1000;

        // 1. 原始算法测试
        GameService originalService = new GameService();
        originalService.setBoardSize(boardSize, boardSize);
        originalService.getGameModel().setWinThreshold(winThreshold);
        long originalTotalTime = 0;
        for (int i = 0; i < samples; i++) {
            long start = System.nanoTime();
            originalService.checkForWinner(i % boardSize, i % boardSize); // 伪调用
            originalTotalTime += (System.nanoTime() - start);
        }
        long originalAvg = originalTotalTime / samples;

        // 2. 优化算法测试
        OptimizedGameService optimizedService = new OptimizedGameService();
        optimizedService.setBoardSize(boardSize, boardSize);
        optimizedService.getGameModel().setWinThreshold(winThreshold);
        long optimizedTotalTime = 0;
        for (int i = 0; i < samples; i++) {
            long start = System.nanoTime();
            optimizedService.checkForWinner(i % boardSize, i % boardSize); // 伪调用
            optimizedTotalTime += (System.nanoTime() - start);
        }
        long optimizedAvg = optimizedTotalTime / samples;

        // 3. 组装结果
        Map<String, Object> algoResults = new HashMap<>();
        algoResults.put("boardSize", boardSize + "x" + boardSize);
        algoResults.put("samples", samples);
        algoResults.put("originalTimeNs", originalAvg);
        algoResults.put("optimizedTimeNs", optimizedAvg);
        double improvement = 100.0 * (originalAvg - optimizedAvg) / originalAvg;
        algoResults.put("improvementPercent", improvement);

        return algoResults;
    }

    /**
     * 演示缓存优化效果。
     * 模拟“读多写少”场景。
     */
    private static Map<String, Object> runCacheBenchmark() {
        final int readIterations = 1000;

        // 1. 无缓存测试
        OptimizedGameService noCacheService = new OptimizedGameService();
        noCacheService.setBoardSize(5, 5);
        long noCacheStart = System.nanoTime();
        for (int i = 0; i < readIterations; i++) {
            noCacheService.getGameState();
        }
        long noCacheTotalTime = System.nanoTime() - noCacheStart;

        // 2. 有缓存测试
        CachedGameService cacheService = new CachedGameService();
        cacheService.setBoardSize(5, 5);
        // 第一次写操作，使缓存失效
        try {
            cacheService.handleIncomingCommand("a1");
        } catch (Exception ignored) {}
        
        long cacheStart = System.nanoTime();
        // 第一次读，是 cache miss
        cacheService.getGameState(); 
        // 后续的读，是 cache hit
        for (int i = 0; i < readIterations; i++) {
            cacheService.getGameState();
        }
        long cacheTotalTime = System.nanoTime() - cacheStart;

        // 3. 组装结果
        Map<String, Object> cacheResults = new HashMap<>();
        cacheResults.put("readIterations", readIterations);
        cacheResults.put("noCacheTimeNs", noCacheTotalTime);
        cacheResults.put("withCacheTimeNs", cacheTotalTime);
        cacheResults.put("cacheStats", cacheService.getCacheStats());
        double improvement = 100.0 * (noCacheTotalTime - cacheTotalTime) / noCacheTotalTime;
        cacheResults.put("improvementPercent", improvement);

        return cacheResults;
    }
}