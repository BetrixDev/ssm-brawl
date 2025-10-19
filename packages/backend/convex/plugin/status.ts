import { v } from "convex/values";
import { internalMutation } from "../_generated/server";

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

export const syncPlayerOnlineStatus = internalMutation({
  args: {
    onlinePlayerUuids: v.array(v.string()),
  },
  handler: async (ctx, args) => {
    const playerDocuments = await ctx.db.query("players").collect();

    for (const playerDocument of playerDocuments) {
      await ctx.db.patch(playerDocument._id, {
        isOnlineOnServer: args.onlinePlayerUuids.includes(playerDocument.uuid),
      });
    }
  },
});
