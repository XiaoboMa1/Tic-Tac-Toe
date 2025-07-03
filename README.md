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

## 🕹️ How to Play

1.  **Game Setup**: Use the input fields to set the number of players and the board size.
2.  **Gameplay**: Click on any empty cell to make a move.
3.  **Reset**: Click "Clear Board" to start a new game.

#### Performance Monitoring Panel

A transparent look into the backend's performance.

*   **Live API & Cache Metrics**: The top cards display live server statistics.
    *   **API Calls & Uptime**: These metrics update with every move.
    *   **Cache Status**: This shows the real-time state of the `CachedGameService`. Note that the panel updates only after game-altering actions (like "Clear Board") or by clicking "Update Performance Data", not during every move, to maintain a clean UI.

*   **On-Demand Performance Demonstration**: The "Run Performance Demonstration" button triggers a backend process to present the optimization impacts.
    *   **How it works**: This is not a live benchmark, but a controlled simulation using script to model the baseline performance. Simulation is necessary as live micro-benchmarks in free cloud environments are notoriously unreliable due to JVM JIT/GC and server load variations. A controlled simulation provides a **consistent, noise-free** demonstration of the underlying architectural improvements..

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
