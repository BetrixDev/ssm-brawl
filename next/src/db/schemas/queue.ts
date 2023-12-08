import { boolean, pgTable, text, uuid } from "drizzle-orm/pg-core";

export const queueTable = pgTable("queue", {
  playerUuid: uuid("player-uuid").primaryKey(),
  modeId: text("mode").notNull(),
  isRanked: boolean("is_ranked"),
});
