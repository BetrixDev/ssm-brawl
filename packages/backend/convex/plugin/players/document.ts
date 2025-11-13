import { ConvexError, v } from "convex/values";
import { internal } from "../../_generated/api";
import { internalMutation, internalQuery } from "../../_generated/server";

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

export const ensurePlayerDocument = internalMutation({
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
      return;
    }

    await ctx.runMutation(internal.plugin.players.document.createPlayerDocument, {
      uuid: args.uuid,
      username: args.username,
    });
  },
});

export const getSelectedKit = internalQuery({
  args: {
    uuid: v.string(),
  },
  returns: v.string(),
  handler: async (ctx, args) => {
    const player = await ctx.db
      .query("players")
      .withIndex("by_uuid", (q) => q.eq("uuid", args.uuid))
      .first();

    if (!player) {
      throw new ConvexError("Player not found");
    }

    return player.selectedKitId;
  },
});

export const updateSelectedKit = internalMutation({
  args: {
    uuid: v.string(),
    kitId: v.string(),
  },
  returns: v.null(),
  handler: async (ctx, args) => {
    const player = await ctx.db
      .query("players")
      .withIndex("by_uuid", (q) => q.eq("uuid", args.uuid))
      .first();

    if (!player) {
      throw new ConvexError("Player not found");
    }

    await ctx.db.patch(player._id, {
      selectedKitId: args.kitId,
    });

    return null;
  },
});
