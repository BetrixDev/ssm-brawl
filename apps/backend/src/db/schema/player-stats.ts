import { relations } from "drizzle-orm";
import { index, jsonb, pgTable, primaryKey, serial, text, timestamp } from "drizzle-orm/pg-core";
import { players } from "./players";

export const playerGeneralStats = pgTable(
  "player_general_stats",
  {
    playerUuid: text()
      .references(() => players.uuid, { onDelete: "cascade" })
      .notNull(),
    statId: text().notNull(),
    value: jsonb().$type<unknown>().notNull(),
  },
  (table) => [
    primaryKey({ name: "player_general_stats_pk", columns: [table.playerUuid, table.statId] }),
  ],
);

export const playerKitStats = pgTable(
  "player_kit_stats",
  {
    playerUuid: text()
      .references(() => players.uuid, { onDelete: "cascade" })
      .notNull(),
    kitId: text().notNull(),
    statId: text().notNull(),
    value: jsonb().$type<unknown>().notNull(),
  },
  (table) => [
    primaryKey({
      name: "player_kit_stats_pk",
      columns: [table.playerUuid, table.kitId, table.statId],
    }),
  ],
);

export const playerMinigameStats = pgTable(
  "player_minigame_stats",
  {
    playerUuid: text()
      .references(() => players.uuid, { onDelete: "cascade" })
      .notNull(),
    minigameId: text().notNull(),
    statId: text().notNull(),
    value: jsonb().$type<unknown>().notNull(),
  },
  (table) => [
    primaryKey({
      name: "player_minigame_stats_pk",
      columns: [table.playerUuid, table.minigameId, table.statId],
    }),
  ],
);

export const playerParkourStats = pgTable(
  "player_parkour_stats",
  {
    playerUuid: text()
      .references(() => players.uuid, { onDelete: "cascade" })
      .notNull(),
    mapId: text().notNull(),
    statId: text().notNull(),
    value: jsonb().$type<unknown>().notNull(),
  },
  (table) => [
    primaryKey({
      name: "player_parkour_stats_pk",
      columns: [table.playerUuid, table.mapId, table.statId],
    }),
  ],
);

export const playerPassiveStats = pgTable(
  "player_passive_stats",
  {
    playerUuid: text()
      .references(() => players.uuid, { onDelete: "cascade" })
      .notNull(),
    passiveId: text().notNull(),
    statId: text().notNull(),
    value: jsonb().$type<unknown>().notNull(),
  },
  (table) => [
    primaryKey({
      name: "player_passive_stats_pk",
      columns: [table.playerUuid, table.passiveId, table.statId],
    }),
  ],
);

export const playerAbilityStats = pgTable(
  "player_ability_stats",
  {
    playerUuid: text()
      .references(() => players.uuid, { onDelete: "cascade" })
      .notNull(),
    abilityId: text().notNull(),
    statId: text().notNull(),
    value: jsonb().$type<unknown>().notNull(),
  },
  (table) => [
    primaryKey({
      name: "player_ability_stats_pk",
      columns: [table.playerUuid, table.abilityId, table.statId],
    }),
  ],
);

// Historical Tables

export const playerGeneralStatsHistory = pgTable(
  "player_general_stats_history",
  {
    id: serial().primaryKey(),
    playerUuid: text()
      .notNull()
      .references(() => players.uuid, { onDelete: "cascade" }),
    statId: text().notNull(),
    value: jsonb().$type<unknown>().notNull(),
    timestamp: timestamp().defaultNow().notNull(),
  },
  (table) => [
    index("player_general_stats_history_player_uuid_idx").using("btree", table.playerUuid),
    index("player_general_stats_history_timestamp_idx").using("btree", table.timestamp),
  ],
);

export const playerKitStatsHistory = pgTable(
  "player_kit_stats_history",
  {
    id: serial().primaryKey(),
    playerUuid: text()
      .notNull()
      .references(() => players.uuid, { onDelete: "cascade" }),
    kitId: text().notNull(),
    statId: text().notNull(),
    value: jsonb().$type<unknown>().notNull(),
    timestamp: timestamp().defaultNow().notNull(),
  },
  (table) => [
    index("player_kit_stats_history_player_uuid_idx").using("btree", table.playerUuid),
    index("player_kit_stats_history_timestamp_idx").using("btree", table.timestamp),
  ],
);

export const playerMinigameStatsHistory = pgTable(
  "player_minigame_stats_history",
  {
    id: serial().primaryKey(),
    playerUuid: text()
      .notNull()
      .references(() => players.uuid, { onDelete: "cascade" }),
    minigameId: text().notNull(),
    statId: text().notNull(),
    value: jsonb().$type<unknown>().notNull(),
    timestamp: timestamp().defaultNow().notNull(),
  },
  (table) => [
    index("player_minigame_stats_history_player_uuid_idx").using("btree", table.playerUuid),
    index("player_minigame_stats_history_timestamp_idx").using("btree", table.timestamp),
  ],
);

