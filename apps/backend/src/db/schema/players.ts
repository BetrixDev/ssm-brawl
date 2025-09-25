import { sql } from "drizzle-orm";
import { sqliteTable, text } from "drizzle-orm/sqlite-core";

export const players = sqliteTable("players", {
  uuid: text("uuid").primaryKey(),
  username: text("username").notNull(),
  lastJoinedDate: text("last_joined_date")
    .default(sql`CURRENT_TIMESTAMP`)
    .notNull(),
});
