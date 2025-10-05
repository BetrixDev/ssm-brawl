import { defineConfig } from "drizzle-kit";

if (!process.env.DATABASE_URL) {
  console.warn(
    "DATABASE_URL is not set, if you are running a migration, you need to set the DATABASE_URL environment variable",
  );
}

export default defineConfig({
  schema: "./src/db/schema",
  out: "./src/db/migrations",
  casing: "snake_case",
  dialect: "postgresql",
  dbCredentials: {
    url: process.env.DATABASE_URL!!,
  },
});
