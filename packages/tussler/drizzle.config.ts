import { Config, defineConfig } from "drizzle-kit";

export default defineConfig({
  driver: "turso",
  dialect: "sqlite",
  schema: "./src/schema.ts",
  verbose: true,
  dbCredentials: {
    url: process.env.TUSSLER_URL!,
    authToken: process.env.TUSSLER_TOKEN!,
  },
  out: "../../migrations",
} as Config & { dialect: "sqlite" });
