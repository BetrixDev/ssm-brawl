import { defineConfig } from "drizzle-kit";
import { env } from "env";

export default defineConfig({
  driver: "turso",
  schema: "./src/db/schema.ts",
  dbCredentials: {
    url: env.TUSSLER_URL,
    authToken: env.TUSSLER_TOKEN,
  },
});
