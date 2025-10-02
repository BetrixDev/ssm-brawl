import { relations, sql } from "drizzle-orm";
import { index, integer, sqliteTable, text } from "drizzle-orm/sqlite-core";

export const players = sqliteTable(
  "players",
  {
    uuid: text().primaryKey(),
    username: text().notNull(),
    lastJoinedDate: integer({ mode: "timestamp" })
      .default(sql`CURRENT_TIMESTAMP`)
      .notNull(),
    firstJoinedDate: integer({ mode: "timestamp" })
      .default(sql`CURRENT_TIMESTAMP`)
      .notNull(),
    stats: text({ mode: "json" })
      .$type<Record<string, string | number | boolean>>()
      .default({})
      .notNull(),
    dailyLoginStreak: integer().default(0).notNull(),
    headSkinBase64: text(),
    selectedKitId: text().notNull().default("skeleton"),
  },
  (table) => [index("username_idx").on(table.username)],
);

export const playersRelations = relations(players, ({ many }) => ({
  bans: many(playerBans),
  joinEvents: many(playerJoinEvents),
}));

export const playerBans = sqliteTable(
  "player_bans",
  {
    id: text().primaryKey(),
    playerUuid: text()
      .references(() => players.uuid, { onDelete: "cascade" })
      .notNull(),
    reason: text().notNull(),
    expiresAt: integer({ mode: "timestamp" }),
    bannedAt: integer({ mode: "timestamp" })
      .default(sql`CURRENT_TIMESTAMP`)
      .notNull(),
    bannedBy: text().notNull(),
  },
  (table) => [index("player_bans_player_uuid_idx").on(table.playerUuid)],
);

export const playerBansRelations = relations(playerBans, ({ one }) => ({
  player: one(players, {
    fields: [playerBans.playerUuid],
    references: [players.uuid],
  }),
}));

export const playerJoinEvents = sqliteTable("player_join_events", {
  id: text().primaryKey(),
  playerUuid: text()
    .references(() => players.uuid, { onDelete: "cascade" })
    .notNull(),
  timestamp: integer({ mode: "timestamp" })
    .default(sql`CURRENT_TIMESTAMP`)
    .notNull(),
  ipAddress: text(),
});

export const playerJoinEventsRelations = relations(playerJoinEvents, ({ one }) => ({
  player: one(players, {
    fields: [playerJoinEvents.playerUuid],
    references: [players.uuid],
  }),
}));
