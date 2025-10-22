import { v } from "convex/values";
import { Id } from "./_generated/dataModel";
import { internalMutation, query } from "./_generated/server";

export const pruneServerStatus = internalMutation({
  args: {},
  returns: v.null(),
  handler: async (ctx) => {
    const oneMonthAgo = Date.now() - 30 * 24 * 60 * 60 * 1000;

    // order by oldest first
    const serverStatus = await ctx.db.query("serverStatus").order("asc").collect();

    const idsToDelete: Id<"serverStatus">[] = [];

    for (const status of serverStatus) {
      if (status._creationTime < oneMonthAgo) {
        idsToDelete.push(status._id);
      } else {
        break;
      }
    }

    for (const id of idsToDelete) {
      await ctx.db.delete(id);
    }

    return null;
  },
});

/**
 * Calculate uptime percentage for a given time period.
 *
 * @param updates - Array of status updates in chronological order (oldest first)
 * @param periodStartMs - Start time of the period in milliseconds
 * @param now - Current time in milliseconds
 * @returns Uptime percentage rounded to 2 decimal places
 */
function calculateUptimePercent(
  updates: { _creationTime: number }[],
  periodStartMs: number,
  now: number,
) {
  if (updates.length === 0) {
    return 0;
  }

  const expectedIntervalMs = 60 * 1000;

  const maxGapMs = 2 * 60 * 1000;

  const startTime = Math.max(updates[0]._creationTime, periodStartMs);
  const endTime = now;
  const totalTimeMs = endTime - startTime;

  let uptimeMs = 0;

  for (let i = 0; i < updates.length; i++) {
    const currentUpdate = updates[i];
    const nextUpdate = updates[i + 1];

    if (nextUpdate) {
      const gap = nextUpdate._creationTime - currentUpdate._creationTime;

      if (gap <= maxGapMs) {
        uptimeMs += gap;
      } else {
        uptimeMs += expectedIntervalMs;
      }
    } else {
      const gap = endTime - currentUpdate._creationTime;

      if (gap <= maxGapMs) {
        uptimeMs += gap;
      } else {
        uptimeMs += expectedIntervalMs;
      }
    }
  }

  const uptimePercent = totalTimeMs > 0 ? (uptimeMs / totalTimeMs) * 100 : 0;

  return Math.round(uptimePercent * 100) / 100;
}

export const getServerStatus = query({
  handler: async (ctx) => {
    const recentStatus = await ctx.db.query("serverStatus").order("desc").first();

    if (!recentStatus) {
      return {
        isOnline: false,
        playerCount: 0,
        uptimePercent24h: 0,
        uptimePercent7d: 0,
        uptimePercent30d: 0,
        lastUpdateTime: null,
      };
    }

    const now = Date.now();

    const threeMinutesInMs = 3 * 60 * 1000;
    const isOnline = now - recentStatus._creationTime < threeMinutesInMs;

    const allStatusUpdates = await ctx.db.query("serverStatus").order("asc").collect();

    const sevenDaysAgo = now - 7 * 24 * 60 * 60 * 1000;

    const updates7d = allStatusUpdates.filter((s) => s._creationTime >= sevenDaysAgo);

    const uptimePercent7d = calculateUptimePercent(updates7d, sevenDaysAgo, now);

    return {
      isOnline,
      playerCount: recentStatus.playerCount,
      uptimePercent7d,
      lastUpdateTime: recentStatus._creationTime,
    };
  },
});

export const getHistoricServerUptimeChartData = query({
  args: {
    period: v.union(v.literal("1d"), v.literal("7d"), v.literal("30d")),
  },
  returns: v.array(
    v.object({
      date: v.string(),
      isOnline: v.boolean(),
    }),
  ),
  handler: async (ctx, args) => {
    const now = Date.now();
    const periodStartMs = getPeriodStartMs(args.period);

    const allStatusUpdates = await ctx.db.query("serverStatus").order("asc").collect();
    const updates = allStatusUpdates.filter((s) => s._creationTime >= periodStartMs);

    const intervalMs = 60 * 60 * 1000;
    const uptimeData: Array<{ date: string; isOnline: boolean }> = [];

    const maxGapMs = 2 * 60 * 1000;

    for (let timestamp = periodStartMs; timestamp <= now; timestamp += intervalMs) {
      let mostRecentUpdate = null;
      for (let i = updates.length - 1; i >= 0; i--) {
        if (updates[i]._creationTime <= timestamp) {
          mostRecentUpdate = updates[i];
          break;
        }
      }

      const isOnline = mostRecentUpdate
        ? timestamp - mostRecentUpdate._creationTime <= maxGapMs
        : false;

      uptimeData.push({
        date: new Date(timestamp).toISOString(),
        isOnline,
      });
    }

    return uptimeData;
  },
});

