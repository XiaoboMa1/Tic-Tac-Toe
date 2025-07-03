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
          {{ benchmarking ? 'Running Demonstration...' : 'Run Performance Demonstration' }}
        </button>
        
        <!-- [MODIFIED] New section to display structured benchmark results -->
        <Transition name="fade">
          <div v-if="benchmarkResults" class="benchmark-results-container">

            <!-- Algorithm Benchmark Card -->
            <div v-if="benchmarkResults.algorithmBenchmark" class="metric-card" key="algo-benchmark">
              <h3>Algorithm Performance (Win-Check)</h3>
              <div class="result-summary">
                Performance Improvement: 
                <span class="highlight">
                  {{ benchmarkResults.algorithmBenchmark.improvementPercent.toFixed(2) }}%
                </span>
              </div>
              <table>
                <tbody>
                  <tr>
                    <td>Board Size</td>
                    <td>{{ benchmarkResults.algorithmBenchmark.boardSize }}</td>
                  </tr>
                  <tr>
                    <td>Original Algorithm (avg ns)</td>
                    <td>{{ benchmarkResults.algorithmBenchmark.originalTimeNs.toLocaleString() }}</td>
                  </tr>
                  <tr>
                    <td>Optimized Algorithm (avg ns)</td>
                    <td>{{ benchmarkResults.algorithmBenchmark.optimizedTimeNs.toLocaleString() }}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Cache Benchmark Card -->
            <div v-if="benchmarkResults.cacheBenchmark" class="metric-card" key="cache-benchmark">
              <h3>Cache Performance (Read-Heavy Scenario)</h3>
              <div class="result-summary">
                Performance Improvement: 
                <span class="highlight">
                  {{ benchmarkResults.cacheBenchmark.improvementPercent.toFixed(2) }}%
                </span>
              </div>
              <table>
                <tbody>
                  <tr>
                    <td>Read Operations</td>
                    <td>{{ benchmarkResults.cacheBenchmark.readIterations.toLocaleString() }}</td>
                  </tr>
                  <tr>
                    <td>Without Cache (total ns)</td>
                    <td>{{ benchmarkResults.cacheBenchmark.noCacheTimeNs.toLocaleString() }}</td>
                  </tr>
                  <tr>
                    <td>With Cache (total ns)</td>
                    <td>{{ benchmarkResults.cacheBenchmark.withCacheTimeNs.toLocaleString() }}</td>
                  </tr>
                  <tr>
                    <td>Cache Hits / Misses</td>
                    <td>
                      {{ benchmarkResults.cacheBenchmark.cacheStats.hits }} / 
                      {{ benchmarkResults.cacheBenchmark.cacheStats.misses }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

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
      benchmarking: false,
      intervalId: null, // Initialize intervalId
    }
  },
  
  mounted() {
    this.fetchPerformanceData();
    this.intervalId = setInterval(this.fetchPerformanceData, 30000);
  },

  beforeUnmount() {
    clearInterval(this.intervalId);
  },
  
  methods: {
    async fetchPerformanceData() {
      // Set loading to true only if it's the initial load
      if (!this.performanceData) {
        this.loading = true;
      }
      try {
        const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/performance`);
        if (!response.ok) {
          throw new Error(`HTTP error: ${response.status}`);
        }
        this.performanceData = await response.json();
      } catch (error) {
        console.error('Failed to fetch performance data:', error);
        this.performanceData = null;
      } finally {
        this.loading = false;
      }
    },
    
    async runBenchmark() {
      if (this.benchmarking) return;
      try {
        this.benchmarking = true;
        this.benchmarkResults = null;
        const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/run-demonstration`);
        if (!response.ok) {
          throw new Error(`HTTP error: ${response.status}`);
        }
        this.benchmarkResults = await response.json();
      } catch (error) {
        console.error('Failed to run benchmark:', error);
        alert('Benchmark failed. Check console for details.');
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
      
      return `${hours}h ${minutes}m ${seconds}s`;
    },
  }
}
</script>

<style scoped>
.performance-metrics {
  margin-top: 2.5rem;
  padding: 1.5rem;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 12px;
  width: 100%;
  max-width: 800px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.3);
}

.performance-metrics h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #ffd700;
  font-size: 1.5rem;
}

.performance-metrics h3 {
  color: #f0e68c;
  margin-bottom: 1rem;
  border-bottom: 1px solid rgba(255,215,0,0.3);
  padding-bottom: 0.5rem;
}

.cards-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.metric-card {
  margin-bottom: 1rem;
  padding: 1.2rem;
  background: rgba(50, 30, 10, 0.8);
  border-radius: 8px;
  border: 1px solid rgba(201, 167, 105, 0.4);
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
  padding: 0.6rem 0.8rem;
  text-align: left;
  border-bottom: 1px solid rgba(255, 255, 255, 0.25);
}

/* Make the first column of the new tables bold for clarity */
.benchmark-results-container table td:first-child {
  font-weight: bold;
  color: #f0e68c;
}


th {
  color: #ffd700;
  font-weight: 600;
}

.benchmark-button {
  display: block;
  margin: 1.5rem auto;
  padding: 0.7rem 1.5rem;
  min-width: 220px;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background: linear-gradient(to bottom, #555, #333) !important;
}

.benchmark-results-container {
  margin-top: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.result-summary {
  font-size: 1.15rem;
  margin-bottom: 1.2rem;
  text-align: center;
  padding: 0.5rem;
  background-color: rgba(255,215,0,0.1);
  border-radius: 4px;
}

.highlight {
  color: #66bb6a; /* Green for positive improvement */
  font-weight: bold;
  font-size: 1.25rem;
}

.loading-metrics {
  text-align: center;
  padding: 2rem;
  font-size: 1.2rem;
  font-style: italic;
  color: #ccc;
}

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
.list-item-move {
  transition: transform 0.5s ease;
}

@media (max-width: 800px) {
  .performance-metrics { font-size: 0.9rem; padding: 1rem; }
  table { font-size: 0.85rem; }
  th, td { padding: 0.5rem 0.6rem; }
  .performance-metrics h2 { font-size: 1.3rem; }
}

@media (max-width: 500px) {
  .performance-metrics { font-size: 0.85rem; }
  table { font-size: 0.75rem; display: block; overflow-x: auto; }
  .performance-metrics h2 { font-size: 1.2rem; }
  .metric-card { padding: 1rem; }
  th, td { padding: 0.4rem 0.5rem; white-space: nowrap; }
}
</style>