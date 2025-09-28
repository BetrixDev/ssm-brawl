import { relations, sql } from "drizzle-orm";
import { index, integer, jsonb, pgTable, text, timestamp } from "drizzle-orm/pg-core";

export const players = pgTable(
  "players",
  {
    uuid: text().primaryKey(),
    username: text().notNull(),
    lastJoinedDate: timestamp().defaultNow().notNull(),
    firstJoinedDate: timestamp().defaultNow().notNull(),
    stats: jsonb().$type<Record<string, string | number | boolean>>().default({}).notNull(),
    dailyLoginStreak: integer().default(0).notNull(),
    headSkinBase64: text(),
  },
  (table) => [
    index("username_search_idx").using("gin", sql`to_tsvector('english', ${table.username})`),
  ],
);

export const playersRelations = relations(players, ({ many }) => ({
  bans: many(playerBans),
  joinEvents: many(playerJoinEvents),
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
