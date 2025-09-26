import { sql } from "drizzle-orm";
import { index, jsonb, pgTable, text, timestamp } from "drizzle-orm/pg-core";

export const players = pgTable(
  "players",
  {
    uuid: text().primaryKey(),
    username: text().notNull(),
    lastJoinedDate: timestamp().defaultNow().notNull(),
    firstJoinedDate: timestamp().defaultNow().notNull(),
    stats: jsonb().$type<Record<string, string | number | boolean>>().notNull(),
  },
  (table) => [
    index("username_search_idx").using("gin", sql`to_tsvector('english', ${table.username})`),
  ],
);
