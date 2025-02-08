import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  // 加载环境变量（仅开发环境需要代理）
  const env = loadEnv(mode, process.cwd(), 'VITE_')

  return {
    plugins: [vue()],
    server: {
      // 仅在开发环境启用代理
      proxy: mode === 'development' ? {
        "/api": {
          target: env.VITE_API_BASE_URL || "http://localhost:8080",
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, "/api/oxo")
        }
      } : {} // 生产环境无需代理配置
    }
  }
})