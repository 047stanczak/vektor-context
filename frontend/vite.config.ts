import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  base: '/vektor/',
  resolve: {
    alias: { '@': path.resolve(__dirname, './src') },
  },
  server: {
    proxy: {
      '/vektor/api': {
        target: 'http://localhost:8080',
        rewrite: (p) => p.replace(/^\/vektor\/api/, '/api'),
        changeOrigin: true,
      },
    },
  },
})
