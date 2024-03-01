import { relations } from "drizzle-orm";
import { decimal, pgTable, text, uuid } from "drizzle-orm/pg-core";

export const kitsTable = pgTable("kits", {
  id: text("id").primaryKey().notNull(),
  meleeDamage: decimal("melee_damage").notNull(),
  armor: decimal("armor").notNull(),
  inventoryIcon: text("inventory_icon").notNull(),
});

export const basicPlayerDataTable = pgTable("basic_player_data", {
  uuid: uuid("uuid").primaryKey().notNull(),
  selectedKitId: text("selected_kit_id")
    .notNull()
    .references(() => kitsTable.id),
});

export const basicPlayerDataRelations = relations(
  basicPlayerDataTable,
  ({ one }) => ({
    selectedKit: one(kitsTable, {
      fields: [basicPlayerDataTable.selectedKitId],
      references: [kitsTable.id],
    }),
  })
);
