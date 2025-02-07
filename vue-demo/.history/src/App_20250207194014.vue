<template>
  <div class="container">
    <header class="header">
      <h1>oxo created by XiaoboMa (Vue)</h1>
      <a
        href="https://github.com/XiaoboMa1"
        target="_blank"
        rel="noopener noreferrer"
        class="github-link"
      >
        Go to GitHub
      </a>
    </header>

    <!-- 如果游戏状态还没获取，则显示 Loading... -->
    <div v-if="!gameState" class="loading">
      Loading game state...
    </div>

    <div v-else class="main-content">
      <div class="info-row">
        <span>PlayerCount: {{ gameState.playerCount }}</span>
        <span>CurrentPlayer: {{ gameState.currentPlayer || 'N/A' }}</span>
        <span>Winner: {{ gameState.winner || 'None' }}</span>
        <span>Drawn: {{ gameState.drawn ? 'Yes' : 'No' }}</span>
        <span>Board: {{ gameState.rows }} x {{ gameState.cols }}</span>
        <span>WinThreshold: {{ gameState.winThreshold }}</span>
      </div>

      <!-- 设置玩家数量 -->
      <div class="row">
        <input
          v-model="playerCountInput"
          type="number"
          placeholder="player numbers "
        />
        <button @click="setPlayers">Set Players</button>
      </div>

      <!-- 设置棋盘大小 -->
      <div class="row">
        <input v-model="rowsInput" type="number" placeholder="Rows" />
        <input v-model="colsInput" type="number" placeholder="Cols" />
        <button @click="setBoardSize">Set Board Size</button>
      </div>

      <!-- 棋盘 -->
      <div
        class="board"
        :style="{
          gridTemplateRows: `repeat(${gameState.rows}, 2rem)`,
          gridTemplateColumns: `repeat(${gameState.cols}, 2rem)`,
        }"
      >
        <!-- 注意这里用 display: contents 避免多余的嵌套层导致布局错乱 -->
        <div
          v-for="(rowArr, rIdx) in gameState.board"
          :key="rIdx"
          style="display: contents;"
        >
          <div
            v-for="(cell, cIdx) in rowArr"
            :key="rIdx + '-' + cIdx"
            class="cell"
            @click="clickCell(rIdx, cIdx)"
          >
            {{ cell === ' ' ? '' : cell }}
          </div>
        </div>
      </div>

      <!-- 按钮：Clear -->
      <div class="row">
        <button @click="resetBoard">Clear</button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'

export default {
  setup() {
    const apiBase = import.meta.env.VITE_API_BASE_URL;

    // 输入框数据
    const playerCountInput = ref('')
    const rowsInput = ref('')
    const colsInput = ref('')

    // （1）只刷新当前棋盘，不重置
    async function fetchGameState() {
      try {
        const res = await fetch(`${apiBase}/state`)
        const data = await res.json()
        gameState.value = data
      } catch (err) {
        console.error('fetchGameState error:', err)
      }
    }

    // （2）重置棋盘
    async function resetBoard() {
      try {
        const res = await fetch('${apiBase}/reset', {
          method: 'POST'
        })
        const data = await res.json()
        gameState.value = data
      } catch (err) {
        console.error('resetBoard error:', err)
      }
    }

    // 设置玩家数量
    async function setPlayers() {
      if (!playerCountInput.value) return
      try {
        const url = `${apiBase}/setPlayers?count=${playerCountInput.value}`
        const res = await fetch(url, { method: 'POST' })
        const data = await res.json()
        gameState.value = data
        playerCountInput.value = ''
      } catch (err) {
        console.error('setPlayers error:', err)
      }
    }

    // 设置棋盘大小
    async function setBoardSize() {
      if (!rowsInput.value || !colsInput.value) return
      try {
        const url = `${apiBase}/setSize?rows=${rowsInput.value}&cols=${colsInput.value}`
        const res = await fetch(url, { method: 'POST' })
        const data = await res.json()
        gameState.value = data

        // 清空输入框
        rowsInput.value = ''
        colsInput.value = ''
      } catch (err) {
        console.error('setBoardSize error:', err)
      }
    }

    // 点击棋盘格子落子
    async function clickCell(rIdx, cIdx) {
      try {
        // 后端接受的指令格式如 "a1", "b2"
        const rowLetter = String.fromCharCode('a'.charCodeAt(0) + rIdx)
        const colNumber = cIdx + 1
        const command = rowLetter + colNumber
        const res = await fetch('${apiBase}/move', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ command })
        })
        const data = await res.json()
        gameState.value = data
      } catch (err) {
        console.error('clickCell error:', err)
      }
    }

    // 初次载入组件就获取一次状态（不重置）
    onMounted(() => {
      fetchGameState()
    })

    return {
      gameState,
      playerCountInput,
      rowsInput,
      colsInput,
      fetchGameState,
      resetBoard,
      setPlayers,
      setBoardSize,
      clickCell
    }
  }
}
</script>

<style>
/* 让容器居中 */
.container {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  min-width: 800px;
  padding: 2rem;
  z-index: 1;
}

/* 让所有文本变白并加粗 */
.container, 
.container * {
  color: white !important;
  font-weight: bold !important;
}

/* 头部标题 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h1 {
  color: white !important;
  font-weight: bold !important;
  margin-bottom: 1rem;
}

/* GitHub 按钮 */
.github-link {
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
}

.main-content {
  display: flex;
  flex-direction: column;
  align-items: center; /* 让棋盘、输入框等内容居中 */
}

.info-row {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1rem;
}

.row {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
}

input {
  padding: 0.3rem;
  width: 6rem;
}

.board {
  display: grid;
  margin-bottom: 1rem;
}

.cell {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.loading {
  color: #333;
  font-size: 1.2rem;
  text-align: center;
}
</style>
