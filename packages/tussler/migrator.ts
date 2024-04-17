import { db, libsqlClient } from "./src/index.js";
import { migrate } from "drizzle-orm/libsql/migrator";

await migrate(db, { migrationsFolder: "./migrations" });
