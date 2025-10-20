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

    const { abilities } = await ctx.runAction(internal.pluginData.fetchPluginData, {
      url: `${urlPrefix}/abilities.yml`,
    });

    const { passives } = await ctx.runAction(internal.pluginData.fetchPluginData, {
      url: `${urlPrefix}/passives.yml`,
    });

    const { kits } = await ctx.runAction(internal.pluginData.fetchPluginData, {
      url: `${urlPrefix}/kits.yml`,
    });

    await ctx.runMutation(internal.pluginData.upsertKits, {
      kits: kits,
    });

    await ctx.runMutation(internal.pluginData.upsertPassives, {
      passives: passives,
    });

    await ctx.runMutation(internal.pluginData.upsertAbilities, {
      abilities: abilities,
    });

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
          armorItems: kit.armorItems,
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
