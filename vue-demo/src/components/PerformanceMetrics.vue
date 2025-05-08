<template>
  <div class="performance-metrics">
    <h2>Performance Monitoring</h2>
    
    <Transition name="fade" mode="out-in">
      <div v-if="loading" class="loading-metrics">Loading data...</div>
      
      <div v-else-if="performanceData" class="metrics-content">
        <TransitionGroup tag="div" name="list-item" class="cards-container">
          <div class="metric-card" key="api-stats">
            <h3>API Call Statistics</h3>
            <table>
              <thead>
                <tr>
                  <th>API Endpoint</th>
                  <th>Call Count</th>
                  <th>Avg. Response (ms)</th>
                  <th>Max Response (ms)</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(stat, name) in performanceData.apiCalls" :key="name">
                  <td>{{ name }}</td>
                  <td>{{ stat.callCount }}</td>
                  <td>{{ stat.avgTimeMs.toFixed(2) }}</td>
                  <td>{{ stat.maxTimeMs }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          
          <div class="metric-card" key="uptime">
            <h3>Server Uptime</h3>
            <p>{{ formatUptime(performanceData.uptime) }}</p>
          </div>
          
          <div v-if="performanceData.cacheStats" class="metric-card" key="cache-stats">
            <h3>Cache Status</h3>
            <p>Cache Hit Rate: {{ (performanceData.cacheStats.hitRate * 100).toFixed(2) }}%</p>
            <p>Cache Hits: {{ performanceData.cacheStats.hits }}</p>
            <p>Cache Misses: {{ performanceData.cacheStats.misses }}</p>
          </div>
        </TransitionGroup>

        <button @click="runBenchmark" :disabled="benchmarking" class="benchmark-button animated-button">
          {{ benchmarking ? 'Testing...' : 'Run Performance Test' }}
        </button>
        
        <Transition name="fade">
          <div v-if="benchmarkResults" class="benchmark-results metric-card" key="benchmark-res">
            <h3>Another Random Test</h3>
            <div class="result-summary">
              Algorithm Performance Improvement: 
              <span class="highlight">
                {{ calculateAverageImprovement().toFixed(2) }}%
              </span>
            </div>
            <table>
              <thead>
                <tr>
                  <th>Board Size</th>
                  <th>Original Algorithm (ns)</th>
                  <th>Optimized Algorithm (ns)</th>
                  <th>Performance Improvement</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(_, size) in benchmarkResults.original" :key="size">
                  <td>{{ size }}</td>
                  <td>{{ benchmarkResults.original[size].toLocaleString() }}</td>
                  <td>{{ benchmarkResults.optimized[size].toLocaleString() }}</td>
                  <td class="improvement">{{ benchmarkResults.improvements[size].toFixed(2) }}%</td>
                </tr>
              </tbody>
            </table>
          </div>
        </Transition>
      </div>
      <div v-else class="loading-metrics" key="no-data">No performance data available.</div>
    </Transition>
  </div>
</template>

<script>
export default {
  data() {
    return {
      performanceData: null,
      benchmarkResults: null,
      loading: true,
      benchmarking: false
    }
  },
  
  mounted() {
    this.fetchPerformanceData();
    this.intervalId = setInterval(this.fetchPerformanceData, 30000); // Store interval ID
  },

  beforeUnmount() {
    clearInterval(this.intervalId); // Clear interval on component unmount
  },
  
  methods: {
    async fetchPerformanceData() {
      this.loading = true; // Ensure loading is true at the start
      try {
        const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/performance`);
        if (!response.ok) {
          throw new Error(`HTTP error: ${response.status}`);
        }
        this.performanceData = await response.json();
      } catch (error) {
        console.error('Failed to fetch performance data:', error);
        this.performanceData = null; // Set to null on error to show "No data" message
      } finally {
        this.loading = false;
      }
    },
    
    async runBenchmark() {
      if (this.benchmarking) return;
      try {
        this.benchmarking = true;
        this.benchmarkResults = null; // Clear previous results
        const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/benchmark?iterations=20`);
        if (!response.ok) {
          throw new Error(`HTTP error: ${response.status}`);
        }
        this.benchmarkResults = await response.json();
        this.$nextTick(() => { // Ensure DOM is updated before scrolling
          document.querySelector('.benchmark-results')?.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        });
      } catch (error) {
        console.error('Performance test failed:', error);
        alert('Performance test failed. Please check console logs.');
      } finally {
        this.benchmarking = false;
      }
    },
    
    formatUptime(ms) {
      if (typeof ms !== 'number' || ms < 0) return 'N/A';
      const totalSeconds = Math.floor(ms / 1000);
      const hours = Math.floor(totalSeconds / 3600);
      const minutes = Math.floor((totalSeconds % 3600) / 60);
      const seconds = totalSeconds % 60;
      
      return `${hours}h ${minutes}m ${seconds}s`; // English format
    },
    
    calculateAverageImprovement() {
      if (!this.benchmarkResults || !this.benchmarkResults.improvements) {
        return 0;
      }
      const improvements = Object.values(this.benchmarkResults.improvements);
      if (improvements.length === 0) return 0;
      return improvements.reduce((sum, value) => sum + value, 0) / improvements.length;
    }
  }
}
</script>

