import { env } from "env/tussler";
import * as schema from "./schema.js";
import { drizzle, LibSQLDatabase } from "drizzle-orm/libsql";
import { Client, createClient } from "@libsql/client";
import { migrate } from "drizzle-orm/libsql/migrator";
import { SQLiteTableWithColumns } from "drizzle-orm/sqlite-core";
import path from "path";
import { existsSync, mkdirSync, readFileSync } from "fs";

const TEST_DB_BASE_DIR = path.join(process.cwd(), "test_dbs");

if (env.NODE_ENV === "test" && !existsSync(TEST_DB_BASE_DIR)) {
  mkdirSync(TEST_DB_BASE_DIR);
}

export let libsqlClient: Client;

process.on("beforeExit", async (sig) => {
  try {
    await libsqlClient.sync();
  } catch {}
  process.exit(sig);
});

export let db: LibSQLDatabase<typeof schema>;
export * from "drizzle-orm";
export * from "./schema.js";

export function initTussler(databaseName?: string) {
  libsqlClient = createClient({
    url:
      env.NODE_ENV === "test"
        ? `file:${path.join(
            TEST_DB_BASE_DIR,
            `${Date.now()}-${databaseName?.replaceAll("/", "") ?? ""}.sqlite`,
          )}`
        : env.TUSSLER_URL,
    syncUrl: env.NODE_ENV === "test" ? undefined : env.TUSSLER_SYNC_URL,
    syncInterval: env.NODE_ENV === "test" ? undefined : env.TUSSLER_SYNC_INTERVAL,
    authToken: env.NODE_ENV === "test" ? undefined : env.TUSSLER_TOKEN,
  });

  db = drizzle(libsqlClient, { schema });
}

export async function runMirations() {
  await migrate(db, {
    migrationsFolder: path.join(process.cwd(), "..", "migrations"),
  });
}

export const schemaTableMappings: Record<string, SQLiteTableWithColumns<any>> = Object.entries(
  schema,
).reduce((prev, curr) => {
  return {
    ...prev,
    [curr[0]]: curr[1],
  };
}, {});

export async function clearAllTables() {
  if (env.NODE_ENV !== "test") {
    throw new Error("This function should only be called in test environments");
  }

  for (const table of Object.values(schemaTableMappings)) {
    try {
      await db.delete(table);
    } catch {}
  }
}

export async function loadTestTableData(name: string) {
  const testPath = path.join(process.cwd(), "test", "data", `${name}.json`);
  const tableName = name.split("-")[0];

  const jsonContents = JSON.parse(readFileSync(testPath).toString());

  await db.insert(schemaTableMappings[tableName]).values(jsonContents);

  return jsonContents;
}
