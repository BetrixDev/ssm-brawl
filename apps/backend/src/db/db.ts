import { env } from "@/lib/env";
import { drizzle } from "drizzle-orm/bun-sql";
import { migrate } from "drizzle-orm/bun-sql/migrator";
import path from "node:path";
import * as schema from "./schema";

export const db = drizzle(env.DATABASE_URL, {
  schema,
  casing: "snake_case",
});

export async function migrateDb() {
  await migrate(db as any, {
    migrationsFolder: path.join(__dirname, "migrations"),
  });
}

export const Table = schema;
