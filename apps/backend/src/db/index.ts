import { drizzle } from "drizzle-orm/neon-serverless";

import * as schema from "./schema";

export function getDb() {
  const db = drizzle(process.env.DATABASE_URL!, {
    schema,
    casing: "snake_case",
  });

  return db;
}

export type Database = ReturnType<typeof getDb>;

export const Table = schema;