export const getHistoricServerTpsChartData = query({
  args: {
    period: v.union(v.literal("1d"), v.literal("7d"), v.literal("30d")),
  },
  handler: async (ctx, args) => {
    const allStatusUpdates = await ctx.db.query("serverStatus").order("asc").collect();
    const periodStartMs = getPeriodStartMs(args.period);

    const updates = allStatusUpdates.filter((s) => s._creationTime >= periodStartMs);

    const tpsData = updates.map((s) => ({
      date: new Date(s._creationTime).toISOString(),
      tps: s.tps,
    }));

    return tpsData;
  },
});

export const getHistoricServerMemoryUsageChartData = query({
  args: {
    period: v.union(v.literal("1d"), v.literal("7d"), v.literal("30d")),
  },
  handler: async (ctx, args) => {
    const allStatusUpdates = await ctx.db.query("serverStatus").order("asc").collect();
    const periodStartMs = getPeriodStartMs(args.period);

    const updates = allStatusUpdates.filter((s) => s._creationTime >= periodStartMs);

    const memoryUsageData = updates.map((s) => ({
      date: new Date(s._creationTime).toISOString(),
      memoryUsage: s.memoryUsageMb,
    }));

    return memoryUsageData;
  },
});

export const getHistoricServerLoadedChunksChartData = query({
  args: {
    period: v.union(v.literal("1d"), v.literal("7d"), v.literal("30d")),
  },
  handler: async (ctx, args) => {
    const allStatusUpdates = await ctx.db.query("serverStatus").order("asc").collect();
    const periodStartMs = getPeriodStartMs(args.period);

    const updates = allStatusUpdates.filter((s) => s._creationTime >= periodStartMs);

    const loadedChunksData = updates.map((s) => ({
      date: new Date(s._creationTime).toISOString(),
      loadedChunks: s.loadedChunks,
    }));

    return loadedChunksData;
  },
});

export const getHistoricServerLoadedWorldsChartData = query({
  args: {
    period: v.union(v.literal("1d"), v.literal("7d"), v.literal("30d")),
  },
  handler: async (ctx, args) => {
    const allStatusUpdates = await ctx.db.query("serverStatus").order("asc").collect();
    const periodStartMs = getPeriodStartMs(args.period);

    const updates = allStatusUpdates.filter((s) => s._creationTime >= periodStartMs);

    const loadedWorldsData = updates.map((s) => ({
      date: new Date(s._creationTime).toISOString(),
      loadedWorlds: s.loadedWorlds,
    }));

    return loadedWorldsData;
  },
});

export const getHistoricServerPlayerCountChartData = query({
  args: {
    period: v.union(v.literal("1d"), v.literal("7d"), v.literal("30d")),
  },
  handler: async (ctx, args) => {
    const allStatusUpdates = await ctx.db.query("serverStatus").order("asc").collect();
    const periodStartMs = getPeriodStartMs(args.period);

    const updates = allStatusUpdates.filter((s) => s._creationTime >= periodStartMs);

    const playerCountData = updates.map((s) => ({
      date: new Date(s._creationTime).toISOString(),
      playerCount: s.playerCount,
    }));

    return playerCountData;
  },
});

function getPeriodStartMs(period: string) {
  switch (period) {
    case "1d":
      return Date.now() - 24 * 60 * 60 * 1000;
    case "7d":
      return Date.now() - 7 * 24 * 60 * 60 * 1000;
    case "30d":
      return Date.now() - 30 * 24 * 60 * 60 * 1000;
    default:
      throw new Error("Invalid period");
  }
}
