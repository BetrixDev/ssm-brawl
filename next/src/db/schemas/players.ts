import {
  boolean,
  mysqlTable,
  text,
  uniqueIndex,
  varchar,
} from "drizzle-orm/mysql-core";
import { kitsTable } from ".";

export const playersTables = mysqlTable(
  "players",
  {
    uuid: varchar("uuid", { length: 36 }).primaryKey(),
    isBanned: boolean("is_banned").notNull().default(false),
    selectedKit: text("selected_kit").notNull().default("creeper"),
  },
  (table) => {
    return {
      uuidIdx: uniqueIndex("uuid_idx").on(table.uuid),
    };
  }
);
