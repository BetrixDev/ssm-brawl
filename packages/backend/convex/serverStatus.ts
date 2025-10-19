import { Id } from "./_generated/dataModel";
import { internalMutation } from "./_generated/server";

export const pruneServerStatus = internalMutation({
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
  },
});
