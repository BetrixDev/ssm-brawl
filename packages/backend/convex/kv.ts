import { v } from "convex/values";
import { internalMutation, query } from "./_generated/server";

export const getKv = query({
  args: {
    key: v.string(),
  },
  returns: v.union(v.any(), v.null()),
  handler: async (ctx, args) => {
    const kv = await ctx.db
      .query("kv")
      .withIndex("by_key", (q) => q.eq("key", args.key))
      .unique();
    return kv?.value ?? null;
  },
});

export const setKv = internalMutation({
  args: {
    key: v.string(),
    value: v.any(),
  },
  returns: v.null(),
  handler: async (ctx, args) => {
    const existing = await ctx.db
      .query("kv")
      .withIndex("by_key", (q) => q.eq("key", args.key))
      .unique();

    if (existing) {
      await ctx.db.replace(existing._id, {
        key: args.key,
        value: args.value,
      });
    } else {
      await ctx.db.insert("kv", {
        key: args.key,
        value: args.value,
      });
    }
    return null;
  },
});
