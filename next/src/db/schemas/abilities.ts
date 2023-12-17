import {
  decimal,
  json,
  mysqlTable,
  text,
  uniqueIndex,
  varchar,
} from "drizzle-orm/mysql-core";

export const abilitiesTables = mysqlTable(
  "abilities",
  {
    id: varchar("id", { length: 25 }).primaryKey(),
    displayName: text("display_name").notNull(),
    toolId: text("tool_id").notNull(),
    cooldown: decimal("cooldown", { precision: 1 }).notNull(),
    meta: json("meta").$type<Record<string, number>>().notNull(),
  },
  (table) => {
    return {
      idIdx: uniqueIndex("id_idx").on(table.id),
    };
  }
);
