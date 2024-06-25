import { env } from "env/tussler";
import * as schema from "./schema.js";
import { drizzle as postgresDrizzle, PostgresJsDatabase } from "drizzle-orm/postgres-js";
import { drizzle as pgLiteDrizzle, PgliteDatabase } from "drizzle-orm/pglite";
import { migrate as postgresMigrate } from "drizzle-orm/postgres-js/migrator";
import { migrate as pgLiteMigrate } from "drizzle-orm/pglite/migrator";
import { SQLiteTableWithColumns } from "drizzle-orm/sqlite-core";
import postgres from "postgres";
import path from "path";
import { readFileSync } from "fs";
import { PGlite } from "@electric-sql/pglite";

export let db: PostgresJsDatabase<typeof schema> | PgliteDatabase<typeof schema>;
export * from "drizzle-orm";
export * from "./schema.js";

export function initTussler() {
  if (env.TUSSLER_TYPE === "postgres") {
    const postgresClient = postgres({ host: env.TUSSLER_HOST, password: env.TUSSLER_PASSWORD });
    db = postgresDrizzle(postgresClient);
  } else {
    const pgLite = new PGlite();
    db = pgLiteDrizzle(pgLite);
  }
}

export async function runMigrations() {
  if (env.TUSSLER_TYPE === "postgres") {
    await postgresMigrate(db as PostgresJsDatabase<typeof schema>, {
      migrationsFolder: path.join(process.cwd(), "..", "migrations"),
    });
  } else {
    await pgLiteMigrate(db as PgliteDatabase<typeof schema>, {
      migrationsFolder: path.join(process.cwd(), "..", "migrations"),
    });
  }
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
