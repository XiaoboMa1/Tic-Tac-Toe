<template>
  <div class="container">
    <header class="header">
      <h1>OXO Game</h1>
      <a href="https://github.com/XiaoboMa1" target="_blank" rel="noopener noreferrer" class="github-link">
        Visit GitHub
      </a>
    </header>

    <Transition name="fade">
      <div v-if="!gameState" class="loading">
        Loading game state...
      </div>
    </Transition>

    <div v-if="gameState" class="main-content">
      <Transition name="slide-fade-down">
        <div v-if="gameState.winner || gameState.drawn" class="game-result">
          <div v-if="gameState.winner" class="winner-message">
            <div class="trophy-icon">🏆</div>
            <h2>Player {{ gameState.winner }} Wins!</h2>
            <button @click="resetBoard" class="play-again-btn animated-button">Play Again</button>
          </div>
          <div v-else-if="gameState.drawn" class="draw-message">
            <div class="draw-icon">🤝</div>
            <h2>It's a Draw!</h2>
            <button @click="resetBoard" class="play-again-btn animated-button">Play Again</button>
          </div>
        </div>
      </Transition>

      <div class="info-row">
        <span>Players: {{ gameState.playerCount }}</span>
        <span>Current Player: <span class="current-player">{{ gameState.currentPlayer || 'None' }}</span></span>
        <span>Winner: {{ gameState.winner || 'None' }}</span>
        <span>Draw: {{ gameState.drawn ? 'Yes' : 'No' }}</span>
        <span>Board: {{ gameState.rows }} x {{ gameState.cols }}</span>
        <span>Win Threshold: {{ gameState.winThreshold }}</span>
      </div>

      <div class="row input-group">
        <input v-model="playerCountInput" type="number" min="2" max="9" placeholder="Number of Players" class="input-field" />
        <button @click="setPlayers" class="animated-button">Set Players</button>
      </div>

      <div class="row input-group">
        <input v-model="rowsInput" type="number" min="3" max="9" placeholder="Rows" class="input-field" />
        <input v-model="colsInput" type="number" min="3" max="9" placeholder="Columns" class="input-field" />
        <button @click="setBoardSize" class="animated-button">Set Board Size</button>
      </div>

      <Transition name="board-fade" appear>
        <div class="board" :style="{ gridTemplateRows: `repeat(${gameState.rows}, 2.2rem)`, gridTemplateColumns: `repeat(${gameState.cols}, 2.2rem)`, opacity: gameState.winner || gameState.drawn ? 0.7 : 1, }" v-if="gameState.board">
          <div v-for="(rowArr, rIdx) in gameState.board" :key="rIdx" style="display: contents;">
            <div v-for="(cell, cIdx) in rowArr" :key="rIdx + '-' + cIdx" class="cell" :class="{ 'winner-cell': isWinningCell(rIdx, cIdx), 'occupied': cell !== ' ' }" @click="clickCell(rIdx, cIdx)">
              {{ cell === ' ' ? '' : cell }}
            </div>
          </div>
        </div>
      </Transition>

      <div class="row">
        <button @click="resetBoard" class="clear-btn animated-button">Clear Board</button>
      </div>
      
      <!-- [FIX #1] Add a ref to the component -->
      <performance-metrics ref="performanceMetricsRef" />
    </div>
  </div>
</template>

<script setup> // [FIX #2] Changed to script setup
import { ref, reactive, onMounted } from 'vue';
import PerformanceMetrics from './components/PerformanceMetrics.vue';

const apiBase = import.meta.env.VITE_API_BASE_URL;
const gameState = ref(null);
const playerCountInput = ref('');
const rowsInput = ref('');
const colsInput = ref('');
const winningCells = reactive([]);

// [FIX #3] Create a ref to hold the PerformanceMetrics component instance
const performanceMetricsRef = ref(null);

const isWinningCell = (row, col) => {
  if (!gameState.value || !gameState.value.winner) return false;
  return winningCells.some(cell => cell.row === row && cell.col === col);
};

const calculateWinningPath = () => {
  if (!gameState.value || !gameState.value.winner) {
    winningCells.length = 0;
    return;
  }
  const { board, rows, cols, winThreshold, winner: winnerLetter } = gameState.value;
  winningCells.length = 0;
  for (let r = 0; r < rows; r++) { for (let c = 0; c <= cols - winThreshold; c++) { let allMatch = true; for (let i = 0; i < winThreshold; i++) { if (board[r][c + i] !== winnerLetter) { allMatch = false; break; } } if (allMatch) { for (let i = 0; i < winThreshold; i++) winningCells.push({ row: r, col: c + i }); return; } } }
  for (let c = 0; c < cols; c++) { for (let r = 0; r <= rows - winThreshold; r++) { let allMatch = true; for (let i = 0; i < winThreshold; i++) { if (board[r + i][c] !== winnerLetter) { allMatch = false; break; } } if (allMatch) { for (let i = 0; i < winThreshold; i++) winningCells.push({ row: r + i, col: c }); return; } } }
  for (let r = 0; r <= rows - winThreshold; r++) { for (let c = 0; c <= cols - winThreshold; c++) { let allMatch = true; for (let i = 0; i < winThreshold; i++) { if (board[r + i][c + i] !== winnerLetter) { allMatch = false; break; } } if (allMatch) { for (let i = 0; i < winThreshold; i++) winningCells.push({ row: r + i, col: c + i }); return; } } }
  for (let r = 0; r <= rows - winThreshold; r++) { for (let c = winThreshold - 1; c < cols; c++) { let allMatch = true; for (let i = 0; i < winThreshold; i++) { if (board[r + i][c - i] !== winnerLetter) { allMatch = false; break; } } if (allMatch) { for (let i = 0; i < winThreshold; i++) winningCells.push({ row: r + i, col: c - i }); return; } } }
};

async function fetchGameState() {
  try {
    const res = await fetch(`${apiBase}/state`);
    gameState.value = await res.json();
    calculateWinningPath();
  } catch (err) {
    console.error('Failed to fetch game state:', err);
  }
}

async function resetBoard() {
  try {
    const res = await fetch(`${apiBase}/reset`, { method: 'POST' });
    gameState.value = await res.json();
    winningCells.length = 0;
    // [FIX #4] Trigger metrics refresh
    performanceMetricsRef.value?.fetchPerformanceData();
  } catch (err) {
    console.error('Failed to reset board:', err);
  }
}

async function setPlayers() {
  if (!playerCountInput.value) return;
  try {
    const count = parseInt(playerCountInput.value);
    if (isNaN(count) || count < 2 || count > 9) { alert('Please enter a player count between 2 and 9.'); return; }
    const res = await fetch(`${apiBase}/setPlayers?count=${count}`, { method: 'POST' });
    gameState.value = await res.json();
    playerCountInput.value = '';
    // [FIX #4] Trigger metrics refresh
    performanceMetricsRef.value?.fetchPerformanceData();
  } catch (err) {
    console.error('Failed to set players:', err);
  }
}

async function setBoardSize() {
  if (!rowsInput.value || !colsInput.value) return;
  try {
    const rows = parseInt(rowsInput.value);
    const cols = parseInt(colsInput.value);
    if (isNaN(rows) || rows < 3 || rows > 9 || isNaN(cols) || cols < 3 || cols > 9) { alert('Rows and columns must be between 3 and 9.'); return; }
    const res = await fetch(`${apiBase}/setSize?rows=${rows}&cols=${cols}`, { method: 'POST' });
    gameState.value = await res.json();
    rowsInput.value = '';
    colsInput.value = '';
    // [FIX #4] Trigger metrics refresh
    performanceMetricsRef.value?.fetchPerformanceData();
  } catch (err) {
    console.error('Failed to set board size:', err);
  }
}

async function clickCell(rIdx, cIdx) {
  if (gameState.value.winner || gameState.value.drawn) return;
  try {
    if (gameState.value.board[rIdx][cIdx] !== ' ') { alert("This cell is already occupied. Please choose an empty cell!"); return; }
    const command = `${String.fromCharCode('a'.charCodeAt(0) + rIdx)}${cIdx + 1}`;
    const res = await fetch(`${apiBase}/move`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ command }) });
    if (!res.ok) { const errorData = await res.json(); alert(`Error: ${errorData.error}`); return; }
    gameState.value = await res.json();
    if (gameState.value.winner) calculateWinningPath();
    // [FIX #4] Trigger metrics refresh
    performanceMetricsRef.value?.fetchPerformanceData();
  } catch (err) {
    console.error('Operation failed:', err);
    alert('Operation failed. Please check console logs.');
  }
}

