package com.example.oxo.service;
import com.example.oxo.model.MoveException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class CachedGameService extends OptimizedGameService {
    
    private final Map<String, Object> stateCache = new ConcurrentHashMap<>();
    private final AtomicBoolean stateDirty = new AtomicBoolean(true);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final boolean cacheEnabled = true; // 控制缓存启用状态
    
    @Override
    public Object getGameState() {
        // 如果状态未改变且缓存启用，直接返回缓存
        if (cacheEnabled && !stateDirty.get() && stateCache.containsKey("gameState")) {
            cacheHits.incrementAndGet();
            return stateCache.get("gameState");
        }
        
        cacheMisses.incrementAndGet();
        // 计算新状态
        Object state = super.getGameState();
        
        // 更新缓存
        stateCache.put("gameState", state);
        stateDirty.set(false);
        
        return state;
    }
    
    // 标记所有修改游戏状态的方法
    @Override
    public void handleIncomingCommand(String command) throws MoveException {
        stateDirty.set(true);
        super.handleIncomingCommand(command);
    }
    
    @Override
    public void setPlayers(int count) {
        stateDirty.set(true);
        super.setPlayers(count);
    }
    
    @Override
    public void setBoardSize(int newRows, int newCols) {
        stateDirty.set(true);
        super.setBoardSize(newRows, newCols);
    }
    
    @Override
    public void resetGame() {
        stateDirty.set(true);
        stateCache.clear(); // 重置时清空缓存
        super.resetGame();
    }

    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("hits", cacheHits.get());
        stats.put("misses", cacheMisses.get());
        stats.put("hitRate", (cacheHits.get() + cacheMisses.get() == 0) ? 0 : 
                  (double) cacheHits.get() / (cacheHits.get() + cacheMisses.get()));
        stats.put("cacheSize", stateCache.size());
        stats.put("cacheEnabled", cacheEnabled);
        stats.put("currentCacheStatus", stateDirty.get() ? "invalid" : "valid");
        return stats;
    }
}