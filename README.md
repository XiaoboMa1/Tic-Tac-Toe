# RESTful Tic-Tac-Toe: A High-Performance Game Engine

A full-stack Tic-Tac-Toe application featuring algorithmic improvements, stateful caching, and a real-time dashboard to visualize the results.

## 🌐 Play Online

[**Play Here**](https://tic-tac-toe-1-fr16.onrender.com)

Just migrated from Azure to Render for cost-efficiency, so please bear with the wait before the start since it's now running on a free server!

## 🔧 Tech Stack

- Backend: Java 17, Spring Boot 3, Maven
- Frontend: Vue 3, JavaScript
- Deployment: Docker, <del>Azure App Service</del> Render

## 🎮 Features

- Dynamic Board: Supports adjustable board sizes (3x3 to 9x9) and player counts.
- O(k) Win-Detection: Optimized win-checking logic from a naive O(n²) board scan to a targeted O(k) directional check from the last move.
- In-Memory Caching: A lightweight, stateful cache using a dirty-checking mechanism reduces redundant state calculations.
- Live Performance Dashboard: Visualizes real-time backend metrics.
- On-Demand Benchmarking: Triggers a live backend test to compare the performance of the original vs. optimized algorithms, displaying the results directly in the UI.

## 🕹️ How to Play

1.  **Game Setup**: Use the input fields to set the number of players and the board size.
2.  **Gameplay**: Click on any empty cell to make a move.
3.  **Reset**: Click "Clear Board" to start a new game.

### Performance Monitoring Panel

This dashboard provides a transparent look into the backend's performance. All metrics are generated and served by the Spring Boot application.

- API Call Statistics:
    - How it works: The backend `PerformanceStats.java` component intercepts each API call in `GameController.java`. It uses `System.currentTimeMillis()` to time the execution of each endpoint and aggregates the data in a `ConcurrentHashMap`.
    - Metrics:
        *   `Call Count`: Total hits for each endpoint.
        *   `Avg. Response (ms)`: Average execution time.
        *   `Max Response (ms)`: The highest execution time recorded.

- Server Uptime:
    - How it works: `PerformanceStats.java` records the application's start time and calculates the duration to the current time.

- Cache Status:
    - How it works: The `CachedGameService.java` uses an `AtomicBoolean` as a dirty flag. A **cache hit** occurs when a game state is requested and the flag is clean. A **cache miss** occurs if the state has been modified (e.g., after a move), forcing re-computation.
    - Metrics:
        *   `Cache Hit Rate`: The percentage of requests served directly from the cache.
        *   `Cache Hits / Misses`: The raw count for each outcome.

- Run Performance Test:
    - How it works: This button triggers the `/benchmark` endpoint. The backend's `PerformanceBenchmark.java` then simulates game sessions, executing moves using both the unoptimized O(n²) algorithm (`GameService`) and the optimized O(k) algorithm (`OptimizedGameService`, k=win-threshold). It measures the average execution time in nanoseconds for each.
    - Metrics:
        *   `Original Algorithm (ns)`: Average execution time for the slow, full-board scan.
        *   `Optimized Algorithm (ns)`: Average execution time for the fast, directional check.
        *   `Performance Improvement`: The percentage reduction in execution time.

## 🛠️ Local Deployment

### Backend (Port 8080)

```bash
# Navigate to the project root (contains pom.xml)
mvn spring-boot:run
```

**Docker Deployment** (Optional): runs the backend in a Docker container.

```bash
# 1. Build the Docker image from the backend project root
docker build -t oxo-game .

# 2. Run the container
docker run -p 8080:8080 oxo-game
```

### Frontend (Port 5173)

```bash
# Navigate to the frontend project root (contains package.json)
npm install
npm run dev
```

Open your browser to `http://localhost:5173`. 
