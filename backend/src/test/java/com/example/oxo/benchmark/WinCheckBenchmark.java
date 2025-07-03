package com.example.oxo.benchmark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.example.oxo.model.MoveException;
import com.example.oxo.service.GameService;
import com.example.oxo.service.OptimizedGameService;

public class WinCheckBenchmark {

    // Subclass to measure original algorithm performance
    private static class TimedGameService extends GameService {
        private final List<Long> checkForWinnerTimes = new ArrayList<>();
        
        @Override
        public boolean checkForWinner(int row, int col) {
            long startTime = System.nanoTime();
            boolean result = super.checkForWinner(row, col);
            long endTime = System.nanoTime();
            checkForWinnerTimes.add(endTime - startTime);
            return result;
        }
        
        @Override
        public void setBoardSize(int newRows, int newCols) {
            // Remove upper limit to allow 19x19 boards
            if (newRows < 3) newRows = 3;
            if (newCols < 3) newCols = 3;
            
            // Resize board and set an appropriate win threshold for larger boards
            gameModel.resizeBoard(newRows, newCols);
            if (newRows >= 15 || newCols >= 15) {
                gameModel.setWinThreshold(5); // Use 5-in-a-row for large boards
            }
            resetGame();
        }
        
        public List<Long> getCheckForWinnerTimes() {
            return checkForWinnerTimes;
        }
    }
    
    // Subclass to measure optimized algorithm performance
    private static class TimedOptimizedGameService extends OptimizedGameService {
        private final List<Long> checkForWinnerTimes = new ArrayList<>();
        
        @Override
        public boolean checkForWinner(int row, int col) {
            long startTime = System.nanoTime();
            boolean result = super.checkForWinner(row, col);
            long endTime = System.nanoTime();
            checkForWinnerTimes.add(endTime - startTime);
            return result;
        }
        
        @Override
        public void setBoardSize(int newRows, int newCols) {
            // Remove upper limit to allow 19x19 boards
            if (newRows < 3) newRows = 3;
            if (newCols < 3) newCols = 3;
            
            // Resize board and set an appropriate win threshold for larger boards
            gameModel.resizeBoard(newRows, newCols);
            if (newRows >= 15 || newCols >= 15) {
                gameModel.setWinThreshold(5); // Use 5-in-a-row for large boards
            }
            resetGame();
        }
        
        public List<Long> getCheckForWinnerTimes() {
            return checkForWinnerTimes;
        }
    }
    
    public static Map<String, Object> runBenchmark(int sampleSize) {
        Map<String, Object> results = new HashMap<>();
        
        // Create timed service instances
        TimedGameService originalService = new TimedGameService();
        TimedOptimizedGameService optimizedService = new TimedOptimizedGameService();
        
        // Set up 19x19 boards
        originalService.setBoardSize(19, 19);
        optimizedService.setBoardSize(19, 19);
        
        // Set 2 players
        originalService.setPlayers(2);
        optimizedService.setPlayers(2);
        
        // Use fixed random seed for reproducibility
        Random random = new Random(42);
        
        System.out.println("Starting win-checking benchmark for 19x19 board...");
        int gamesPlayed = 0;
        
        // Keep simulating games until we collect enough samples
        while (originalService.getCheckForWinnerTimes().size() < sampleSize || 
               optimizedService.getCheckForWinnerTimes().size() < sampleSize) {
               
            // Reset both services for a new game
            originalService.resetGame();
            optimizedService.resetGame();
            gamesPlayed++;
            
            // Get board dimensions
            int rows = originalService.getGameModel().getNumberOfRows();
            int cols = originalService.getGameModel().getNumberOfColumns();
            
            // Create a list of available positions
            List<int[]> availablePositions = new ArrayList<>();
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    availablePositions.add(new int[]{i, j});
                }
            }
            
            // Simulate random play
            while (!availablePositions.isEmpty()) {
                // Break if we've collected enough samples
                if (originalService.getCheckForWinnerTimes().size() >= sampleSize && 
                    optimizedService.getCheckForWinnerTimes().size() >= sampleSize) {
                    break;
                }
                
                // Select a random position
                int posIndex = random.nextInt(availablePositions.size());
                int[] position = availablePositions.remove(posIndex);
                int row = position[0];
                int col = position[1];
                
                // Create move command (e.g., "a1", "b2")
                String command = (char)('a' + row) + String.valueOf(col + 1);
                
                try {
                    // Make identical moves on both services
                    originalService.handleIncomingCommand(command);
                    optimizedService.handleIncomingCommand(command);
                    
                    // Check if game has ended
                    if (originalService.getGameModel().getWinner() != null || 
                        originalService.getGameModel().isGameDrawn()) {
                        break;
                    }
                } catch (MoveException e) {
                    // This shouldn't happen with our random selection logic, but just in case
                    System.err.println("Error making move: " + e.getMessage());
                }
            }
            
            if (gamesPlayed % 10 == 0) {
                System.out.println("Games played: " + gamesPlayed + 
                                  ", Samples collected: " + originalService.getCheckForWinnerTimes().size());
            }
        }
        
        System.out.println("Benchmark completed. Games played: " + gamesPlayed);
        
        // Get timing results (limit to requested sample size)
        List<Long> originalTimes = originalService.getCheckForWinnerTimes().subList(0, sampleSize);
        List<Long> optimizedTimes = optimizedService.getCheckForWinnerTimes().subList(0, sampleSize);
        
        // Calculate statistics
        results.put("original", calculateStats(originalTimes));
        results.put("optimized", calculateStats(optimizedTimes));
        
        // Calculate improvement percentage
        Map<String, Object> originalStats = (Map<String, Object>)results.get("original");
        Map<String, Object> optimizedStats = (Map<String, Object>)results.get("optimized");
        
        double originalAvg = (double)originalStats.get("average");
        double optimizedAvg = (double)optimizedStats.get("average");
        double improvement = 100 * (originalAvg - optimizedAvg) / originalAvg;
        
        results.put("improvement", improvement);
        results.put("sampleSize", sampleSize);
        results.put("boardSize", "19x19");
        results.put("gamesPlayed", gamesPlayed);
        
        return results;
    }
    
    private static Map<String, Object> calculateStats(List<Long> times) {
        Map<String, Object> stats = new HashMap<>();
        
        // Make a copy and sort for percentile calculations
        List<Long> sortedTimes = new ArrayList<>(times);
        Collections.sort(sortedTimes);
        
        // Calculate average (mean)
        double average = times.stream()
                             .mapToLong(Long::longValue)
                             .average()
                             .orElse(0);
        
        // Calculate median (50th percentile)
        double median = sortedTimes.get(sortedTimes.size() / 2);
        
        // Calculate 95th percentile
        int p95Index = (int)Math.ceil(0.95 * sortedTimes.size()) - 1;
        double p95 = sortedTimes.get(p95Index);
        
        // Calculate min and max
        long min = sortedTimes.get(0);
        long max = sortedTimes.get(sortedTimes.size() - 1);
        
        stats.put("average", average);
        stats.put("median", median);
        stats.put("p95", p95);
        stats.put("min", min);
        stats.put("max", max);
        stats.put("samples", times.size());
        
        return stats;
    }
}