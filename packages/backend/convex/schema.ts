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
    firstJoinedAt: v.number(),
    lastJoinedAt: v.number(),
    stats: v.record(v.string(), v.any()),
  })
    .index("by_username", ["username"])
    .index("by_uuid", ["uuid"]),
});
