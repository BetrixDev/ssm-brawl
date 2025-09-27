import { drizzle } from "drizzle-orm/neon-serverless";

import * as schema from "./schema";

export async function getDb() {
  const db = drizzle(process.env.DATABASE_URL!, {
    schema,
    casing: "snake_case",
  });

  await db.$client.connect();

  return db;
}

export type Database = Awaited<ReturnType<typeof getDb>>;

export const Table = schema;
