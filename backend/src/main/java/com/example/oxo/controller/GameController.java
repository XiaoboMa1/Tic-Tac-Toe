package com.example.oxo.controller;

import com.example.oxo.benchmark.BenchmarkDemonstrator; // 导入新的演示类
import com.example.oxo.model.MoveException;
import com.example.oxo.monitoring.PerformanceStats;
import com.example.oxo.service.CachedGameService;
import com.example.oxo.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/oxo")
public class GameController {
    private final GameService gameService;
    private final PerformanceStats performanceStats;
    
    @Autowired
    public GameController(GameService gameService, PerformanceStats performanceStats) {
        this.gameService = gameService;
        this.performanceStats = performanceStats;
    }

    @GetMapping("/performance")
    public Object getPerformanceStats() {
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> result = performanceStats.getStats();
            if (gameService instanceof CachedGameService) {
                CachedGameService cachedService = (CachedGameService) gameService;
                Map<String, Object> mutableResult = new HashMap<>(result);
                mutableResult.put("cacheStats", cachedService.getCacheStats());
                return mutableResult;
            }
            return result;
        } finally {
            long duration = System.currentTimeMillis() - start;
            performanceStats.recordApiCall("getPerformanceStats", duration);
        }
    }
    
    /**
     * [新增] 统一的性能优化演示端点。
     * 该端点会运行一系列精心设计的基准测试，
     * 用于清晰地展示算法优化和缓存策略带来的性能提升。
     * 返回的结果可以直接在前端进行结构化展示。
     */
    @GetMapping("/run-demonstration")
    public ResponseEntity<Object> runDemonstration() {
        long controllerCallStartMs = System.currentTimeMillis();
        try {
            System.out.println("Running unified performance demonstration...");
            // 调用新的、统一的演示器来执行所有基准测试
            Map<String, Object> benchmarkData = BenchmarkDemonstrator.run();
            System.out.println("Demonstration completed. Returning structured results.");
            performanceStats.recordApiCall("runDemonstration", System.currentTimeMillis() - controllerCallStartMs);
            return ResponseEntity.ok(benchmarkData);
        } catch (Exception e) {
            System.err.println("GameController: Critical error during /run-demonstration endpoint execution: " + e.getMessage());
            e.printStackTrace();
            performanceStats.recordApiCall("runDemonstration_Error", System.currentTimeMillis() - controllerCallStartMs);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("error", "Demonstration execution failed due to an internal server error: " + e.getMessage()));
        }
    }
    
    // --- 旧的 /benchmark 和 /winCheckBenchmark 端点已被移除 ---

    /** 获取当前棋局状态（玩家信息、棋盘、当前玩家、赢家等） */
    @GetMapping("/state")
    public Object getGameState() {
        long start = System.currentTimeMillis();
        try {
            return gameService.getGameState();
        } finally {
            long duration = System.currentTimeMillis() - start;
            performanceStats.recordApiCall("getGameState", duration);
        }
    }

    /** 处理玩家在前端输入的指令（如"a1", "b2"...） */
    @PostMapping("/move")
    public ResponseEntity<?> makeMove(@RequestBody MoveRequest request) {
        long start = System.currentTimeMillis();
        try {
            gameService.handleIncomingCommand(request.getCommand());
            return ResponseEntity.ok(gameService.getGameState());
        } catch (MoveException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Invalid Move: " + e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "Internal server error: " + e.getMessage())
            );
        } finally {
            long duration = System.currentTimeMillis() - start;
            performanceStats.recordApiCall("makeMove", duration);
        }
    }

    /** 设置玩家数量，并分配字母 */
    @PostMapping("/setPlayers")
    public Object setPlayers(@RequestParam int count) {
        long start = System.currentTimeMillis();
        try {
            gameService.setPlayers(count);
            return gameService.getGameState();
        } finally {
            long duration = System.currentTimeMillis() - start;
            performanceStats.recordApiCall("setPlayers", duration);
        }
    }

    /** 设置棋盘大小（行数与列数），使玩家可以在输入框里指定行列数 */
    @PostMapping("/setSize")
    public Object setBoardSize(@RequestParam int rows, @RequestParam int cols) {
        long start = System.currentTimeMillis();
        try {
            gameService.setBoardSize(rows, cols);
            return gameService.getGameState();
        } finally {
            long duration = System.currentTimeMillis() - start;
            performanceStats.recordApiCall("setBoardSize", duration);
        }
    }
    
    @PostMapping("/reset")
    public Object resetGame() {
        long start = System.currentTimeMillis();
        try {
            gameService.resetGame();
            return gameService.getGameState();
        } finally {
            long duration = System.currentTimeMillis() - start;
            performanceStats.recordApiCall("resetGame", duration);
        }
    }
}