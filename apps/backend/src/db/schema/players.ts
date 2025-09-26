import { relations, sql } from "drizzle-orm";
import { index, jsonb, pgTable, text, timestamp } from "drizzle-orm/pg-core";

export const players = pgTable(
  "players",
  {
    uuid: text().primaryKey(),
    username: text().notNull(),
    lastJoinedDate: timestamp().defaultNow().notNull(),
    firstJoinedDate: timestamp().defaultNow().notNull(),
    stats: jsonb().$type<Record<string, string | number | boolean>>().notNull(),
    headSkinBase64: text(),
  },
  (table) => [
    index("username_search_idx").using("gin", sql`to_tsvector('english', ${table.username})`),
  ],
);

export const playersRelations = relations(players, ({ many }) => ({
  bans: many(playerBans),
}));

export const playerBans = pgTable(
  "player_bans",
  {
    id: text().primaryKey(),
    playerUuid: text().references(() => players.uuid),
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
