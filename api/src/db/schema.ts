import { relations } from "drizzle-orm";
import {
  real,
  index,
  sqliteTable,
  primaryKey,
  integer,
  text,
  int,
} from "drizzle-orm/sqlite-core";

export type MapRole = "game" | "hub";

export const kitsTable = sqliteTable(
  "kits",
  {
    id: text("id").primaryKey().notNull(),
    meleeDamage: real("melee_damage").notNull(),
    armor: real("armor").notNull(),
    inventoryIcon: text("inventory_icon").notNull(),
    disguiseId: text("disguise_id").notNull(),
    helmetId: text("helmet_id"),
    chestplateId: text("chestplate_id"),
    leggingsId: text("leggings_id"),
    bootsId: text("boots_id"),
    meta: text("meta", { mode: "json" }),
  },
  (table) => ({
    idIdx: index("kits_id_idx").on(table.id),
  })
);

export const abilitiesTable = sqliteTable(
  "abilities",
  {
    id: text("id").primaryKey().notNull(),
    meta: text("meta", { mode: "json" }),
    cooldown: int("cooldown").notNull(),
  },
  (table) => ({
    idIdx: index("abilities_id_idx").on(table.id),
  })
);

export const disguisesTable = sqliteTable("disguises", {
  id: text("id").primaryKey().notNull(),
  displayEntity: text("display_entity").notNull(),
  hurtSound: text("hurt_sound").notNull(),
});

export const abilitiesToKitsTable = sqliteTable(
  "abilities_to_kits",
  {
    kitId: text("kit_id").notNull(),
    abilityId: text("ability_id").notNull(),
    abilityToolSlot: int("ability_tool_slot").notNull(),
  },
  (table) => ({
    kitIdIdx: index("atk_kit_id_idx").on(table.kitId),
    abilityIdIdx: index("atk_ability_id_idx").on(table.abilityId),
    pk: primaryKey({
      name: "abilities_to_kits_pk",
      columns: [table.abilityId, table.kitId],
    }),
  })
);

export const passivesTable = sqliteTable(
  "passives",
  {
    id: text("id").primaryKey().notNull(),
    meta: text("meta", { mode: "json" }),
  },
  (table) => ({
    idIdx: index("passives_id_idx").on(table.id),
  })
);

export const passivesToKitsTable = sqliteTable(
  "passives_to_kits",
  {
    kitId: text("kit_id").notNull(),
    passiveId: text("passive_id").notNull(),
  },
  (table) => ({
    kitIdIdx: index("ptk_kit_id_idx").on(table.kitId),
    passiveIdIdx: index("ptk_passive_id_idx").on(table.passiveId),
    pk: primaryKey({
      name: "passives_to_kits_pk",
      columns: [table.passiveId, table.kitId],
    }),
  })
);

export const basicPlayerDataTable = sqliteTable(
  "basic_player_data",
  {
    uuid: text("uuid", { length: 36 }).primaryKey().notNull(),
    selectedKitId: text("selected_kit_id").notNull(),
    totalGamesPlayed: int("total_games_played").notNull().default(0),
    totalGamesWon: int("total_games_won").notNull().default(0),
    totalPlaytimeSeconds: int("total_playtime_seconds").notNull().default(0),
    isBanned: int("is_banned", { mode: "boolean" }).notNull().default(false),
  },
  (table) => ({
    uuidIdx: index("b_player_uuid_idx").on(table.uuid),
  })
);

export const ipBansTable = sqliteTable(
  "ip_bans",
  {
    ip: text("ip").primaryKey().notNull(),
    isBanned: int("is_banned", { mode: "boolean" }).notNull().default(true),
  },
  (table) => ({
    ipIdx: index("ip_idx").on(table.ip),
  })
);

export const minigamesTable = sqliteTable(
  "minigames",
  {
    id: text("id").primaryKey(),
    minPlayers: integer("min_players").notNull(),
    maxPlayers: integer("max_players").notNull(),
    playersPerTeam: integer("players_per_team").notNull().default(1),
    countdownSeconds: integer("countdown_seconds").notNull().default(5),
    stocks: integer("stocks").notNull().default(4),
  },
  (table) => ({
    idIdx: index("minigames_id_idx").on(table.id),
  })
);

export const queueTable = sqliteTable(
  "queue",
  {
    playerUuid: text("player_uuid", { length: 36 }).primaryKey(),
    minigameId: text("minigame_id").notNull(),
  },
  (table) => ({
    minigameIdIdx: index("queue_minigame_id_idx").on(table.minigameId),
    playerUuidIdx: index("queue_player_uuid_idx").on(table.playerUuid),
  })
);

export const mapsTable = sqliteTable(
  "maps",
  {
    id: text("id").primaryKey(),
    minPlayers: integer("min_players").notNull(),
    maxPlayers: integer("max_players").notNull(),
    originId: text("origin_id").notNull(),
    worldBorderRadius: integer("world_border_radius").notNull(),
    role: text("role", { enum: ["game", "hub"] })
      .$type<MapRole>()
      .notNull()
      .default("game"),
  },
  (table) => ({
    idIdx: index("maps_id_idx").on(table.id),
  })
);

export const mapOriginsTable = sqliteTable("map_origins", {
  mapId: text("map_id").primaryKey(),
  x: real("x").notNull(),
  y: real("y").notNull(),
  z: real("z").notNull(),
});

export const mapSpawnpointsTable = sqliteTable(
  "map_spawnpoints",
  {
    mapId: text("map_id").notNull(),
    x: real("x").notNull(),
    y: real("y").notNull(),
    z: real("z").notNull(),
  },
  (table) => ({
    pk: primaryKey({ columns: [table.mapId, table.x, table.y, table.z] }),
    mapIdIdx: index("spawnpoints_map_id_idx").on(table.mapId),
  })
);

export const langTable = sqliteTable("lang", {
  id: text("id").notNull().primaryKey(),
  text: text("text").notNull(),
});

export const kitsRelations = relations(kitsTable, ({ many, one }) => ({
  abilities: many(abilitiesToKitsTable),
  passives: many(passivesToKitsTable),
  disguise: one(disguisesTable, {
    fields: [kitsTable.disguiseId],
    references: [disguisesTable.id],
  }),
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

export const mapsRelations = relations(mapsTable, ({ many, one }) => ({
  spawnPoints: many(mapSpawnpointsTable),
  origin: one(mapOriginsTable, {
    fields: [mapsTable.originId],
    references: [mapOriginsTable.mapId],
  }),
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
