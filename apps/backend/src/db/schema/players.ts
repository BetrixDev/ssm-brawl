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
    dailyLoginStreak: integer().default(0).notNull(),
    headSkinBase64: text(),
    selectedKitId: text().notNull().default("skeleton"),
  },
  (table) => [
    index("username_search_idx").using("gin", sql`to_tsvector('english', ${table.username})`),
  ],
);

export const playersRelations = relations(players, ({ many }) => ({
  bans: many(playerBans),
  joinEvents: many(playerJoinEvents),
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

export const playerJoinEvents = pgTable("player_join_events", {
  id: text().primaryKey(),
  playerUuid: text()
    .references(() => players.uuid, { onDelete: "cascade" })
    .notNull(),
  timestamp: timestamp().defaultNow().notNull(),
  ipAddress: text(),
});

export const playerJoinEventsRelations = relations(playerJoinEvents, ({ one }) => ({
  player: one(players, {
    fields: [playerJoinEvents.playerUuid],
    references: [players.uuid],
  }),
}));
