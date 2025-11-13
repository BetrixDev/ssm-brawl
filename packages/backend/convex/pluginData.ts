import { v } from "convex/values";
import { parse } from "yaml";
import { internal } from "./_generated/api";
import { internalAction, internalMutation } from "./_generated/server";

export const syncPluginData = internalAction({
  args: {
    urlPrefix: v.optional(v.string()),
  },
  handler: async (ctx, args) => {
    const urlPrefix =
      args.urlPrefix ??
      "https://raw.githubusercontent.com/BetrixDev/ssm-brawl/refs/heads/reimagined/plugin/src/main/resources/data";

    const [
      { abilities },
      { passives },
      { kits },
      { gameMaps, hubMaps },
      { disguises },
      { minigames },
    ] = await Promise.all([
      ctx.runAction(internal.pluginData.fetchPluginData, {
        url: `${urlPrefix}/abilities.yml`,
      }),
      ctx.runAction(internal.pluginData.fetchPluginData, {
        url: `${urlPrefix}/passives.yml`,
      }),
      ctx.runAction(internal.pluginData.fetchPluginData, {
        url: `${urlPrefix}/kits.yml`,
      }),
      ctx.runAction(internal.pluginData.fetchPluginData, {
        url: `${urlPrefix}/maps.yml`,
      }),
      ctx.runAction(internal.pluginData.fetchPluginData, {
        url: `${urlPrefix}/disguises.yml`,
      }),
      ctx.runAction(internal.pluginData.fetchPluginData, {
        url: `${urlPrefix}/minigames.yml`,
      }),
    ]);

    await Promise.all([
      ctx.runMutation(internal.pluginData.upsertKits, {
        kits: kits,
      }),
      ctx.runMutation(internal.pluginData.upsertPassives, {
        passives: passives,
      }),
      ctx.runMutation(internal.pluginData.upsertAbilities, {
        abilities: abilities,
      }),
      ctx.runMutation(internal.pluginData.upsertGameMaps, {
        gameMaps: gameMaps,
      }),
      ctx.runMutation(internal.pluginData.upsertHubMaps, {
        hubMaps: hubMaps,
      }),
      ctx.runMutation(internal.pluginData.upsertDisguises, {
        disguises: disguises,
      }),
      ctx.runMutation(internal.pluginData.upsertMinigames, {
        minigames: minigames,
      }),
    ]);

    return "ok";
  },
});

export const fetchPluginData = internalAction({
  args: {
    url: v.string(),
  },
  handler: async (_, args) => {
    const response = await fetch(args.url);
    const data = await response.text();

    return parse(data);
  },
});

export const upsertAbilities = internalMutation({
  args: {
    abilities: v.array(v.any()),
  },
  handler: async (ctx, args) => {
    for (const ability of args.abilities) {
      const existingAbility = await ctx.db
        .query("abilities")
        .withIndex("by_ability_id", (q) => q.eq("abilityId", ability.id))
        .first();

      if (existingAbility) {
        await ctx.db.patch(existingAbility._id, {
          cooldown: ability.cooldown,
          type: ability.type,
          itemSlot: ability.itemSlot,
          usage: ability.usage,
          hotbarItem: ability.hotbarItem,
          displayItem: ability.displayItem,
          metadata: ability.metadata,
        });
      } else {
        await ctx.db.insert("abilities", {
          abilityId: ability.id,
          cooldown: ability.cooldown,
          type: ability.type,
          itemSlot: ability.itemSlot,
          usage: ability.usage,
          hotbarItem: ability.hotbarItem,
          displayItem: ability.displayItem,
          metadata: ability.metadata,
        });
      }
    }
  },
});

export const upsertPassives = internalMutation({
  args: {
    passives: v.array(v.any()),
  },
  handler: async (ctx, args) => {
    for (const passive of args.passives) {
      const existingPassive = await ctx.db
        .query("passives")
        .withIndex("by_passive_id", (q) => q.eq("passiveId", passive.id))
        .first();

      if (existingPassive) {
        await ctx.db.patch(existingPassive._id, {
          userFacing: passive.userFacing,
          displayItem: passive.displayItem,
          metadata: passive.metadata,
        });
      } else {
        await ctx.db.insert("passives", {
          passiveId: passive.id,
          userFacing: passive.userFacing,
          displayItem: passive.displayItem,
          metadata: passive.metadata,
        });
      }
    }
  },
});

