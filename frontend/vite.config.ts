/// <reference types="vitest/config" />
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "node:path";

export default defineConfig(({ mode }) => {
  const isDev = mode === "development";

  return {
    plugins: [react()],
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "src"),
      },
    },
    test: {
      globals: true,
      environment: "jsdom",
      css: true,
      setupFiles: ["./src/test-setup.ts"],
      exclude: ["node_modules", "e2e"],
    },
    server: {
      port: 5173,
      proxy: {
        "/api": {
          target: "http://localhost:8080",
          changeOrigin: true,
        },
        "/actuator": {
          target: "http://localhost:8080",
          changeOrigin: true,
        },
        "/auth": {
          target: "http://localhost:8080",
          changeOrigin: true,
        },
      },
    },
    build: {
      outDir: "dist",
      emptyOutDir: true,
      sourcemap: isDev,
      manifest: false,
      chunkSizeWarningLimit: 1500,
    },
  };
});
