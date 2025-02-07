import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    // 仅在开发环境启用代理
    proxy: import.meta.env.MODE === 'development' ? {
      "/api": {
        target: import.meta.env.VITE_API_BASE_URL, // 直接使用环境变量
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, "/api/oxo")
      }
    } : {} // 生产环境无需代理
  }
})