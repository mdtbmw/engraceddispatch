import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";

function jsonDefaultPlugin() {
  return {
    name: "json-default-plugin",
    transform(code: string, id: string) {
      if (id.endsWith(".json")) {
        return {
          code: `export default ${code};`,
          map: null,
        };
      }
    },
  };
}

export default defineConfig({
  plugins: [
    jsonDefaultPlugin(),
    react({
      include: "**/*.{jsx,tsx,js,ts}",
    }),
  ],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
      "~": path.resolve(__dirname, "./src"),
    },
  },
  server: {
    port: 3000,
  },
  build: {
    outDir: "dist",
  },
});
