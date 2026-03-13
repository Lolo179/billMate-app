import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";
import path from "node:path";
export default defineConfig({
    plugins: [react()],
    server: {
        proxy: {
            "/auth": {
                target: "http://localhost:8080",
                changeOrigin: true
            },
            "/billing": {
                target: "http://localhost:8080",
                changeOrigin: true
            }
        }
    },
    resolve: {
        alias: {
            "@": path.resolve(__dirname, "./src")
        }
    },
    test: {
        environment: "jsdom",
        setupFiles: "./src/test/setup.ts",
        globals: true
    }
});
