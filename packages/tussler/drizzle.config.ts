import { defineConfig } from "drizzle-kit";

export default defineConfig({
  driver: "turso",
  schema: "./src/schema.ts",
  verbose: true,
  dbCredentials: {
    url: process.env.TUSSLER_URL!,
    authToken: process.env.TUSSLER_TOKEN!,
  },
  out: "./migrations",
});
