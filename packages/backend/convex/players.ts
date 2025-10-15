import { paginationOptsValidator } from "convex/server";
import { v } from "convex/values";
import { query } from "./_generated/server";

export const searchByPlayerUsername = query({
  args: {
    query: v.string(),
    pagination: paginationOptsValidator,
  },
  returns: {
    isDone: v.boolean(),
    page: v.array(
      v.object({
        uuid: v.string(),
        username: v.string(),
        headSkinBase64: v.string(),
      }),
    ),
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
        headSkinBase64: result.headSkinBase64,
      })),
    };
  },
});
