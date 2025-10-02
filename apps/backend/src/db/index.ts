import { Database } from "bun:sqlite";
import { drizzle } from "drizzle-orm/bun-sqlite";
import { migrate } from "drizzle-orm/bun-sqlite/migrator";
import { mkdirSync } from "node:fs";
import path from "node:path";
import * as schema from "./schema";

mkdirSync("./data", { recursive: true });

const sqlite = new Database("./data/database.sqlite", { create: true });

export const db = drizzle(sqlite, {
  schema,
  casing: "snake_case",
});

export async function initDb() {
  sqlite.exec("PRAGMA journal_mode = WAL;");

  migrate(db, {
    migrationsFolder: path.join(__dirname, "migrations"),
  });
}

export const Table = schema;
