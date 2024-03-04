import { relations } from "drizzle-orm";
import {
  double,
  index,
  json,
  mysqlTable,
  primaryKey,
  tinyint,
  varchar,
} from "drizzle-orm/mysql-core";

export const kitsTable = mysqlTable(
  "kits",
  {
    id: varchar("id", { length: 36 }).primaryKey().notNull(),
    meleeDamage: double("melee_damage").notNull(),
    armor: double("armor").notNull(),
    inventoryIcon: varchar("inventory_icon", { length: 32 }).notNull(),
    meta: json("meta"),
  },
  (table) => ({
    idIdx: index("id_idx").on(table.id),
  })
);

export const abilitiesTable = mysqlTable(
  "abilities",
  {
    id: varchar("id", { length: 36 }).primaryKey().notNull(),
    meta: json("meta"),
  },
  (table) => ({
    idIdx: index("id_idx").on(table.id),
  })
);

export const abilitiesToKitsTable = mysqlTable("abilities_to_kits", {
  kitId: varchar("kit_id", { length: 36 }).notNull(),
  abilityId: varchar("ability_id", { length: 36 }).notNull(),
});

export const passivesTable = mysqlTable(
  "passives",
  {
    id: varchar("id", { length: 36 }).primaryKey().notNull(),
    meta: json("meta"),
  },
  (table) => ({
    idIdx: index("id_idx").on(table.id),
  })
);

export const passivesToKitsTable = mysqlTable("passives_to_kits", {
  kitId: varchar("kit_id", { length: 36 }).notNull(),
  passiveId: varchar("passive_id", { length: 36 }).notNull(),
});

export const basicPlayerDataTable = mysqlTable(
  "basic_player_data",
  {
    uuid: varchar("uuid", { length: 36 }).primaryKey().notNull(),
    selectedKitId: varchar("selected_kit_id", { length: 32 }).notNull(),
  },
  (table) => ({
    uuidIdx: index("uuid_idx").on(table.uuid),
  })
);

export const minigamesTable = mysqlTable(
  "minigames",
  {
    id: varchar("id", { length: 64 }).primaryKey(),
    minPlayers: tinyint("min_players").notNull(),
    maxPlayers: tinyint("max_players").notNull(),
  },
  (table) => ({
    idIdx: index("id_idx").on(table.id),
  })
);

export const queueTable = mysqlTable(
  "queue",
  {
    playerUuid: varchar("player_uuid", { length: 36 }).primaryKey(),
    minigameId: varchar("minigame_id", { length: 64 }).notNull(),
  },
  (table) => ({
    minigameIdIdx: index("minigame_id_idx").on(table.minigameId),
    playerUuidIdx: index("player_uuid_idx").on(table.playerUuid),
  })
);

export const mapsTable = mysqlTable(
  "maps",
  {
    id: varchar("id", { length: 32 }).primaryKey(),
    minPlayers: tinyint("min_players").notNull(),
    maxPlayers: tinyint("max_players").notNull(),
  },
  (table) => ({
    idIdx: index("id_idx").on(table.id),
  })
);

export const mapSpawnpointsTable = mysqlTable(
  "map_spawnpoints",
  {
    mapId: varchar("map_id", { length: 32 }).notNull(),
    x: double("x").notNull(),
    y: double("y").notNull(),
    z: double("z").notNull(),
  },
  (table) => ({
    pk: primaryKey({ columns: [table.mapId, table.x, table.y, table.z] }),
    mapIdIdx: index("map_id_idx").on(table.mapId),
  })
);

export const kitsRelations = relations(kitsTable, ({ many }) => ({
  abilities: many(abilitiesToKitsTable),
  passives: many(passivesToKitsTable),
}));

export const abilitiesRelations = relations(abilitiesTable, ({ many }) => ({
  kits: many(abilitiesToKitsTable),
}));

export const passivesRelations = relations(passivesTable, ({ many }) => ({
  kits: many(passivesToKitsTable),
}));

export const abilitiesToKitsRelations = relations(
  abilitiesToKitsTable,
  ({ one }) => ({
    kit: one(kitsTable, {
      fields: [abilitiesToKitsTable.kitId],
      references: [kitsTable.id],
    }),
    ability: one(abilitiesTable, {
      fields: [abilitiesToKitsTable.abilityId],
      references: [abilitiesTable.id],
    }),
  })
);

export const passivesToKitsRelations = relations(
  passivesToKitsTable,
  ({ one }) => ({
    kit: one(kitsTable, {
      fields: [passivesToKitsTable.kitId],
      references: [kitsTable.id],
    }),
    passive: one(passivesTable, {
      fields: [passivesToKitsTable.passiveId],
      references: [passivesTable.id],
    }),
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
    fields: [queueTable.playerUuid],
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
