import { paginationOptsValidator } from "convex/server";
import { v } from "convex/values";
import { query } from "./_generated/server";
import { getKv } from "./util/kv";

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
    return await getKv(ctx, "online_player_count", 0);
  },
});
