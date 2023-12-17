import {
  doublePrecision,
  json,
  pgTable,
  text,
  uniqueIndex,
} from "drizzle-orm/pg-core";

export const abilitiesTables = pgTable(
  "abilities",
  {
    id: text("id").primaryKey(),
    displayName: text("display_name").notNull(),
    toolId: text("tool_id").notNull(),
    cooldown: doublePrecision("cooldown").notNull(),
    meta: json("meta").$type<Record<string, number>>().notNull(),
  },
  (table) => {
    return {
      idIdx: uniqueIndex("id_idx").on(table.id),
    };
  }
);
