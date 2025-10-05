import { env } from "@/lib/env";
import { Database } from "bun:sqlite";
import { drizzle } from "drizzle-orm/bun-sqlite";
import { migrate } from "drizzle-orm/bun-sqlite/migrator";
import { mkdirSync } from "node:fs";
import path from "node:path";
import * as schema from "./schema";

if (env.DATABASE_URL !== ":memory:") {
  mkdirSync(path.dirname(env.DATABASE_URL), { recursive: true });
}

const sqlite = new Database(env.DATABASE_URL, { create: true });

export const db = drizzle(sqlite, {
  schema,
  casing: "snake_case",
});

export async function initDb() {
  if (env.DATABASE_URL !== ":memory:") {
    sqlite.run("PRAGMA journal_mode = WAL;");
  }

  migrate(db, {
    migrationsFolder: path.join(__dirname, "migrations"),
  });
}

export const Table = schema;
