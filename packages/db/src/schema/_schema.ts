import { relations } from "drizzle-orm";
import { randomUUID } from "crypto";
import {
  decimal,
  json,
  mysqlTable,
  primaryKey,
  tinyint,
  varchar,
} from "drizzle-orm/mysql-core";

export const kitsTable = mysqlTable("kits", {
  id: varchar("id", { length: 36 }).primaryKey().notNull(),
  meleeDamage: decimal("melee_damage").notNull(),
  armor: decimal("armor").notNull(),
  inventoryIcon: varchar("inventory_icon", { length: 32 }).notNull(),
  meta: json("meta"),
});

export const basicPlayerDataTable = mysqlTable("basic_player_data", {
  uuid: varchar("uuid", { length: 36 }).primaryKey().notNull(),
  selectedKitId: varchar("selected_kit_id", { length: 32 }).notNull(),
});

export const minigamesTable = mysqlTable("minigames", {
  id: varchar("id", { length: 64 }).primaryKey(),
  minPlayers: tinyint("min_players").notNull(),
  maxPlayers: tinyint("max_players").notNull(),
});

export const queueTable = mysqlTable("queue", {
  id: varchar("id", { length: 36 })
    .primaryKey()
    .$defaultFn(() => randomUUID()),
  minigameId: varchar("minigame_id", { length: 64 }).notNull(),
  playerId: varchar("player_id", { length: 32 }).notNull(),
});

export const mapsTable = mysqlTable("maps", {
  id: varchar("id", { length: 32 }).primaryKey(),
  minPlayers: tinyint("min_players").notNull(),
  maxPlayers: tinyint("max_players").notNull(),
});

export const mapSpawnpointsTable = mysqlTable(
  "map_spawnpoints",
  {
    mapId: varchar("map_id", { length: 32 }).notNull(),
    x: decimal("x").notNull(),
    y: decimal("y").notNull(),
    z: decimal("z").notNull(),
  },
  (table) => ({
    pk: primaryKey({ columns: [table.mapId, table.x, table.y, table.z] }),
  })
);

export const mapsRelations = relations(mapsTable, ({ many }) => ({
  spawnPoints: many(mapSpawnpointsTable),
}));

export const mapSpawnpointsRelations = relations(
  mapSpawnpointsTable,
  ({ one }) => ({
    map: one(mapsTable, {
      fields: [mapSpawnpointsTable.mapId],
      references: [mapsTable.id],
    }),
  })
);

export const minigamesRelations = relations(minigamesTable, ({ many }) => ({
  queueEntries: many(queueTable),
}));

export const queueRelations = relations(queueTable, ({ one }) => ({
  minigame: one(minigamesTable, {
    fields: [queueTable.minigameId],
    references: [minigamesTable.id],
  }),
  player: one(basicPlayerDataTable, {
    fields: [queueTable.playerId],
    references: [basicPlayerDataTable.uuid],
  }),
}));

export const basicPlayerDataRelations = relations(
  basicPlayerDataTable,
  ({ one }) => ({
    selectedKit: one(kitsTable, {
      fields: [basicPlayerDataTable.selectedKitId],
      references: [kitsTable.id],
    }),
  })
);
