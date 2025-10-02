import { sqliteTable, text } from "drizzle-orm/sqlite-core";

export const kv = sqliteTable("kv", {
  key: text("key").primaryKey(),
  value: text("value"),
});
