import { v } from "convex/values";
import { z } from "zod";
import { internal } from "./_generated/api";
import { Id } from "./_generated/dataModel";
import { internalAction, internalMutation, query } from "./_generated/server";

export const getPlayerDocument = query({
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

export const getPlayerByUuid = query({
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
    const response = await fetch(`https://playerdb.co/api/player/minecraft/${args.uuid}`);
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
      firstJoinedAt: new Date().toISOString(),
      skinTextureUrl: args.minecraftPlayerData.skinTextureUrl,
      avatarUrl: args.minecraftPlayerData.avatarUrl,
      lastJoinedAt: new Date().toISOString(),
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
      lastJoinedAt: new Date().toISOString(),
      stats: {
        ...player.stats,
        joinCount: currentJoinCount + 1,
      },
    });

    return null;
  },
});
