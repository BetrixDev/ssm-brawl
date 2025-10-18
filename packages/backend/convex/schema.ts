import { defineSchema, defineTable } from "convex/server";
import { v } from "convex/values";

const playerGeneralStatId = v.union(
  v.literal("join_count"),
  v.literal("play_time_seconds"),
  v.literal("damage_dealt_to_players"),
  v.literal("kills"),
  v.literal("deaths"),
);

export default defineSchema({
  players: defineTable({
    uuid: v.string(),
    username: v.string(),
    firstJoinedDate: v.string(),
    lastJoinedDate: v.string(),
    isOnlineOnServer: v.boolean(),
    selectedKitId: v.string(),
  })
    .searchIndex("search_username", {
      searchField: "username",
      filterFields: ["uuid"],
    })
    .index("by_uuid", ["uuid"]),

  playerBans: defineTable({
    playerUuid: v.string(),
    banReason: v.string(),
    bannedBy: v.string(),
    bannedAt: v.number(),
    expiresAt: v.number(),
  }).index("by_player_uuid", ["playerUuid"]),

  /**
   * Player Stats
   */

  playerGeneralStats: defineTable({
    playerUuid: v.string(),
    updatedAt: v.number(),
    statId: playerGeneralStatId,
    value: v.any(),
  })
    .index("by_player_uuid", ["playerUuid"])
    .index("by_stat_id", ["statId"])
    .index("by_player_uuid_and_stat_id", ["playerUuid", "statId"]),

  playerKitStats: defineTable({
    playerUuid: v.string(),
    updatedAt: v.number(),
    kitId: v.string(),
    statId: v.union(),
    value: v.any(),
  })
    .index("by_player_uuid", ["playerUuid"])
    .index("by_kit_id", ["kitId"])
    .index("by_player_uuid_and_stat_id", ["playerUuid", "statId"]),

  playerMinigameStats: defineTable({
    playerUuid: v.string(),
    updatedAt: v.number(),
    minigameId: v.string(),
    statId: v.union(),
    value: v.any(),
  })
    .index("by_player_uuid", ["playerUuid"])
    .index("by_minigame_id", ["minigameId"])
    .index("by_player_uuid_and_stat_id", ["playerUuid", "statId"]),

  playerParkourStats: defineTable({
    playerUuid: v.string(),
    updatedAt: v.number(),
    mapId: v.string(),
    statId: v.union(),
    value: v.any(),
  })
    .index("by_player_uuid", ["playerUuid"])
    .index("by_map_id", ["mapId"])
    .index("by_player_uuid_and_stat_id", ["playerUuid", "statId"]),

  playerPassiveStats: defineTable({
    playerUuid: v.string(),
    updatedAt: v.number(),
    passiveId: v.string(),
    statId: v.union(),
    value: v.any(),
  })
    .index("by_player_uuid", ["playerUuid"])
    .index("by_passive_id", ["passiveId"])
    .index("by_player_uuid_and_stat_id", ["playerUuid", "statId"]),

  playerAbilityStats: defineTable({
    playerUuid: v.string(),
    updatedAt: v.number(),
    abilityId: v.string(),
    statId: v.union(),
    value: v.any(),
  })
    .index("by_player_uuid", ["playerUuid"])
    .index("by_ability_id", ["abilityId"])
    .index("by_player_uuid_and_stat_id", ["playerUuid", "statId"]),

  /**
   * Player Stats History
   * Tables already have a _creationTime field, so we don't need to add it again
   */

  playerGeneralStatsHistory: defineTable({
    playerUuid: v.string(),
    statId: playerGeneralStatId,
    value: v.any(),
  })
    .index("by_player_uuid", ["playerUuid"])
    .index("by_stat_id", ["statId"])
    .index("by_player_uuid_and_stat_id", ["playerUuid", "statId"]),

  playerKitStatsHistory: defineTable({
    playerUuid: v.string(),
    kitId: v.string(),
    statId: v.union(),
    value: v.any(),
  })
    .index("by_player_uuid", ["playerUuid"])
    .index("by_kit_id", ["kitId"])
    .index("by_stat_id", ["statId"])
    .index("by_player_uuid_and_stat_id", ["playerUuid", "statId"]),

  playerMinigameStatsHistory: defineTable({
    playerUuid: v.string(),
    minigameId: v.string(),
    statId: v.union(),
    value: v.any(),
  })
    .index("by_player_uuid", ["playerUuid"])
    .index("by_minigame_id", ["minigameId"])
    .index("by_stat_id", ["statId"])
    .index("by_player_uuid_and_stat_id", ["playerUuid", "statId"]),

  playerParkourStatsHistory: defineTable({
    playerUuid: v.string(),
    mapId: v.string(),
    statId: v.union(),
    value: v.any(),
  })
    .index("by_player_uuid", ["playerUuid"])
    .index("by_map_id", ["mapId"])
    .index("by_stat_id", ["statId"])
    .index("by_player_uuid_and_stat_id", ["playerUuid", "statId"]),

  playerPassiveStatsHistory: defineTable({
    playerUuid: v.string(),
    passiveId: v.string(),
    statId: v.union(),
    value: v.any(),
  })
    .index("by_player_uuid", ["playerUuid"])
    .index("by_passive_id", ["passiveId"])
    .index("by_stat_id", ["statId"])
    .index("by_player_uuid_and_stat_id", ["playerUuid", "statId"]),

  playerAbilityStatsHistory: defineTable({
    playerUuid: v.string(),
    abilityId: v.string(),
    statId: v.union(),
    value: v.any(),
  })
    .index("by_player_uuid", ["playerUuid"])
    .index("by_ability_id", ["abilityId"])
    .index("by_stat_id", ["statId"])
    .index("by_player_uuid_and_stat_id", ["playerUuid", "statId"]),
});
