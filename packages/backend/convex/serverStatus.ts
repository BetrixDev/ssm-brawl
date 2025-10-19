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
  updates: Array<{ _creationTime: number }>,
  periodStartMs: number,
  now: number,
): number {
  if (updates.length === 0) {
    return 0;
  }

  // Expected interval between updates (1 minute)
  const expectedIntervalMs = 60 * 1000;
  // Maximum gap before considering server offline (2 minutes)
  const maxGapMs = 2 * 60 * 1000;

  // Start from the earliest update in our period window
  const startTime = Math.max(updates[0]._creationTime, periodStartMs);
  const endTime = now;
  const totalTimeMs = endTime - startTime;

  let uptimeMs = 0;

  // Check gaps between consecutive updates
  for (let i = 0; i < updates.length; i++) {
    const currentUpdate = updates[i];
    const nextUpdate = updates[i + 1];

    if (nextUpdate) {
      const gap = nextUpdate._creationTime - currentUpdate._creationTime;

      if (gap <= maxGapMs) {
        // Normal gap, count as uptime
        uptimeMs += gap;
      } else {
        // Gap too large, only count the expected interval as uptime
        uptimeMs += expectedIntervalMs;
        // The rest is downtime (gap - expectedIntervalMs)
      }
    } else {
      // Last update - check gap to current time
      const gap = endTime - currentUpdate._creationTime;

      if (gap <= maxGapMs) {
        // Recent update, count as uptime
        uptimeMs += gap;
      } else {
        // Old update, only count expected interval as uptime
        uptimeMs += expectedIntervalMs;
      }
    }
  }

  // Calculate percentage
  const uptimePercent = totalTimeMs > 0 ? (uptimeMs / totalTimeMs) * 100 : 0;

  // Round to 2 decimal places
  return Math.round(uptimePercent * 100) / 100;
}

export const getServerStatus = query({
  handler: async (ctx) => {
    // Get the most recent status
    const recentStatus = await ctx.db.query("serverStatus").order("desc").first();

    // Default values if no status exists
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

    // Server is considered online if the last update was within 3 minutes
    const threeMinutesInMs = 3 * 60 * 1000;
    const isOnline = now - recentStatus._creationTime < threeMinutesInMs;

    // Get all status updates
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
