import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tsconfigPaths from 'vite-tsconfig-paths';
// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react(), tsconfigPaths()],
  server: {
    // proxy: {
    //   '/api': {
    //     target: 'http://localhost:8080',
    //     changeOrigin: true,
    //     secure: false,
    //     ws: true,
    //   },
    // },
    port: 3000,
    // allowedHosts: [],
    // hmr: {
    //   clientPort: 443
    // }
  },
  build: {
    outDir: './build',
    chunkSizeWarningLimit: 1500,
  },
});
