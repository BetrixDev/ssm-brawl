import { env } from "env/tussler";
import { drizzle } from "drizzle-orm/libsql";
import * as schema from "./schema.js";
import { createClient } from "@libsql/client";

const client = createClient({
  url: env.TUSSLER_URL,
  authToken: env.TUSSLER_TOKEN,
});

export const db = drizzle(client, { schema });
export * from "drizzle-orm";
export * from "./schema.js";
