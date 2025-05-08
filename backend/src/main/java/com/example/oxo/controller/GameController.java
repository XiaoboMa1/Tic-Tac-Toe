package com.example.oxo.controller;

import com.example.oxo.benchmark.PerformanceBenchmark;
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
            Map<String, Object> result = performanceStats.getStats(); // performanceStats.getStats() already returns a Map
            if (gameService instanceof CachedGameService) {
                CachedGameService cachedService = (CachedGameService) gameService;
                // Ensure 'result' is mutable or create a new map if it's not
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
    
    @GetMapping("/benchmark")
    public ResponseEntity<Object> runBenchmarkController(@RequestParam(defaultValue = "10") int iterations) {
        // To test quickly, you might temporarily hardcode iterations here:
        // int testIterations = 1; // For quick testing
        // System.out.println("Running benchmark with iterations: " + testIterations); // Log actual iterations
        System.out.println("Received /benchmark request with iterations: " + iterations);

        long controllerCallStartMs = System.currentTimeMillis();
        try {
            Map<String, Object> benchmarkData = PerformanceBenchmark.runBenchmark(iterations); // Call with received/test iterations
            System.out.println("Benchmark execution completed. Returning results.");
            performanceStats.recordApiCall("runBenchmark", System.currentTimeMillis() - controllerCallStartMs);
            return ResponseEntity.ok(benchmarkData);
        } catch (Exception e) {
            System.err.println("GameController: Critical error during /benchmark endpoint execution: " + e.getMessage());
            e.printStackTrace();
            performanceStats.recordApiCall("runBenchmark_Error", System.currentTimeMillis() - controllerCallStartMs);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("error", "Benchmark execution failed due to an internal server error: " + e.getMessage()));
        }
        // finally block was here, but duration recording is better done before returning or in catch.
    }
    
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
            // 返回400错误 + JSON格式错误信息
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Invalid Move: " + e.getMessage())
            );
        } catch (Exception e) {
            // 捕获其他未处理的异常，返回500 错误
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