import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  server: {
    proxy: {
      '/auth': {
        target: 'http://localhost:9090',
        changeOrigin: true,
        secure: false,
      }
    }
  },

  plugins: [
    vue(),
    vueDevTools(),
  ],
   resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    }
  }
})
