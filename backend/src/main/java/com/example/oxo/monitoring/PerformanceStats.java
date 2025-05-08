package com.example.oxo.monitoring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class PerformanceStats {
    
    private final Map<String, ApiStat> apiStats = new ConcurrentHashMap<>();
    private final long startTime = System.currentTimeMillis();
    
    public void recordApiCall(String apiName, long durationMs) {
        apiStats.computeIfAbsent(apiName, k -> new ApiStat())
                .recordCall(durationMs);
    }
    
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("uptime", System.currentTimeMillis() - startTime);
        stats.put("apiCalls", apiStats);
        return stats;
    }
    
    public static class ApiStat {
        private long callCount = 0;
        private long totalTimeMs = 0;
        private long minTimeMs = Long.MAX_VALUE;
        private long maxTimeMs = 0;
        
        public synchronized void recordCall(long timeMs) {
            callCount++;
            totalTimeMs += timeMs;
            minTimeMs = Math.min(minTimeMs, timeMs);
            maxTimeMs = Math.max(maxTimeMs, timeMs);
        }
        
        public long getCallCount() {
            return callCount;
        }
        
        public long getTotalTimeMs() {
            return totalTimeMs;
        }
        
        public long getMinTimeMs() {
            return minTimeMs == Long.MAX_VALUE ? 0 : minTimeMs;
        }
        
        public long getMaxTimeMs() {
            return maxTimeMs;
        }
        
        public double getAvgTimeMs() {
            return callCount > 0 ? (double) totalTimeMs / callCount : 0;
        }
    }
}