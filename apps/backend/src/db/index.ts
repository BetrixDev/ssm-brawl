import { env } from "@/lib/env";
import { drizzle } from "drizzle-orm/bun-sql";
import * as schema from "./schema";

export function getDb() {
  const db = drizzle(env.DATABASE_URL, {
    schema,
    casing: "snake_case",
  });

  return db;
}

export type Database = ReturnType<typeof getDb>;

export const Table = schema;
