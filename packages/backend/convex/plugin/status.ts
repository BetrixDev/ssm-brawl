import { v } from "convex/values";
import { internalMutation } from "../_generated/server";
import { setKv } from "../util/kv";

export const updateServerStatus = internalMutation({
  args: {
    playerCount: v.number(),
    tps: v.number(),
    memoryUsageMb: v.number(),
    loadedChunks: v.number(),
    loadedWorlds: v.number(),
    averagePlayerPing: v.number(),
  },
  handler: async (ctx, args) => {
    await ctx.db.insert("serverStatus", {
      playerCount: args.playerCount,
      tps: args.tps,
      memoryUsageMb: args.memoryUsageMb,
      loadedChunks: args.loadedChunks,
      loadedWorlds: args.loadedWorlds,
      averagePlayerPing: args.averagePlayerPing,
    });
  },
});

export const syncOnlinePlayerCount = internalMutation({
  args: {
    onlinePlayerUuids: v.array(v.string()),
  },
  handler: async (ctx, args) => {
    await setKv(ctx, "online_player_count", args.onlinePlayerUuids.length);
  },
});
