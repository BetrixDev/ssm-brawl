import { defineConfig } from "drizzle-kit";

const databaseUrl = process.env.DATABASE_URL;

if (!databaseUrl) {
  throw new Error(
    "DATABASE_URL is not set. Configure the database connection before running drizzle-kit.",
  );
}

export default defineConfig({
  schema: "./src/db/schema",
  out: "./src/db/migrations",
  casing: "snake_case",
  dialect: "postgresql",
  dbCredentials: {
    url: databaseUrl,
  },
});
