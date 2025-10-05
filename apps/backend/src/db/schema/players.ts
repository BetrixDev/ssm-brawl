import { relations, sql } from "drizzle-orm";
import { index, integer, pgTable, text, timestamp } from "drizzle-orm/pg-core";
import {
  playerAbilityStats,
  playerAbilityStatsHistory,
  playerGeneralStats,
  playerGeneralStatsHistory,
  playerKitStats,
  playerKitStatsHistory,
  playerMinigameStats,
  playerMinigameStatsHistory,
  playerParkourStats,
  playerParkourStatsHistory,
  playerPassiveStats,
  playerPassiveStatsHistory,
} from "./player-stats";

export const players = pgTable(
  "players",
  {
    uuid: text().primaryKey(),
    username: text().notNull(),
    lastJoinedDate: timestamp().defaultNow().notNull(),
    firstJoinedDate: timestamp().defaultNow().notNull(),
    lastSeenOnlineDate: timestamp().$defaultFn(() => new Date(0)),
    dailyLoginStreak: integer().default(0).notNull(),
    headSkinBase64: text(),
    selectedKitId: text().notNull().default("skeleton"),
  },
  (table) => [
    index("username_search_idx").using("gin", sql`to_tsvector('english', ${table.username})`),
    index("last_seen_online_date_idx").using("btree", table.lastSeenOnlineDate),
  ],
);

export const playersRelations = relations(players, ({ many }) => ({
  bans: many(playerBans),
  joinEvents: many(playerJoinEvents),
  quitEvents: many(playerQuitEvents),
  generalStats: many(playerGeneralStats),
  kitStats: many(playerKitStats),
  minigameStats: many(playerMinigameStats),
  parkourStats: many(playerParkourStats),
  passiveStats: many(playerPassiveStats),
  abilityStats: many(playerAbilityStats),
  generalStatsHistory: many(playerGeneralStatsHistory),
  kitStatsHistory: many(playerKitStatsHistory),
  minigameStatsHistory: many(playerMinigameStatsHistory),
  parkourStatsHistory: many(playerParkourStatsHistory),
  passiveStatsHistory: many(playerPassiveStatsHistory),
  abilityStatsHistory: many(playerAbilityStatsHistory),
}));

export const playerBans = pgTable(
  "player_bans",
  {
    id: text().primaryKey(),
    playerUuid: text()
      .references(() => players.uuid, { onDelete: "cascade" })
      .notNull(),
    reason: text().notNull(),
    expiresAt: timestamp(),
    bannedAt: timestamp().defaultNow().notNull(),
    bannedBy: text().notNull(),
  },
  (table) => [index("player_bans_player_uuid_idx").using("btree", table.playerUuid)],
);

export const playerBansRelations = relations(playerBans, ({ one }) => ({
  player: one(players, {
    fields: [playerBans.playerUuid],
    references: [players.uuid],
  }),
}));

export const playerJoinEvents = pgTable(
  "player_join_events",
  {
    id: text().primaryKey(),
    playerUuid: text()
      .references(() => players.uuid, { onDelete: "cascade" })
      .notNull(),
    timestamp: timestamp().defaultNow().notNull(),
    ipAddress: text(),
  },
  (table) => [
    index("player_join_events_player_uuid_idx").using("btree", table.playerUuid),
    index("player_join_events_timestamp_idx").using("btree", table.timestamp),
  ],
);

export const playerJoinEventsRelations = relations(playerJoinEvents, ({ one }) => ({
  player: one(players, {
    fields: [playerJoinEvents.playerUuid],
    references: [players.uuid],
  }),
}));

export const playerQuitEvents = pgTable(
  "player_quit_events",
  {
    id: text().primaryKey(),
    playerUuid: text()
      .references(() => players.uuid, { onDelete: "cascade" })
      .notNull(),
    timestamp: timestamp().defaultNow().notNull(),
  },
  (table) => [
    index("player_quit_events_player_uuid_idx").using("btree", table.playerUuid),
    index("player_quit_events_timestamp_idx").using("btree", table.timestamp),
  ],
);

export const playerQuitEventsRelations = relations(playerQuitEvents, ({ one }) => ({
  player: one(players, {
    fields: [playerQuitEvents.playerUuid],
    references: [players.uuid],
  }),
}));
