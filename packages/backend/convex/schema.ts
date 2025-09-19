import { defineSchema, defineTable } from "convex/server";
import { v } from "convex/values";

export default defineSchema({
  kv: defineTable({
    key: v.string(),
    value: v.any(),
  }).index("by_key", ["key"]),
  players: defineTable({
    minecraftUsername: v.string(),
    minecraftUuid: v.string(),
    firstJoinedAt: v.number(),
    lastJoinedAt: v.number(),
    stats: v.record(v.string(), v.any()),
  })
    .index("by_minecraftUsername", ["minecraftUsername"])
    .index("by_minecraftUuid", ["minecraftUuid"])
    .index("by_lastJoinedAt", ["lastJoinedAt"]),
});
