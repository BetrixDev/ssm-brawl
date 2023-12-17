import {
  decimal,
  json,
  mysqlTable,
  text,
  varchar,
} from "drizzle-orm/mysql-core";

export const kitsTable = mysqlTable("kits", {
  id: varchar("id", { length: 15 }).primaryKey(),
  displayName: text("display_name").notNull(),
  inventoryIcon: text("inventory_icon").notNull(),
  visualArmor: json("visual_armor")
    .$type<{ id: string; slot: string }[]>()
    .notNull(),
  passives: json("passives").$type<string[]>().default([]).notNull(),
  abilities: json("abilities").$type<string[]>().default([]).notNull(),
  damage: decimal("damage").notNull(),
  armor: decimal("armor").notNull(),
  knockback: decimal("knockback").notNull(),
});
