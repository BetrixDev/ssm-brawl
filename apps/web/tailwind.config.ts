import type { Config } from "tailwindcss";

export default {
  content: [
    "./apps/web/index.html",
    "./apps/web/src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {},
  },
  plugins: [],
} satisfies Config;