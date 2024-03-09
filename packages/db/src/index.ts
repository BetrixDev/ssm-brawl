import { env } from "env";
import { drizzle } from "drizzle-orm/libsql";
import * as schema from "./schema/_schema.js";
import { createClient } from "@libsql/client";

const client = createClient({
  url: env.DATABASE_URL,
  authToken: env.DATABASE_TOKEN,
});

export const db = drizzle(client, { schema });
export * from "drizzle-orm";
export * from "./schema/_schema.js";
