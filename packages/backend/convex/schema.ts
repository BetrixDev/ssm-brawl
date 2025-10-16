import { defineSchema, defineTable } from "convex/server";
import { v } from "convex/values";

export default defineSchema({
  players: defineTable({
    uuid: v.string(),
    username: v.string(),
    firstJoinedDate: v.string(),
    lastJoinedDate: v.string(),
    isOnlineOnServer: v.boolean(),
    headSkinBase64: v.string(),
    selectedKitId: v.string(),
  }).searchIndex("search_username", {
    searchField: "username",
    filterFields: ["uuid"],
  }),

  /**
   * Player Stats
   */

  playerGeneralStats: defineTable({
    playerUuid: v.string(),
    updatedAt: v.number(),
    statId: v.string(),
    value: v.any(),
  }).index("by_player_uuid", ["playerUuid"]),

  playerKitStats: defineTable({
    playerUuid: v.string(),
    updatedAt: v.number(),
    kitId: v.string(),
    statId: v.string(),
    value: v.any(),
  })
    .index("by_player_uuid", ["playerUuid"])
    .index("by_kit_id", ["kitId"]),

  playerMinigameStats: defineTable({
    playerUuid: v.string(),
    updatedAt: v.number(),
    minigameId: v.string(),
    statId: v.string(),
    value: v.any(),
  })
    .index("by_player_uuid", ["playerUuid"])
    .index("by_minigame_id", ["minigameId"]),

  playerParkourStats: defineTable({
    playerUuid: v.string(),
    updatedAt: v.number(),
    mapId: v.string(),
    statId: v.string(),
    value: v.any(),
  })
    .index("by_player_uuid", ["playerUuid"])
    .index("by_map_id", ["mapId"]),

  playerPassiveStats: defineTable({
    playerUuid: v.string(),
    updatedAt: v.number(),
    passiveId: v.string(),
    statId: v.string(),
    value: v.any(),
  })
    .index("by_player_uuid", ["playerUuid"])
    .index("by_passive_id", ["passiveId"]),

  playerAbilityStats: defineTable({
    playerUuid: v.string(),
    updatedAt: v.number(),
    abilityId: v.string(),
    statId: v.string(),
    value: v.any(),
  })
    .index("by_player_uuid", ["playerUuid"])
    .index("by_ability_id", ["abilityId"]),

  /**
   * Player Stats History
   * Tables already have a _creationTime field, so we don't need to add it again
   */

  playerGeneralStatsHistory: defineTable({
    playerUuid: v.string(),
    statId: v.string(),
    value: v.any(),
  })
    .index("by_player_uuid", ["playerUuid"])
    .index("by_stat_id", ["statId"]),

  playerKitStatsHistory: defineTable({
    playerUuid: v.string(),
    kitId: v.string(),
    statId: v.string(),
    value: v.any(),
  })
    .index("by_player_uuid", ["playerUuid"])
    .index("by_kit_id", ["kitId"])
    .index("by_stat_id", ["statId"]),

  playerMinigameStatsHistory: defineTable({
    playerUuid: v.string(),
    minigameId: v.string(),
    statId: v.string(),
    value: v.any(),
  })
    .index("by_player_uuid", ["playerUuid"])
    .index("by_minigame_id", ["minigameId"])
    .index("by_stat_id", ["statId"]),

  playerParkourStatsHistory: defineTable({
    playerUuid: v.string(),
    mapId: v.string(),
    statId: v.string(),
    value: v.any(),
  })
    .index("by_player_uuid", ["playerUuid"])
    .index("by_map_id", ["mapId"])
    .index("by_stat_id", ["statId"]),

  playerPassiveStatsHistory: defineTable({
    playerUuid: v.string(),
    passiveId: v.string(),
    statId: v.string(),
    value: v.any(),
  })
    .index("by_player_uuid", ["playerUuid"])
    .index("by_passive_id", ["passiveId"])
    .index("by_stat_id", ["statId"]),

  playerAbilityStatsHistory: defineTable({
    playerUuid: v.string(),
    abilityId: v.string(),
    statId: v.string(),
    value: v.any(),
  })
    .index("by_player_uuid", ["playerUuid"])
    .index("by_ability_id", ["abilityId"])
    .index("by_stat_id", ["statId"]),
});
