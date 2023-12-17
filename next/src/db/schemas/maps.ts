import {
  boolean,
  int,
  json,
  mysqlTable,
  text,
  varchar,
} from "drizzle-orm/mysql-core";

export const mapsTable = mysqlTable("maps", {
  mapId: varchar("map_id", { length: 25 }).primaryKey().notNull(),
  displayName: text("display_name").notNull(),
  canBeRanked: boolean("can_be_ranked").notNull().default(false),
  isHidden: boolean("is_hidden").notNull().default(false),
  minPlayerCount: int("min_player_count").notNull(),
  maxPlayerCount: int("max_player_count").notNull(),
  spawnLocations: json("spawn_locations")
    .$type<{ x: number; y: number; z: number }[]>()
    .notNull(),
});
