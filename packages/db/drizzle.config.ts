import { defineConfig } from "drizzle-kit";
import { env } from "env";

export default defineConfig({
  driver: "mysql2",
  schema: "./src/schema/*.ts",
  dbCredentials: {
    uri: env.DATABASE_URI,
  },
});
