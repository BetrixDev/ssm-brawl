import { env } from "env/tussler";
import { drizzle } from "drizzle-orm/libsql";
import * as schema from "./schema.js";
import { createClient } from "@libsql/client";

const client = createClient({
  url: env.TUSSLER_URL,
  syncUrl: env.TUSSLER_SYNC_URL,
  syncInterval: env.TUSSLER_SYNC_INTERVAL,
  authToken: env.TUSSLER_TOKEN,
});

process.on("beforeExit", async (sig) =>{
  await client.sync()
  process.exit(sig)
})

export const db = drizzle(client, { schema });
export * from "drizzle-orm";
export * from "./schema.js";
