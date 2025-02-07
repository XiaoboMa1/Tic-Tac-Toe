# Tic-Tac-Toe Game with Vue & Spring Boot

Tired of the same old 3×3 Tic-Tac-Toe? This OXO project takes the classic game to the next level! Customize the number of players, set a larger board size—go for a 5×5 or even 7×7 grid for a more exciting challenge. Built with Vue 3 + JavaScript on the frontend and Spring Boot on the backend, this game features a sleek interface and smooth gameplay, offering a more flexible and creative Tic-Tac-Toe experience.  

### **Tech Stack**  
- **Frontend**: Vue 3 + Vite + JavaScript  
- **Backend**: Spring Boot (Java)  
- **Styling**: CSS (You can also use Tailwind, but for this project it is unnecessary)  
- **Build Tools**: Vite (Frontend), Maven/Gradle (Backend)  

### **Key Features**  
- **Customizable Player Count**: Add multiple players, each assigned a unique symbol.  
- **Flexible Board Size**: No more 3×3 limitations—play on 4×4, 5×5, or even larger grids.  
- **Simple Click-to-Place Mechanism**: Click any empty cell to mark your move, and the system automatically switches to the next player.  
- **Win/Draw Detection**: Automatically checks for a winning sequence or detects a draw if the board is full.  
- **Minimalist UI**: Centered layout with three built-in themes (customizable), plus a "Go to GitHub" button for easy access.  

### **Getting Started**  

#### **1. Backend Setup**  
Clone the backend repository:  
```bash
git clone https://github.com/YourName/oxo-backend.git
```
Build and run the Spring Boot application using Maven or Gradle:  
```bash
# Using Maven
mvn clean spring-boot:run
# Runs on http://localhost:8080 by default
```

#### **2. Frontend Setup**  
Clone the frontend repository:  
```bash
git clone https://github.com/YourName/oxo-frontend-vue.git
```
Navigate to the frontend directory, install dependencies, and start the development server:  
```bash
npm install
npm run dev
```
Open `http://localhost:5173` in your browser to access the game.  

**Note**: If using Vite proxy, ensure the backend CORS settings or proxy configuration are properly set up.  

### **How to Play**  
1. **Set Player Count**: Enter the desired number of players (e.g., 2, 3, 4...) in the input field and click "Set Players."  
2. **Set Board Size**: Define the number of rows and columns (e.g., 5×5) and click "Set Board Size" to expand the grid.  
3. **Make a Move**: The current player (indicated on the UI) clicks an empty cell to place their mark.  
4. **Winning Conditions**: A player wins when they achieve the required number of aligned marks (default: 3). If the board is full without a winner, it's a draw.  
5. **Restart the Game**: Click "Reset" or refresh the page to start a new game.  

### **Why This Project?**  
- **Highly Customizable**: Play on grids from 3×3 up to 9×9, with multiple players for a more competitive experience.  
- **Easy to Install & Use**: Built with a modern frontend-backend separation using Vue 3 and Spring Boot, making setup and deployment simple.  

Get ready for a fresh and exciting twist on Tic-Tac-Toe! 🚀
