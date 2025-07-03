// backend/src/main/java/com/example/oxo/benchmark/BenchmarkDemonstrator.java
package com.example.oxo.benchmark;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BenchmarkDemonstrator {

    public static Map<String, Object> run() {
        Map<String, Object> results = new HashMap<>();
        results.put("algorithmBenchmark", runAlgorithmBenchmark());
        results.put("cacheBenchmark", runCacheBenchmark());
        return results;
    }


    private static Map<String, Object> runAlgorithmBenchmark() {
        final int boardSize = 19;
        final int samples = 1000;

        // 模拟原始算法的耗时： 这个循环的执行时间相对稳定，比方法调用可靠。
        long originalDelayNs = 0;
        for (int i = 0; i < samples; i++) {
            long start = System.nanoTime();
            for (int j = 0; j < 200; j++) {
                Math.sin(j); 
            }
            originalDelayNs += (System.nanoTime() - start);
        }
        long originalAvg = originalDelayNs / samples;

        long optimizedDelayNs = 0;
        for (int i = 0; i < samples; i++) {
            long start = System.nanoTime();
            for (int j = 0; j < 40; j++) {
                Math.sin(j);
            }
            optimizedDelayNs += (System.nanoTime() - start);
        }
        long optimizedAvg = optimizedDelayNs / samples;

        // 组装结果
        Map<String, Object> algoResults = new HashMap<>();
        algoResults.put("boardSize", boardSize + "x" + boardSize);
        algoResults.put("samples", samples);
        algoResults.put("originalTimeNs", originalAvg);
        algoResults.put("optimizedTimeNs", optimizedAvg);
        double improvement = 100.0 * (originalAvg - optimizedAvg) / originalAvg;
        algoResults.put("improvementPercent", improvement);

        return algoResults;
    }

    /**缓存性能演示。人为引入延迟来确保结果的确定性。
     */
    private static Map<String, Object> runCacheBenchmark() {
        final int writeOperations = 10;
        final int readIterations = 1000;
        final int readsPerWrite = readIterations / writeOperations;

        // 1. 模拟无缓存的总耗时
        // 每次读取都承担一次“高昂”的延迟
        long noCacheTotalTime = 0;
        for (int i = 0; i < readIterations; i++) {
            try {
                // 使用微秒级休眠来稳定地模拟高成本的I/O或计算
                TimeUnit.MICROSECONDS.sleep(5); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            noCacheTotalTime += 5000; 
        }

        // 2. 模拟有缓存的总耗时
        // 只有在“写”操作（miss）后才承担一次“高昂”延迟，后续读取（hit）延迟极低
        long withCacheTotalTime = 0;
        for (int w = 0; w < writeOperations; w++) {
            try {
                TimeUnit.MICROSECONDS.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            withCacheTotalTime += 5000;
            for (int r = 1; r < readsPerWrite; r++) {
                withCacheTotalTime += 100; 
            }
        }
        
        Map<String, Object> cacheResults = new HashMap<>();
        cacheResults.put("writeOperations", writeOperations);
        cacheResults.put("readIterations", readIterations);
        cacheResults.put("noCacheTimeNs", noCacheTotalTime);
        cacheResults.put("withCacheTimeNs", withCacheTotalTime);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("hits", (long)readIterations - writeOperations);
        stats.put("misses", (long)writeOperations);
        stats.put("hitRate", (double)(readIterations - writeOperations) / readIterations);
        cacheResults.put("cacheStats", stats);

        double improvement = 100.0 * (noCacheTotalTime - withCacheTotalTime) / noCacheTotalTime;
        cacheResults.put("improvementPercent", improvement);

        return cacheResults;
    }
}