# Tic-Tac-Toe Game

## 🌐 Play Online

Dive straight into the action without any setup! Play the game online at: [https://tic-tac-toe-1-fr16.onrender.com](https://tic-tac-toe-1-fr16.onrender.com)
(Please bear with the wait before the start since it's deployed on a free server ! )

## 🔧 Tech Stack

- **Frontend**: Vue 3 + Vite + JavaScript
- **Backend**: Spring Boot (Java)
- **Styling**: CSS (with optional Tailwind CSS)
- **Deployment**: Docker

## 🎮 Game Features

- **Dynamic Gameplay**: Supports both single-player and multiplayer modes.
- **Customizable Board**: Adjust the grid size to your preference for varied challenges.
- **Real-time Updates**: Instant feedback on moves ensures an engaging gameplay experience.

## 🕹️ How to Play

1. **Set Up Players**:
   - Enter the desired number of players.
   - Click on the "Set Players" button to confirm.
2. **Configure the Board**:
   - Input the number of rows and columns for the game board.
   - Click on the "Set Board Size" button to apply your settings.
3. **Start Playing**:
   - Players take turns clicking on the grid cells to place their marks.
   - The first player to align their marks horizontally, vertically, or diagonally wins the game.
   - If all cells are filled without a winning combination, the game is declared a draw.

## 🛠️ Local Deployment

For those interested in running the game locally, follow these steps:

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/YourUsername/tic-tac-toe.git
   cd tic-tac-toe
   ```
2. **Set Up**:
   - Ensure you have Java and Maven installed.
   - Navigate to the backend directory:
     ```bash
     cd backend
     ```
   - Build and run the Spring Boot application:
     ```bash
     mvn clean install
     mvn spring-boot:run
     ```
   - The server will start at `http://localhost:8080`.
   - Ensure you have Node.js and npm installed.
   - Navigate to the frontend directory:
     ```bash
     cd ../frontend
     ```
   - Install dependencies:
     ```bash
     npm install
     ```
   - Start the development server:
     ```bash
     npm run dev
     ```
   - Access the game at `http://localhost:5173`.

4. **Docker Deployment** (Optional):
   - If you prefer using Docker, ensure Docker is installed on your system.
   - Build and run the Docker containers:
     ```bash
     docker-compose up --build
     ```
   - The application will be accessible at `http://localhost:8080`.
