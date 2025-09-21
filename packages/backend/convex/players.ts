import { zid } from "convex-helpers/server/zod";
import { v } from "convex/values";
import { z } from "zod/v3";
import { internal } from "./_generated/api";
import { Id } from "./_generated/dataModel";
import { internalAction, internalMutation, internalQuery } from "./_generated/server";
import { zInternalMutation } from "./common";
import { playerDocumentSchema } from "./schemas";

export const savePlayerDocument = zInternalMutation({
  args: {
    uuid: zid("players"),
    document: playerDocumentSchema,
  },
  handler: async (ctx, args) => {
    if (args.document.banData) {
      const existingBan = await ctx.db
        .query("playerBans")
        .withIndex("by_uuid", (q) => q.eq("uuid", args.uuid))
        .unique();

      if (existingBan) {
        await ctx.db.patch(existingBan._id, {
          reason: args.document.banData.reason,
          expiresAt: args.document.banData.expiresAt,
        });
      } else {
        await ctx.db.insert("playerBans", {
          uuid: args.uuid,
          reason: args.document.banData.reason,
          expiresAt: args.document.banData.expiresAt,
          bannedAt: args.document.banData.bannedAt,
          bannedBy: args.document.banData.bannedBy,
        });
      }
    }

    return await ctx.db.patch(args.uuid, {
      lastJoinDate: args.document.lastJoinDate,
      stats: args.document.stats,
      avatarUrl: args.document.avatarUrl,
    });
  },
});

export const deletePlayerDocument = internalMutation({
  args: {
    uuid: v.id("players"),
  },
  handler: async (ctx, args) => {
    const player = await ctx.db
      .query("players")
      .withIndex("by_uuid", (q) => q.eq("uuid", args.uuid))
      .unique();

    if (!player) {
      throw new Error("Player not found");
    }

    await ctx.db.delete(player._id);

    return null;
  },
});

export const getPlayerByUuid = internalQuery({
  args: {
    uuid: v.string(),
  },
  handler: async (ctx, args) => {
    return await ctx.db
      .query("players")
      .withIndex("by_uuid", (q) => q.eq("uuid", args.uuid))
      .unique();
  },
});

export const getPlayerBanByUuid = internalQuery({
  args: {
    uuid: v.string(),
  },
  handler: async (ctx, args) => {
    return await ctx.db
      .query("playerBans")
      .withIndex("by_uuid", (q) => q.eq("uuid", args.uuid))
      .unique();
  },
});

const playerDbApiSchema = z.object({
  data: z.object({
    player: z.object({
      username: z.string(),
      id: z.string(),
      avatar: z.string(),
      skin_texture: z.string(),
    }),
  }),
});

export const createPlayer = internalAction({
  args: {
    uuid: v.string(),
  },
  returns: v.id("players"),
  handler: async (ctx, args) => {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 10000);

    const response = await fetch(`https://playerdb.co/api/player/minecraft/${args.uuid}`, {
      signal: controller.signal,
    }).finally(() => clearTimeout(timeout));

    if (!response.ok) {
      if (response.status >= 400 && response.status < 500) {
        throw new Error("Player not found on playerdb.co");
      }

      throw new Error("Failed to fetch player data from playerdb.co");
    }

    const json = await response.json();

    const { data } = playerDbApiSchema.parse(json);

    const minecraftPlayerData: Id<"players"> = await ctx.runMutation(
      internal.players.insertPlayer,
      {
        player: {
          username: data.player.username,
          uuid: args.uuid,
        },
        minecraftPlayerData: {
          skinTextureUrl: data.player.skin_texture,
          avatarUrl: data.player.avatar,
        },
      },
    );

    return minecraftPlayerData;
  },
});

export const insertPlayer = internalMutation({
  args: {
    player: v.object({
      username: v.string(),
      uuid: v.string(),
    }),
    minecraftPlayerData: v.object({
      skinTextureUrl: v.string(),
      avatarUrl: v.string(),
    }),
  },
  returns: v.id("players"),
  handler: async (ctx, args) => {
    return await ctx.db.insert("players", {
      username: args.player.username,
      uuid: args.player.uuid,
      firstJoinDate: new Date().toISOString(),
      lastJoinDate: new Date().toISOString(),
      skinTextureUrl: args.minecraftPlayerData.skinTextureUrl,
      avatarUrl: args.minecraftPlayerData.avatarUrl,
      stats: {
        joinCount: 1,
      },
    });
  },
});

export const updatePlayerJoin = internalMutation({
  args: {
    uuid: v.string(),
  },
  handler: async (ctx, args) => {
    const player = await ctx.db
      .query("players")
      .withIndex("by_uuid", (q) => q.eq("uuid", args.uuid))
      .unique();

    if (!player) {
      throw new Error("Player not found");
    }

    const currentJoinCount = (player.stats?.joinCount as number) || 0;

    await ctx.db.patch(player._id, {
      lastJoinDate: new Date().toISOString(),
      stats: {
        ...player.stats,
        joinCount: currentJoinCount + 1,
      },
    });

    return null;
  },
});