<style scoped> /* Changed to scoped to avoid conflicts if this component is reused */
.performance-metrics {
  margin-top: 2.5rem; /* Increased margin */
  padding: 1.5rem; /* Increased padding */
  background: rgba(0, 0, 0, 0.6); /* Darker background for better contrast */
  border-radius: 12px; /* More rounded */
  width: 100%;
  max-width: 800px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.3); /* Softer shadow */
}

.performance-metrics h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #ffd700; /* Gold color for headings */
  font-size: 1.5rem;
}

.performance-metrics h3 {
  color: #f0e68c; /* Khaki for subheadings */
  margin-bottom: 1rem;
  border-bottom: 1px solid rgba(255,215,0,0.3);
  padding-bottom: 0.5rem;
}

.cards-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem; /* Gap between cards */
}

.metric-card {
  margin-bottom: 1rem; /* Kept for fallback if not in flex/grid */
  padding: 1.2rem; /* Increased padding */
  background: rgba(50, 30, 10, 0.8); /* Slightly more vibrant */
  border-radius: 8px; /* More rounded */
  border: 1px solid rgba(201, 167, 105, 0.4); /* Subtle border */
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.metric-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 15px rgba(0,0,0,0.2);
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 0.5rem;
}

th, td {
  padding: 0.6rem 0.8rem; /* Increased padding */
  text-align: left;
  border-bottom: 1px solid rgba(255, 255, 255, 0.25); /* Lighter border */
}

th {
  color: #ffd700; /* Gold for table headers */
  font-weight: 600; /* Bolder */
}

.benchmark-button {
  display: block; /* Make it block to center with margin */
  margin: 1.5rem auto; /* More margin and auto for centering */
  padding: 0.7rem 1.5rem; /* Increased padding */
  /* General styling inherited or from App.vue if .animated-button is global */
  min-width: 220px; /* Wider button */
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background: linear-gradient(to bottom, #555, #333) !important; /* Darker disabled state */
}

.benchmark-results {
  margin-top: 1.5rem;
  /* animation: fadeIn 0.5s ease-in-out; (Replaced by Transition) */
}

.result-summary {
  font-size: 1.15rem; /* Slightly larger */
  margin-bottom: 1.2rem;
  text-align: center;
  padding: 0.5rem;
  background-color: rgba(255,215,0,0.1);
  border-radius: 4px;
}

.highlight {
  color: #ffd700;
  font-weight: bold;
  font-size: 1.25rem; /* Slightly larger */
}

.improvement {
  color: #66bb6a; /* Softer green */
  font-weight: bold;
}

.loading-metrics { /* Renamed from .loading to avoid conflict */
  text-align: center;
  padding: 2rem; /* More padding */
  font-size: 1.2rem;
  font-style: italic;
  color: #ccc;
}

/* Vue Transition Classes for PerformanceMetrics */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.4s ease-in-out;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.list-item-enter-active,
.list-item-leave-active {
  transition: all 0.5s ease;
}
.list-item-enter-from,
.list-item-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
/* Ensure leave transitions are applied correctly for lists */
.list-item-move {
  transition: transform 0.5s ease;
}


/* Responsive adjustments (existing styles maintained, can be adjusted) */
@media (max-width: 800px) {
  .performance-metrics { font-size: 0.9rem; padding: 1rem; }
  table { font-size: 0.85rem; } /* Slightly larger base for tables */
  th, td { padding: 0.5rem 0.6rem; } /* Adjusted padding */
  .performance-metrics h2 { font-size: 1.3rem; }
}

@media (max-width: 500px) {
  .performance-metrics { font-size: 0.85rem; }
  table { font-size: 0.75rem; display: block; overflow-x: auto; } /* Allow table horizontal scroll */
  .performance-metrics h2 { font-size: 1.2rem; }
  .metric-card { padding: 1rem; }
  th, td { padding: 0.4rem 0.5rem; white-space: nowrap; } /* Prevent text wrapping in cells */
}
</style>