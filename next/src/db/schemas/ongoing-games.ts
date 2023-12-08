import { boolean, json, pgTable, text, uuid } from "drizzle-orm/pg-core";
import { sql } from "..";

export const ongoingGamesTable = pgTable("ongoing-games", {
  gameId: uuid("game-id")
    .primaryKey()
    .default(sql`gen_random_uuid()`),
  modeId: text("mode_id").notNull(),
  isRanked: boolean("is_ranked").notNull(),
  playerUuids: json("player_uuids").$type<string[]>().notNull(),
});
