import { defineConfig } from "drizzle-kit";
import { env } from "env";

export default defineConfig({
  driver: "turso",
  schema: "./src/schema/*.ts",
  dbCredentials: {
    url: env.DATABASE_URL,
    authToken: env.DATABASE_TOKEN,
  },
});
