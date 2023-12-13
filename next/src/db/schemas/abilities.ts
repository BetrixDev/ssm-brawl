import { decimal, json, pgTable, text } from "drizzle-orm/pg-core";

export const abilitiesTables = pgTable("abilities", {
  id: text("id").primaryKey(),
  displayName: text("display_name").notNull(),
  toolId: text("tool_id").notNull(),
  cooldown: decimal("cooldown").notNull(),
  meta: json("meta"),
});
