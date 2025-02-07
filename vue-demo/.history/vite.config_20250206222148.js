import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  // 如果后端没配跨域，你想用代理，可以取消注释下面并改 "/api" => "http://localhost:8080"
  /*
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true
      }
    }
  }
  */
})
