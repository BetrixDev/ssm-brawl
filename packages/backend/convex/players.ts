import { paginationOptsValidator } from "convex/server";
import { v } from "convex/values";
import { query } from "./_generated/server";

export const searchByPlayerUsername = query({
  args: {
    query: v.string(),
    pagination: paginationOptsValidator,
  },
  handler: async (ctx, args) => {
    const results = await ctx.db
      .query("players")
      .withSearchIndex("search_username", (q) => q.search("username", args.query))
      .paginate(args.pagination);

    return {
      ...results,
      page: results.page.map((result) => ({
        uuid: result.uuid,
        username: result.username,
      })),
    };
  },
});

export const getOnlinePlayerCount = query({
  handler: async (ctx) => {
    const onlinePlayers = await ctx.db
      .query("players")
      .withIndex("by_is_online_on_server", (q) => q.eq("isOnlineOnServer", true))
      .collect();

    return onlinePlayers.length;
  },
});
