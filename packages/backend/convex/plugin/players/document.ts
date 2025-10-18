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

    const ban = await ctx.db
      .query("playerBans")
      .withIndex("by_player_uuid", (q) => q.eq("playerUuid", args.uuid))
      .first();

    return {
      uuid: player.uuid,
      username: player.username,
      firstJoinedDate: player.firstJoinedDate,
      lastJoinedDate: player.lastJoinedDate,
      isFirstTimeOnServer: false,
      selectedKitId: player.selectedKitId,
      banData: ban
        ? {
            reason: ban.banReason,
            bannedBy: ban.bannedBy,
            bannedAt: ban.bannedAt,
            expiresAt: ban.expiresAt,
          }
        : null,
    };
  },
});

export const createPlayerDocument = internalMutation({
  args: {
    uuid: v.string(),
    username: v.string(),
  },
  handler: async (ctx, args) => {
    const playerDocumentId = await ctx.db.insert("players", {
      uuid: args.uuid,
      username: args.username,
      firstJoinedDate: new Date().toISOString(),
      lastJoinedDate: new Date().toISOString(),
      isOnlineOnServer: false,
      selectedKitId: "skeleton",
    });

    const player = await ctx.db.get(playerDocumentId);

    if (!player) {
      throw new ConvexError("Failed to create player document");
    }

    return {
      uuid: player.uuid,
      username: player.username,
      firstJoinedDate: player.firstJoinedDate,
      lastJoinedDate: player.lastJoinedDate,
      isFirstTimeOnServer: true,
      selectedKitId: player.selectedKitId,
      banData: null,
    };
  },
});
