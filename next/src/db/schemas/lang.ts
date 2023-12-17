import { mysqlTable, text, varchar } from "drizzle-orm/mysql-core";

export const langTable = mysqlTable("lang", {
  key: varchar("key", { length: 75 }).primaryKey(),
  locale: varchar("locale", { length: 5 }).notNull(),
  string: text("string").notNull(),
});
