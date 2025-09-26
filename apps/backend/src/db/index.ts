import { env } from "cloudflare:workers";
import { drizzle } from "drizzle-orm/node-postgres";
import { Pool } from "pg";
import { CloudflareKvCache } from "./cache";
import * as schema from "./schema";

const pool = new Pool({
  connectionString: env.DATABASE_URL,
});

export const db = drizzle(pool, {
  schema,
  casing: "snake_case",
  cache: new CloudflareKvCache(env.KV),
});

export const Table = schema;
