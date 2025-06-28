import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'
import path from 'path';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './setupTests.js', // Configuración opcional para extensiones como jest-dom
  },
  resolve: {
    alias: {
      '@': path.resolve(new URL('.', import.meta.url).pathname, 'src'),
    },
  },
  server: {
    host: '0.0.0.0',
    port: 5173,
//    hmr: false,
    hmr:{
      port: 24678,
      clientPort: 5173,
    },
  },
  /*preview:{
    port: 4173
  }*/
});
