import { fileURLToPath, URL } from "node:url";

import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [vue()],
    resolve: {
        alias: {
            "@": fileURLToPath(new URL("./src", import.meta.url)),
        },
    },
    server: {
        port: 8081,
        proxy: {
            // proxy all webpack dev-server requests starting with /api to our Spring Boot backend (localhost:8080)
            "/api": {
                target: "http://localhost:8080",
                changeOrigin: true,
                secure: false,
            },
        },
    },
    build: {
        outDir: "./target/dist/",
        chunkSizeWarningLimit: 1500,
    },
});