onMounted(async () => {
  try {
    await fetch(`${apiBase}/setPlayers?count=2`, { method: 'POST' });
    await fetchGameState();
    // [FIX #4] Trigger metrics refresh after initial setup
    performanceMetricsRef.value?.fetchPerformanceData();
  } catch (err) {
    console.error(err);
  }
});
</script>

<style>
/* Styles remain the same */
.container { position: relative; max-width: 100%; width: 800px; margin: 0 auto; padding: 2rem; overflow-y: auto; min-height: 100vh; }
.container, .container * { color: white !important; font-weight: bold !important; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; padding-bottom: 1rem; border-bottom: 1px solid rgba(255, 255, 255, 0.2); }
.header h1 { font-size: 1.8rem; margin: 0; color: #ffd700 !important; text-shadow: 1px 1px 3px rgba(0,0,0,0.5); }
.github-link { text-decoration: none; padding: 0.6rem 1.2rem; border-radius: 6px; background: rgba(0, 0, 0, 0.4); transition: background-color 0.3s ease, transform 0.2s ease; }
.github-link:hover { background: rgba(0, 0, 0, 0.6); transform: translateY(-2px); }
.main-content { display: flex; flex-direction: column; align-items: center; gap: 1.5rem; }
.info-row { display: flex; flex-wrap: wrap; gap: 1.2rem; margin-bottom: 1rem; justify-content: center; padding: 0.8rem; background: rgba(0,0,0,0.2); border-radius: 8px; width: 100%; box-sizing: border-box; }
.info-row span { font-size: 0.95rem; }
.current-player { color: gold !important; font-size: 1.1em; animation: blinkPlayer 1.5s infinite alternate; }
@keyframes blinkPlayer { from { opacity: 0.7; } to { opacity: 1; } }
.input-group { display: flex; flex-wrap: wrap; gap: 1rem; margin-bottom: 1rem; justify-content: center; align-items: center; padding: 1rem; background: rgba(0,0,0,0.15); border-radius: 8px; width: 100%; max-width: 600px; box-sizing: border-box; }
.input-field { padding: 0.5rem 0.8rem; width: auto; min-width: 120px; background: rgba(255, 255, 255, 0.15); border: 1px solid #c9a769; border-radius: 4px; color: white !important; transition: border-color 0.3s ease, box-shadow 0.3s ease; }
.input-field:focus { border-color: #ffd700; box-shadow: 0 0 8px rgba(255,215,0,0.5); }
.animated-button { transition: transform 0.2s ease-out, box-shadow 0.2s ease-out !important; }
.animated-button:hover { transform: translateY(-2px) scale(1.02) !important; box-shadow: 0 4px 8px rgba(0,0,0,0.3) !important; }
.animated-button:active { transform: translateY(0px) scale(0.98) !important; }
.board { display: grid; margin: 1rem 0; transition: opacity 0.5s ease-in-out; border: 2px solid rgba(201, 167, 105, 0.5); padding: 0.5rem; background-color: rgba(0,0,0,0.2); border-radius: 8px; }
.cell { display: flex; align-items: center; justify-content: center; cursor: pointer; width: 2.2rem; height: 2.2rem; font-size: 1.4rem; transition: all 0.2s ease, transform 0.1s linear; border-radius: 4px; }
.cell:not(.occupied):hover { background-color: rgba(255, 255, 255, 0.15) !important; transform: scale(1.05); }
.cell.occupied { cursor: not-allowed; opacity: 0.8; }
.winner-cell { background: rgba(255, 215, 0, 0.4) !important; box-shadow: 0 0 12px gold, inset 0 0 5px rgba(255,255,255,0.3) !important; animation: pulseWinner 1.2s infinite; transform: scale(1.05); }
@keyframes pulseWinner { 0%, 100% { transform: scale(1.05); box-shadow: 0 0 12px gold, inset 0 0 5px rgba(255,255,255,0.3); } 50% { transform: scale(1.15); box-shadow: 0 0 20px #ffec80, inset 0 0 8px rgba(255,255,255,0.5); } }
.clear-btn { margin-top: 1rem; }
.loading { color: white; font-size: 1.3rem; text-align: center; padding: 2rem; font-style: italic; }
.game-result { position: relative; width: 100%; max-width: 450px; padding: 1.5rem; margin-bottom: 1.5rem; text-align: center; border-radius: 12px; backdrop-filter: blur(5px); }
.winner-message { background: rgba(40, 130, 40, 0.8); padding: 1.5rem; border-radius: 10px; box-shadow: 0 4px 15px rgba(0,0,0,0.3); }
.draw-message { background: rgba(80, 80, 130, 0.8); padding: 1.5rem; border-radius: 10px; box-shadow: 0 4px 15px rgba(0,0,0,0.3); }
.trophy-icon, .draw-icon { font-size: 3.5rem; margin-bottom: 0.8rem; animation: iconPop 0.5s ease-out 0.2s backwards; }
@keyframes iconPop { 0% { transform: scale(0.5); opacity: 0; } 80% { transform: scale(1.1); } 100% { transform: scale(1); opacity: 1; } }
.game-result h2 { margin-bottom: 1rem; font-size: 1.6rem; }
.play-again-btn { margin-top: 1rem; background: rgba(255, 255, 255, 0.3) !important; padding: 0.7rem 1.8rem; font-size: 1.2rem; border: 1px solid rgba(255,255,255,0.5) !important; border-radius: 6px; }
.fade-enter-active, .fade-leave-active { transition: opacity 0.5s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.slide-fade-down-enter-active { transition: all 0.4s ease-out; }
.slide-fade-down-leave-active { transition: all 0.4s cubic-bezier(1, 0.5, 0.8, 1); }
.slide-fade-down-enter-from, .slide-fade-down-leave-to { transform: translateY(-30px); opacity: 0; }
.board-fade-enter-active, .board-fade-leave-active { transition: opacity 0.6s ease-in-out, transform 0.6s ease-in-out; }
.board-fade-enter-from, .board-fade-leave-to { opacity: 0; transform: scale(0.95) translateY(10px); }
.board-fade-enter-to, .board-fade-leave-from { opacity: 1; transform: scale(1) translateY(0); }
@media (max-width: 850px) { .container { width: 95%; padding: 1.5rem; } .header { flex-direction: column; gap: 1.5rem; } .info-row { font-size: 0.9rem; gap: 0.8rem; padding: 0.6rem; } .board { transform: scale(0.9); transform-origin: center; } .header h1 { font-size: 1.6rem; } .input-group { padding: 0.8rem; } }
@media (max-width: 500px) { .header h1 { font-size: 1.4rem; } .info-row { font-size: 0.85rem; flex-direction: column; align-items: flex-start;} .board { transform: scale(0.85); } .input-field { width: 100%; min-width: unset; margin-bottom: 0.5rem; } .input-group { flex-direction: column; } .input-group button { width: 100%; } .cell { width: 2rem; height: 2rem; font-size: 1.2rem;} .game-result { padding: 1rem; } .game-result h2 { font-size: 1.4rem; } .play-again-btn { padding: 0.6rem 1.5rem; font-size: 1.1rem; } }
</style>