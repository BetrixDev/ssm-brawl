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
    .index("by_uuid", ["uuid"])
    .index("by_is_online_on_server", ["isOnlineOnServer"]),

  playerBans: defineTable({
    playerUuid: v.string(),
    banReason: v.string(),
    bannedBy: v.string(),
    bannedAt: v.number(),
    expiresAt: v.number(),
  }).index("by_player_uuid", ["playerUuid"]),

  serverStatus: defineTable({
    playerCount: v.number(),
    tps: v.number(),
    memoryUsageMb: v.number(),
    loadedChunks: v.number(),
    loadedWorlds: v.number(),
    averagePlayerPing: v.number(),
  }),

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

  /**
   * Game Data - Abilities, Kits, Maps, Minigames, Passives, Disguises
   */

  abilities: defineTable({
    abilityId: v.string(),
    cooldown: v.number(),
    type: v.string(),
    itemSlot: v.number(),
    usage: v.string(),
    hotbarItem: v.string(),
    displayItem: v.string(),
    metadata: v.any(),
  }).index("by_ability_id", ["abilityId"]),

  disguises: defineTable({
    disguiseId: v.string(),
  }).index("by_disguise_id", ["disguiseId"]),

  passives: defineTable({
    passiveId: v.string(),
    userFacing: v.boolean(),
    displayItem: v.optional(v.string()),
    metadata: v.any(),
  }).index("by_passive_id", ["passiveId"]),

  kits: defineTable({
    kitId: v.string(),
    userFacing: v.optional(v.boolean()),
    meleeDamage: v.number(),
    armor: v.number(),
    knockbackMultiplier: v.number(),
    disguiseId: v.optional(v.string()),
    selectionSound: v.optional(v.string()),
    displayItem: v.optional(v.string()),
    metadata: v.optional(v.any()),
    passives: v.array(
      v.object({
        id: v.string(),
        overrides: v.optional(v.any()),
      }),
    ),
    abilities: v.array(
      v.object({
        id: v.string(),
      }),
    ),
    armorItems: v.object({
      helmet: v.union(v.string(), v.null()),
      chestplate: v.union(v.string(), v.null()),
      leggings: v.union(v.string(), v.null()),
      boots: v.union(v.string(), v.null()),
    }),
  }).index("by_kit_id", ["kitId"]),

  gameMaps: defineTable({
    mapId: v.string(),
    voidLevel: v.number(),
    maxPlayers: v.number(),
    worldBorderSize: v.number(),
    creators: v.array(v.string()),
    spectatorSpawnPoint: v.object({
      x: v.number(),
      y: v.number(),
      z: v.number(),
      yaw: v.number(),
      pitch: v.number(),
    }),
    spawnPoints: v.array(
      v.object({
        x: v.number(),
        y: v.number(),
        z: v.number(),
      }),
    ),
  }).index("by_map_id", ["mapId"]),

  hubMaps: defineTable({
    mapId: v.string(),
    voidLevel: v.number(),
    worldBorderSize: v.number(),
    creators: v.array(v.string()),
    spawnPoints: v.array(
      v.object({
        x: v.number(),
        y: v.number(),
        z: v.number(),
        yaw: v.number(),
        pitch: v.number(),
      }),
    ),
  }).index("by_map_id", ["mapId"]),

  minigames: defineTable({
    minigameId: v.string(),
    countdown: v.number(),
    type: v.string(),
    isHidden: v.boolean(),
    minPlayers: v.optional(v.number()),
    maxPlayers: v.number(),
    kitSwitchingMode: v.string(),
    allowParties: v.boolean(),
    passiveBlacklist: v.optional(v.array(v.string())),
    respawnDelaySeconds: v.number(),
    allowRejoinAfterLeave: v.boolean(),
    // Team-based fields
    playersPerTeam: v.optional(v.number()),
    amountOfTeams: v.optional(v.number()),
    stocks: v.optional(v.number()),
  }).index("by_minigame_id", ["minigameId"]),
});
