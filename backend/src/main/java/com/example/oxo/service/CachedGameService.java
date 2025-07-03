package com.example.oxo.service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.example.oxo.model.MoveException;

public class CachedGameService extends OptimizedGameService {
    
    private final Map<String, Object> stateCache = new ConcurrentHashMap<>();
    private final AtomicBoolean stateDirty = new AtomicBoolean(true);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final boolean cacheEnabled = true;

    // [FIX] 增加一个私有的、final的锁对象
    private final Object cacheLock = new Object();
    
    @Override
    public Object getGameState() {
        // 为了保证“检查-然后-行动”操作的原子性，必须使用锁保护的是 stateDirty 和 stateCache 之间的一致性，防止在读取脏位和返回缓存的间隙中状态被修改。
        synchronized (cacheLock) {
            // 如果状态未改变且缓存启用，直接返回缓存
            if (cacheEnabled && !stateDirty.get() && stateCache.containsKey("gameState")) {
                cacheHits.incrementAndGet();
                return stateCache.get("gameState");
            }
        }
        
        // 注意：计算新状态的耗时操作(super.getGameState())被刻意放在了同步块之外，
        // 以最大程度地减少锁的持有时间，提高并发性能。
        Object state = super.getGameState();
        
        // 第二个同步块，用于安全地更新缓存和状态位。
        synchronized (cacheLock) {
            // 双重检查锁定（Double-Checked Locking）模式：
            // 在我们计算状态的同时，可能有另一个线程已经完成了计算并更新了缓存。因此，在写入之前，我们再次检查状态。
            if (stateDirty.get()) {
                cacheMisses.incrementAndGet();
                stateCache.put("gameState", state);
                stateDirty.set(false);
            } else {
                // 如果计算期间，其他线程已更新缓存，则我们这次算作一次命中。直接返回那个更新后的缓存，丢弃我们自己计算的
                cacheHits.incrementAndGet();
                return stateCache.get("gameState");
            }
        }
        
        return state;
    }
    
    // 在所有修改游戏状态的方法中，也需要使用锁来安全地修改 stateDirty 标志。
    private void invalidateCache() {
        synchronized (cacheLock) {
            stateDirty.set(true);
        }
    }

    @Override
    public void handleIncomingCommand(String command) throws MoveException {
        invalidateCache();
        super.handleIncomingCommand(command);
    }
    
    @Override
    public void setPlayers(int count) {
        invalidateCache();
        super.setPlayers(count);
    }
    
    @Override
    public void setBoardSize(int newRows, int newCols) {
        invalidateCache();
        super.setBoardSize(newRows, newCols);
    }
    
    @Override
    public void resetGame() {
        synchronized(cacheLock) {
            stateDirty.set(true);
            stateCache.clear(); // 重置时清空缓存
        }
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
        synchronized(cacheLock) {
            stats.put("currentCacheStatus", stateDirty.get() ? "invalid" : "valid");
        }
        return stats;
    }
}