export const upsertKits = internalMutation({
  args: {
    kits: v.array(v.any()),
  },
  handler: async (ctx, args) => {
    for (const kit of args.kits) {
      const existingKit = await ctx.db
        .query("kits")
        .withIndex("by_kit_id", (q) => q.eq("kitId", kit.id))
        .first();

      if (existingKit) {
        await ctx.db.patch(existingKit._id, {
          userFacing: kit.userFacing,
          displayItem: kit.displayItem,
          metadata: kit.metadata,
        });
      } else {
        await ctx.db.insert("kits", {
          kitId: kit.id,
          userFacing: kit.userFacing,
          displayItem: kit.displayItem,
          metadata: kit.metadata,
          passives: kit.passives ?? [],
          abilities: kit.abilities ?? [],
          armorItems: kit.armorItems ?? {},
          meleeDamage: kit.meleeDamage,
          armor: kit.armor,
          knockbackMultiplier: kit.knockbackMultiplier,
          disguiseId: kit.disguiseId,
          selectionSound: kit.selectionSound,
        });
      }
    }
  },
});

export const upsertGameMaps = internalMutation({
  args: {
    gameMaps: v.array(v.any()),
  },
  handler: async (ctx, args) => {
    for (const map of args.gameMaps) {
      const existingMap = await ctx.db
        .query("gameMaps")
        .withIndex("by_map_id", (q) => q.eq("mapId", map.id))
        .first();

      if (existingMap) {
        await ctx.db.patch(existingMap._id, {
          voidLevel: map.voidLevel,
          maxPlayers: map.maxPlayers,
          worldBorderSize: map.worldBorderSize,
          creators: map.creators,
          spawnPoints: map.spawnPoints,
        });
      } else {
        await ctx.db.insert("gameMaps", {
          mapId: map.id,
          voidLevel: map.voidLevel,
          maxPlayers: map.maxPlayers,
          worldBorderSize: map.worldBorderSize,
          creators: map.creators,
          spawnPoints: map.spawnPoints,
          spectatorSpawnPoint: map.spectatorSpawnPoint,
        });
      }
    }
  },
});

export const upsertHubMaps = internalMutation({
  args: {
    hubMaps: v.array(v.any()),
  },
  handler: async (ctx, args) => {
    for (const map of args.hubMaps) {
      const existingMap = await ctx.db
        .query("hubMaps")
        .withIndex("by_map_id", (q) => q.eq("mapId", map.id))
        .first();

      if (existingMap) {
        await ctx.db.patch(existingMap._id, {
          voidLevel: map.voidLevel,
          worldBorderSize: map.worldBorderSize,
          creators: map.creators,
          spawnPoints: map.spawnPoints,
        });
      } else {
        await ctx.db.insert("hubMaps", {
          mapId: map.id,
          voidLevel: map.voidLevel,
          worldBorderSize: map.worldBorderSize,
          creators: map.creators,
          spawnPoints: map.spawnPoints,
        });
      }
    }
  },
});

export const upsertDisguises = internalMutation({
  args: {
    disguises: v.array(v.any()),
  },
  handler: async (ctx, args) => {
    for (const disguise of args.disguises) {
      const existingDisguise = await ctx.db
        .query("disguises")
        .withIndex("by_disguise_id", (q) => q.eq("disguiseId", disguise.id))
        .first();

      if (existingDisguise) {
        await ctx.db.patch(existingDisguise._id, {
          disguiseId: disguise.id,
        });
      } else {
        await ctx.db.insert("disguises", {
          disguiseId: disguise.id,
        });
      }
    }
  },
});

export const upsertMinigames = internalMutation({
  args: {
    minigames: v.array(v.any()),
  },
  handler: async (ctx, args) => {
    for (const minigame of args.minigames) {
      const existingMinigame = await ctx.db
        .query("minigames")
        .withIndex("by_minigame_id", (q) => q.eq("minigameId", minigame.id))
        .first();

      if (existingMinigame) {
        await ctx.db.patch(existingMinigame._id, {
          countdown: minigame.countdown,
          type: minigame.type,
          isHidden: minigame.isHidden,
          minPlayers: minigame.minPlayers,
          maxPlayers: minigame.maxPlayers,
          kitSwitchingMode: minigame.kitSwitchingMode,
          allowParties: minigame.allowParties,
          passiveBlacklist: minigame.passiveBlacklist,
          respawnDelaySeconds: minigame.respawnDelaySeconds,
          allowRejoinAfterLeave: minigame.allowRejoinAfterLeave,
          playersPerTeam: minigame.playersPerTeam,
          amountOfTeams: minigame.amountOfTeams,
          stocks: minigame.stocks,
        });
      } else {
        await ctx.db.insert("minigames", {
          minigameId: minigame.id,
          countdown: minigame.countdown,
          type: minigame.type,
          isHidden: minigame.isHidden,
          minPlayers: minigame.minPlayers,
          maxPlayers: minigame.maxPlayers,
          kitSwitchingMode: minigame.kitSwitchingMode,
          allowParties: minigame.allowParties,
          passiveBlacklist: minigame.passiveBlacklist,
          respawnDelaySeconds: minigame.respawnDelaySeconds,
          allowRejoinAfterLeave: minigame.allowRejoinAfterLeave,
          playersPerTeam: minigame.playersPerTeam,
          amountOfTeams: minigame.amountOfTeams,
          stocks: minigame.stocks,
        });
      }
    }
  },
});
