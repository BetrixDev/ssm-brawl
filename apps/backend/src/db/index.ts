import { drizzle } from "drizzle-orm/neon-serverless";

import * as schema from "./schema";

export const db = drizzle(process.env.DATABASE_URL!, {
  schema,
  casing: "snake_case",
});

export const Table = schema;
