import {
  decimal,
  json,
  mysqlTable,
  text,
  varchar,
} from "drizzle-orm/mysql-core";

export const kitsTable = mysqlTable("kits", {
  id: varchar("id", { length: 15 }).primaryKey(),
  inventoryIcon: text("inventory_icon").notNull(),
  damage: decimal("damage").notNull(),
  armor: decimal("armor").notNull(),
  knockback: decimal("knockback").notNull(),
});
