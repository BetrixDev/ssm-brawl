import { env } from "env/tussler";
import * as schema from "./schema.js";
import { drizzle } from "drizzle-orm/libsql";
import { createClient } from "@libsql/client";
import { migrate } from "drizzle-orm/libsql/migrator";
import path from "path";

export const libsqlClient = createClient({
  url: env.TUSSLER_URL,
  syncUrl: env.TUSSLER_SYNC_URL,
  syncInterval: env.TUSSLER_SYNC_INTERVAL,
  authToken: env.TUSSLER_TOKEN,
});

process.on("beforeExit", async (sig) => {
  try {
    await libsqlClient.sync();
  } catch {}
  process.exit(sig);
});

export const db = drizzle(libsqlClient, { schema });
export * from "drizzle-orm";
export * from "./schema.js";

export async function runMirations() {
  await migrate(db, {
    migrationsFolder: path.join(process.cwd(), "..", "migrations"),
  });
}
