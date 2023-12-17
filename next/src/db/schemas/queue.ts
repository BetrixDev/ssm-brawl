import { boolean, text, mysqlTable, varchar } from "drizzle-orm/mysql-core";

export const queueTable = mysqlTable("queue", {
  playerUuid: varchar("player-uuid", { length: 36 }).primaryKey(),
  modeId: text("mode").notNull(),
  isRanked: boolean("is_ranked"),
});
