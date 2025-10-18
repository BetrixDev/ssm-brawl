import { v } from "convex/values";
import { internalMutation } from "../../_generated/server";

export const onPlayerJoin = internalMutation({
  args: {
    uuid: v.string(),
  },
  handler: async (ctx, args) => {
    const player = await ctx.db
      .query("players")
      .withIndex("by_uuid", (q) => q.eq("uuid", args.uuid))
      .first();

    if (!player) {
      return;
    }

    await ctx.db.patch(player._id, {
      isOnlineOnServer: true,
      lastJoinedDate: new Date().toISOString(),
    });

    const joinCountStat = await ctx.db
      .query("playerGeneralStats")
      .withIndex("by_player_uuid_and_stat_id", (q) =>
        q.eq("playerUuid", player.uuid).eq("statId", "join_count"),
      )
      .first();

    if (!joinCountStat) {
      await ctx.db.insert("playerGeneralStats", {
        playerUuid: player.uuid,
        updatedAt: Date.now(),
        statId: "join_count",
        value: 1,
      });
    } else {
      const newJoinCount = joinCountStat.value + 1;

      await ctx.db.patch(joinCountStat._id, {
        value: newJoinCount,
      });

      await ctx.db.insert("playerGeneralStatsHistory", {
        playerUuid: player.uuid,
        statId: "join_count",
        value: newJoinCount,
      });
    }
  },
});

export const onPlayerLeave = internalMutation({
  args: {
    uuid: v.string(),
  },
  handler: async (ctx, args) => {
    const player = await ctx.db
      .query("players")
      .withIndex("by_uuid", (q) => q.eq("uuid", args.uuid))
      .first();

    if (!player) {
      return;
    }
  },
});
