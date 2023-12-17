import {
  boolean,
  json,
  mysqlTable,
  text,
  varchar,
} from "drizzle-orm/mysql-core";
import { randomUUID } from "crypto";

export const ongoingGamesTable = mysqlTable("ongoing-games", {
  gameId: varchar("game-id", { length: 36 })
    .primaryKey()
    .$defaultFn(() => randomUUID()),
  modeId: text("mode_id").notNull(),
  isRanked: boolean("is_ranked").notNull(),
  playerUuids: json("player_uuids").$type<string[]>().notNull(),
});
