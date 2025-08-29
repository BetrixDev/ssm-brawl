import tailwindcss from "@tailwindcss/vite";
import { tanstackStart } from "@tanstack/react-start/plugin/vite";
import viteReact from "@vitejs/plugin-react";
import { defineConfig } from "vite";
import tsConfigPaths from "vite-tsconfig-paths";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    tanstackStart({ customViteReactPlugin: true, target: "vercel" }),
    tsConfigPaths(),
    viteReact(),
    tailwindcss(),
  ],
});
