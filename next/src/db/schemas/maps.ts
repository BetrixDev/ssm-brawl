import { boolean, integer, json, pgTable, text } from "drizzle-orm/pg-core";

export const mapsTable = pgTable("maps", {
  mapId: text("map_id").primaryKey().notNull(),
  displayName: text("display_name").notNull(),
  canBeRanked: boolean("can_be_ranked").notNull().default(false),
  isHidden: boolean("is_hidden").notNull().default(false),
  minPlayerCount: integer("min_player_count").notNull(),
  maxPlayerCount: integer("max_player_count").notNull(),
  spawnLocations: json("spawn_locations")
    .$type<{ x: number; y: number; z: number }[]>()
    .notNull(),
});