export const playerParkourStatsHistory = pgTable(
  "player_parkour_stats_history",
  {
    id: serial().primaryKey(),
    playerUuid: text()
      .notNull()
      .references(() => players.uuid, { onDelete: "cascade" }),
    mapId: text().notNull(),
    statId: text().notNull(),
    value: jsonb().$type<unknown>().notNull(),
    timestamp: timestamp().defaultNow().notNull(),
  },
  (table) => [
    index("player_parkour_stats_history_player_uuid_idx").using("btree", table.playerUuid),
    index("player_parkour_stats_history_timestamp_idx").using("btree", table.timestamp),
  ],
);

export const playerPassiveStatsHistory = pgTable(
  "player_passive_stats_history",
  {
    id: serial().primaryKey(),
    playerUuid: text()
      .notNull()
      .references(() => players.uuid, { onDelete: "cascade" }),
    passiveId: text().notNull(),
    statId: text().notNull(),
    value: jsonb().$type<unknown>().notNull(),
    timestamp: timestamp().defaultNow().notNull(),
  },
  (table) => [
    index("player_passive_stats_history_player_uuid_idx").using("btree", table.playerUuid),
    index("player_passive_stats_history_timestamp_idx").using("btree", table.timestamp),
  ],
);

export const playerAbilityStatsHistory = pgTable(
  "player_ability_stats_history",
  {
    id: serial().primaryKey(),
    playerUuid: text()
      .notNull()
      .references(() => players.uuid, { onDelete: "cascade" }),
    abilityId: text().notNull(),
    statId: text().notNull(),
    value: jsonb().$type<unknown>().notNull(),
    timestamp: timestamp().defaultNow().notNull(),
  },
  (table) => [
    index("player_ability_stats_history_player_uuid_idx").using("btree", table.playerUuid),
    index("player_ability_stats_history_timestamp_idx").using("btree", table.timestamp),
  ],
);

// Relations

export const playerGeneralStatsRelations = relations(playerGeneralStats, ({ one }) => ({
  player: one(players, {
    fields: [playerGeneralStats.playerUuid],
    references: [players.uuid],
  }),
}));

export const playerKitStatsRelations = relations(playerKitStats, ({ one }) => ({
  player: one(players, {
    fields: [playerKitStats.playerUuid],
    references: [players.uuid],
  }),
}));

export const playerMinigameStatsRelations = relations(playerMinigameStats, ({ one }) => ({
  player: one(players, {
    fields: [playerMinigameStats.playerUuid],
    references: [players.uuid],
  }),
}));

export const playerParkourStatsRelations = relations(playerParkourStats, ({ one }) => ({
  player: one(players, {
    fields: [playerParkourStats.playerUuid],
    references: [players.uuid],
  }),
}));

export const playerPassiveStatsRelations = relations(playerPassiveStats, ({ one }) => ({
  player: one(players, {
    fields: [playerPassiveStats.playerUuid],
    references: [players.uuid],
  }),
}));

export const playerAbilityStatsRelations = relations(playerAbilityStats, ({ one }) => ({
  player: one(players, {
    fields: [playerAbilityStats.playerUuid],
    references: [players.uuid],
  }),
}));

export const playerGeneralStatsHistoryRelations = relations(
  playerGeneralStatsHistory,
  ({ one }) => ({
    player: one(players, {
      fields: [playerGeneralStatsHistory.playerUuid],
      references: [players.uuid],
    }),
  }),
);

export const playerKitStatsHistoryRelations = relations(playerKitStatsHistory, ({ one }) => ({
  player: one(players, {
    fields: [playerKitStatsHistory.playerUuid],
    references: [players.uuid],
  }),
}));

export const playerMinigameStatsHistoryRelations = relations(
  playerMinigameStatsHistory,
  ({ one }) => ({
    player: one(players, {
      fields: [playerMinigameStatsHistory.playerUuid],
      references: [players.uuid],
    }),
  }),
);

export const playerParkourStatsHistoryRelations = relations(
  playerParkourStatsHistory,
  ({ one }) => ({
    player: one(players, {
      fields: [playerParkourStatsHistory.playerUuid],
      references: [players.uuid],
    }),
  }),
);

export const playerPassiveStatsHistoryRelations = relations(
  playerPassiveStatsHistory,
  ({ one }) => ({
    player: one(players, {
      fields: [playerPassiveStatsHistory.playerUuid],
      references: [players.uuid],
    }),
  }),
);

export const playerAbilityStatsHistoryRelations = relations(
  playerAbilityStatsHistory,
  ({ one }) => ({
    player: one(players, {
      fields: [playerAbilityStatsHistory.playerUuid],
      references: [players.uuid],
    }),
  }),
);
