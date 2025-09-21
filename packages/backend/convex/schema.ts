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
    firstJoinDate: v.string(),
    lastJoinDate: v.string(),
    stats: v.record(v.string(), v.union(v.string(), v.number(), v.boolean())),
  })
    .index("by_username", ["username"])
    .index("by_uuid", ["uuid"]),
  playerBans: defineTable({
    uuid: v.string(),
    reason: v.string(),
    expiresAt: v.optional(v.string()),
    bannedAt: v.string(),
    bannedBy: v.union(v.string(), v.literal("system")),
  }).index("by_uuid", ["uuid"]),
});
