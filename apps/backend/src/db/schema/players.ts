import { pgTable, text, timestamp } from "drizzle-orm/pg-core";

export const players = pgTable("players", {
  uuid: text().primaryKey(),
  username: text().notNull(),
  lastJoinedDate: timestamp().defaultNow().notNull(),
});
