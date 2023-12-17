import {
  boolean,
  int,
  json,
  mysqlTable,
  text,
  varchar,
} from "drizzle-orm/mysql-core";

export const minigamesTables = mysqlTable("minigames", {
  modeId: varchar("mode_id", { length: 25 }).primaryKey(),
  displayName: text("display_name").notNull(),
  description: text("description").notNull(),
  canBeRanked: boolean("can_be_ranked").notNull().default(false),
  playerCount: int("player_count").notNull(),
  isHidden: boolean("is_hidden").notNull().default(false),
  isEnabled: boolean("is_enabled").notNull().default(true),
  meta: json("meta"),
});
