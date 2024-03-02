import { relations } from "drizzle-orm";
import {
  decimal,
  json,
  mysqlTable,
  tinyint,
  varchar,
} from "drizzle-orm/mysql-core";

export const kitsTable = mysqlTable("kits", {
  id: varchar("id", { length: 32 }).primaryKey().notNull(),
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
  minPlayers: tinyint("minPlayers").notNull(),
  maxPlayers: tinyint("maxPlayers").notNull(),
});

export const queueTable = mysqlTable("queue", {
  uuid: varchar("uuid", { length: 32 }).primaryKey(),
  minigameId: varchar("minigame_id", { length: 64 }).notNull(),
  playerId: varchar("player_id", { length: 32 }).notNull(),
});

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
