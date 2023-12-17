import { boolean, pgTable, text, uniqueIndex } from "drizzle-orm/pg-core";
import { kitsTable } from ".";

export const playersTables = pgTable(
  "players",
  {
    uuid: text("uuid").primaryKey(),
    isBanned: boolean("is_banned").notNull().default(false),
    selectedKit: text("selected_kit")
      .notNull()
      .default("creeper")
      .references(() => kitsTable.id),
  },
  (table) => {
    return {
      uuidIdx: uniqueIndex("uuid_idx").on(table.uuid),
    };
  }
);
