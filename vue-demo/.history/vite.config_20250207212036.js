import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    // 开发环境代理，仅在本地运行时生效
    proxy: {
      "/api": {
        target: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, "/api/oxo")  // 统一API前缀
      }
    }
  }
})