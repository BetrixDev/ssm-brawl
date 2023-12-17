import {
  json,
  mysqlTable,
  text,
  uniqueIndex,
  decimal,
  varchar,
} from "drizzle-orm/mysql-core";

export const passivesTable = mysqlTable(
  "passives",
  {
    id: varchar("id", { length: 25 }).primaryKey(),
    displayName: text("display_name").notNull(),
    cooldown: decimal("cooldown", { precision: 1 }).notNull(),
    meta: json("meta").$type<Record<string, number>>().notNull(),
  },
  (table) => {
    return {
      idIdx: uniqueIndex("id_idx").on(table.id),
    };
  }
);
