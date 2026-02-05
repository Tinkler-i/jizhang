import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/jizhang/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path
      },
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => '/jizhang' + path  // /api/captcha/verify -> /jizhang/api/captcha/verify
      }
    }
  },
  build: {
    outDir: path.resolve(__dirname, '../dist'),  // 指定为 dist 目录，而不是父目录
    emptyOutDir: true,  // 构建前清空输出目录
    rollupOptions: {
      output: {
        entryFileNames: '[name].js',
        chunkFileNames: '[name]-[hash].js',
        assetFileNames: ({ name }) => {
          if (/\.css$/.test(name ?? '')) {
            return 'css/[name].css';
          }
          return 'assets/[name].[hash]';
        }
      }
    }
  }
})
