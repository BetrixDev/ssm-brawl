import { defineSchema, defineTable } from "convex/server";
import { v } from "convex/values";

export default defineSchema({
  kv: defineTable({
    key: v.string(),
    value: v.any(),
  }).index("by_key", ["key"]),
  players: defineTable({
    username: v.string(),
    uuid: v.string(),
    skinTextureUrl: v.string(),
    avatarUrl: v.string(),
    firstJoinedAt: v.string(),
    lastJoinedAt: v.string(),
    stats: v.record(v.string(), v.any()),
  })
    .index("by_username", ["username"])
    .index("by_uuid", ["uuid"]),
});
