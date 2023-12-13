import { decimal, json, pgTable, text } from "drizzle-orm/pg-core";
import { abilitiesTables } from "./abilities";

export const kitsTable = pgTable("kits", {
  id: text("id").primaryKey(),
  displayName: text("display_name").notNull(),
  inventoryIcon: text("inventory_icon").notNull(),
  visualArmor: json("visual_armor")
    .$type<{ id: string; slot: string }[]>()
    .notNull(),
  passives: json("passives").$type<string[]>().notNull(),
  abilities: text("abilities")
    .notNull()
    .references(() => abilitiesTables.id),
  damage: decimal("damage").notNull(),
  armor: decimal("armor").notNull(),
  knockback: decimal("knockback").notNull(),
});
