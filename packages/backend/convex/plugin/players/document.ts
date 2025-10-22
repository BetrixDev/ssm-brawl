import { ConvexError, v } from "convex/values";
import { internalMutation, internalQuery } from "../../_generated/server";

export const getPlayerDocument = internalQuery({
  args: {
    uuid: v.string(),
  },
  handler: async (ctx, args) => {
    const player = await ctx.db
      .query("players")
      .withIndex("by_uuid", (q) => q.eq("uuid", args.uuid))
      .first();

    if (!player) {
      return null;
    }

    return {
      uuid: player.uuid,
      username: player.username,
      selectedKitId: player.selectedKitId,
    };
  },
});

export const createPlayerDocument = internalMutation({
  args: {
    uuid: v.string(),
    username: v.string(),
  },
  handler: async (ctx, args) => {
    const existingPlayer = await ctx.db
      .query("players")
      .withIndex("by_uuid", (q) => q.eq("uuid", args.uuid))
      .first();

    if (existingPlayer) {
      throw new ConvexError("Player already exists");
    }

    const playerDocumentId = await ctx.db.insert("players", {
      uuid: args.uuid,
      username: args.username,
      selectedKitId: "skeleton",
    });

    const player = await ctx.db.get(playerDocumentId);

    if (!player) {
      throw new ConvexError("Failed to create player document");
    }

    return {
      uuid: player.uuid,
      username: player.username,
      selectedKitId: player.selectedKitId,
    };
  },
});
