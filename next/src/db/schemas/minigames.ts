import { boolean, integer, json, pgTable, text } from "drizzle-orm/pg-core";

export const minigamesTables = pgTable("minigames", {
  modeId: text("mode_id").primaryKey(),
  displayName: text("display_name").notNull(),
  description: text("description").notNull(),
  canBeRanked: boolean("can_be_ranked").notNull().default(false),
  playerCount: integer("player_count").notNull(),
  isHidden: boolean("is_hidden").notNull().default(false),
  isEnabled: boolean("is_enabled").notNull().default(true),
  meta: json("meta"),
});
