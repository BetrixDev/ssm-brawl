import { defineConfig } from "vite";

export default defineConfig({
  test: {
    coverage: {
      extension: [".ts"],
    },
  },
});
